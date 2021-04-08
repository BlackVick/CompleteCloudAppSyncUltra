package com.cloudappsync.ultra.Utilities;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.cloudappsync.ultra.Models.FileHistory;
import com.cloudappsync.ultra.Models.User;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.List;

public class UserDatabase extends SQLiteAssetHelper {

    private static final String DB_NAME = "UsersDB.db";
    private static final int DB_VER = 1;
    private static final String USER_TABLE = "users";

    public UserDatabase(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }

    //check if user exist
    public boolean userExists(String companyId, String licenceKey){
        boolean flag = false;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        String SQLQuery = String.format("SELECT * From " + USER_TABLE + " WHERE company_id = '%s' AND licence_key = '%s';", companyId, licenceKey);
        cursor = db.rawQuery(SQLQuery, null);
        if (cursor.getCount()>0)
            flag = true;
        else
            flag = false;
        cursor.close();
        return flag;
    }

    //add new user
    public void addNewUser(String masterUrl, String ftpHost, String ftpUser, String ftpPassword, String ftpPort, String companyId, String licenceKey){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("INSERT INTO " + USER_TABLE + " (master_url, ftp_host, ftp_user, ftp_password, ftp_port, company_id, licence_key) VALUES('%s', '%s', '%s', '%s', '%s', '%s', '%s');",
                masterUrl,
                ftpHost,
                ftpUser,
                ftpPassword,
                ftpPort,
                companyId,
                licenceKey);
        db.execSQL(query);
    }

    //get all users
    public List<User> getUsers() {

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        String SQLQuery = String.format("SELECT * From " + USER_TABLE + ";");
        cursor = db.rawQuery(SQLQuery, null);

        //init list
        final List<User> result = new ArrayList<>();

        //if file exist
        if (cursor.getCount()>0) {

            if (cursor.moveToFirst()){
                do {
                    result.add(new User(
                            cursor.getString(cursor.getColumnIndex("master_url")),
                            cursor.getString(cursor.getColumnIndex("ftp_host")),
                            cursor.getString(cursor.getColumnIndex("ftp_user")),
                            cursor.getString(cursor.getColumnIndex("ftp_password")),
                            cursor.getString(cursor.getColumnIndex("ftp_port")),
                            cursor.getString(cursor.getColumnIndex("company_id")),
                            cursor.getString(cursor.getColumnIndex("licence_key"))
                    ));
                }while (cursor.moveToNext());
            }
        }

        return result;
    }

}
