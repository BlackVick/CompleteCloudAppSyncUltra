package com.cloudappsync.ultra.Utilities;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.cloudappsync.ultra.Models.FileHistory;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteAssetHelper {

    private static final String DB_NAME = "FileHistoryDB.db";
    private static final int DB_VER = 1;

    public Database(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }

    //create folder table
    public void createTable(String licenceKey){
        //init db
        SQLiteDatabase db = getWritableDatabase();

        //create folder query
        db.execSQL(" CREATE TABLE IF NOT EXISTS " + licenceKey + " (" +
                "\"id\"" + " INTEGER NOT NULL UNIQUE," +
                "\"file_path\"" + " TEXT NOT NULL UNIQUE, " +
                "\"file_dir\"" + " TEXT NOT NULL, " +
                "\"file_name\"" + " TEXT NOT NULL, " +
                "\"file_url\"" + " TEXT NOT NULL, " +
                "\"file_time\"" + " TEXT NOT NULL, " +
                "\"file_status\"" + " TEXT NOT NULL, " +
                "\"download_id\"" + " INTEGER, " +
                "PRIMARY KEY(" + "\"id\" AUTOINCREMENT));");

        Log.d("DBTable", "Table Created");
    }

    //check if file exist
    public boolean fileExists(String licenceKey, String fullPath, String fileName){
        boolean flag = false;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        String SQLQuery = String.format("SELECT * From " + licenceKey + " WHERE file_path = '%s' AND file_name = '%s';", fullPath, fileName);
        cursor = db.rawQuery(SQLQuery, null);
        if (cursor.getCount()>0)
            flag = true;
        else
            flag = false;
        cursor.close();
        return flag;
    }

    //check if file changed
    public boolean fileChanged(String licenceKey, String fullPath, String fileName, String fileTime){
        boolean flag = false;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        String SQLQuery = String.format("SELECT * From " + licenceKey + " WHERE file_path = '%s' AND file_name = '%s';", fullPath, fileName);
        cursor = db.rawQuery(SQLQuery, null);

        //if file exist
        if (cursor.getCount()>0) {

            //get file object and compare time
            cursor.moveToNext();
            if (cursor.getString(cursor.getColumnIndex("file_time")).equals(fileTime)) {
                flag = false;
            } else {
                flag = true;
            }
        }

        //close cursor
        cursor.close();
        return flag;
    }

    //add file to history
    public void addFileToHistory(String licenceKey, String fullPath, String fileDir, String fileName, String fileUrl, String fileTime, String fileStatus){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("INSERT INTO " + licenceKey + " (file_path, file_dir, file_name, file_url, file_time, file_status) VALUES('%s', '%s', '%s', '%s', '%s', '%s');",
                fullPath,
                fileDir,
                fileName,
                fileUrl,
                fileTime,
                fileStatus);
        db.execSQL(query);
    }

    //update file to history
    public void updateFileToHistory(String licenceKey, String fullPath, String fileName, String fileTime, String fileStatus){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("UPDATE " + licenceKey + " SET file_time = '%s', file_status = '%s'  WHERE file_path = '%s' AND file_name = '%s';",
                fileTime,
                fileStatus,
                fullPath,
                fileName);
        db.execSQL(query);
    }

    //update assigned download id
    public void updateDownloadId(String licenceKey, int id, int downloadId){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("UPDATE " + licenceKey + " SET download_id = '%d'  WHERE id = '%s';",
                downloadId,
                id);
        db.execSQL(query);
    }

    //update file for complete download
    public void updateFileForCompleteDownload(String licenceKey, int id, String fileStatus){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("UPDATE " + licenceKey + " SET file_status = '%s'  WHERE id = '%d';",
                fileStatus,
                id);
        db.execSQL(query);
    }

    //get all files
    public List<FileHistory> getFiles(String licenceKey) {

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        String SQLQuery = "SELECT * From " + licenceKey + ";";
        cursor = db.rawQuery(SQLQuery, null);

        //init list
        final List<FileHistory> result = new ArrayList<>();

        //if file exist
        if (cursor.getCount()>0) {

            if (cursor.moveToFirst()){
                do {
                    result.add(new FileHistory(
                            cursor.getInt(cursor.getColumnIndex("id")),
                            cursor.getString(cursor.getColumnIndex("file_path")),
                            cursor.getString(cursor.getColumnIndex("file_dir")),
                            cursor.getString(cursor.getColumnIndex("file_name")),
                            cursor.getString(cursor.getColumnIndex("file_url")),
                            cursor.getString(cursor.getColumnIndex("file_time")),
                            cursor.getString(cursor.getColumnIndex("file_status")),
                            cursor.getInt(cursor.getColumnIndex("download_id"))
                    ));
                }while (cursor.moveToNext());
            }
        }

        return result;
    }

    //get all un downloaded files
    public List<FileHistory> getIncompleteFiles(String licenceKey) {

        //string
        String theStatus = Common.FILE_STATUS_PENDING;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        String SQLQuery = String.format("SELECT * From " + licenceKey + " WHERE file_status = '%s';", theStatus);
        cursor = db.rawQuery(SQLQuery, null);

        //init list
        final List<FileHistory> result = new ArrayList<>();

        //if file exist
        if (cursor.getCount()>0) {

            if (cursor.moveToFirst()){
                do {
                    result.add(new FileHistory(
                            cursor.getInt(cursor.getColumnIndex("id")),
                            cursor.getString(cursor.getColumnIndex("file_path")),
                            cursor.getString(cursor.getColumnIndex("file_dir")),
                            cursor.getString(cursor.getColumnIndex("file_name")),
                            cursor.getString(cursor.getColumnIndex("file_url")),
                            cursor.getString(cursor.getColumnIndex("file_time")),
                            cursor.getString(cursor.getColumnIndex("file_status")),
                            cursor.getInt(cursor.getColumnIndex("download_id"))
                    ));
                }while (cursor.moveToNext());
            }
        }

        return result;
    }

    //get file status
    public String getFileStatus(String licenceKey, int fileId) {

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        String SQLQuery = String.format("SELECT * From " + licenceKey + " WHERE id = '%s';", fileId);
        cursor = db.rawQuery(SQLQuery, null);

        //init list
        String result = "";

        //if file exist
        if (cursor.getCount()>0) {

            if (cursor.moveToFirst()){
                result = cursor.getString(cursor.getColumnIndex("file_status"));
            }
        }

        return result;
    }

    //clear file history
    public void cleanFileHistory(String licenceKey){
        SQLiteDatabase db = getReadableDatabase();
        String query = "DELETE FROM " + licenceKey + ";";
        db.execSQL(query);
    }

}
