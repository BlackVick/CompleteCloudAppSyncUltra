package com.cloudappsync.ultra.Utilities;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.cloudappsync.ultra.Interface.DownloadHelper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import io.paperdb.Paper;

public class DownloadSingleFileFTPFolder extends AsyncTask<String, String, String> {

    private DownloadHelper helper;
    private long lenghtOfFile;

    public DownloadSingleFileFTPFolder(DownloadHelper helper) {
        this.helper = helper;
    }

    /* access modifiers changed from: protected */
    public void onPreExecute() {
        super.onPreExecute();
        this.helper.whenExecutionStarts();
    }

    /* access modifiers changed from: protected */
    public String doInBackground(String... f_url) {

        try {

            //file name
            String ftpFilePath = f_url[1];
            String fileName = f_url[2];
            String localFilePath = f_url[3];

            //parse url and connect to ftp
            URL url = new URL(f_url[0]);
            URLConnection connection = url.openConnection();
            connection.connect();

            //get licence
            String companyId = Paper.book().read(Common.COMPANY_ID);
            String licenceKey = Paper.book().read(Common.LICENCE_ID);

            //get length of file for progress calculation
            lenghtOfFile = connection.getContentLength();

            Log.d("Download", "Size = " + lenghtOfFile);

            //file
            File baseDir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME);
            File theDir = new File(baseDir.getAbsolutePath(), "/" + Common.LICENCED_FOLDER_NAME + "/" + companyId + "-" + licenceKey + localFilePath);
            if (!theDir.exists()){
                theDir.mkdir();
            }

            //save file to phone memory
            InputStream input = new BufferedInputStream(url.openStream(), 8192);
            OutputStream output = new FileOutputStream(theDir + "/" + fileName);
            byte[] data = new byte[1024];
            long total = 0;
            while (true) {
                int read = input.read(data);
                int count = read;
                if (read != -1) {
                    total += (long) count;
                    try {
                        publishProgress(new String[]{"" + ((int) ((100 * total) / ((long) lenghtOfFile)))});
                        output.write(data, 0, count);
                    } catch (Exception e) {
                        e = e;
                        Log.e("Error Occurred: ", e.getMessage());
                        return null;
                    }
                } else {
                    output.flush();
                    output.close();
                    input.close();
                    return null;
                }
            }


        } catch (Exception e2) {
            Log.e("Error Occurred: ", e2.getMessage());
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public void onProgressUpdate(String... progress) {
        this.helper.whileInProgress(Integer.parseInt(progress[0]));
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(String file_url) {
        this.helper.afterExecutionIsComplete();
    }

}
