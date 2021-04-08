package com.cloudappsync.ultra.HomeTabs;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudappsync.ultra.AllLocals;
import com.cloudappsync.ultra.Basic.BasicWebActivity;
import com.cloudappsync.ultra.R;
import com.cloudappsync.ultra.Utilities.Common;
import com.cloudappsync.ultra.Utilities.DownloadFromUrl;
import com.cloudappsync.ultra.Interface.DownloadHelper;
import com.cloudappsync.ultra.Ultra.WebActivity;
import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import io.paperdb.Paper;

public class WebData extends Fragment {

    //widgets
    private CardView onlineCard, localCard, listLocalsCard, downloadCard, masterUrlCard;
    private CardView visitUs;
    private ImageView visitImage;

    //download dialog
    public ProgressDialog mDialog;

    //values
    private String savedUrl = "";
    private String builtUrl = "";
    private String savedUsername = "";
    private String savedPassword = "";
    private String companyId;
    private String licenceKey;

    //file
    private File dir;
    private File localWebDirectory;

    public WebData() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_web_data, container, false);

        //values
        companyId = Paper.book().read(Common.COMPANY_ID);
        licenceKey = Paper.book().read(Common.LICENCE_ID);

        //widgets
        masterUrlCard = v.findViewById(R.id.masterUrlCard);
        onlineCard = v.findViewById(R.id.onlineCard);
        localCard = v.findViewById(R.id.localCard);
        listLocalsCard = v.findViewById(R.id.listLocalsCard);
        downloadCard = v.findViewById(R.id.downloadCard);
        visitUs = v.findViewById(R.id.visitUs);
        visitImage = v.findViewById(R.id.visitImage);

        //init
        initialize();

        return v;
    }

    private void initialize() {

        //custom image
        if (Paper.book().read(Common.CUSTOM_VISIT_IMAGE) != null && !TextUtils.isEmpty(Paper.book().read(Common.CUSTOM_VISIT_IMAGE))){

            String uri = Paper.book().read(Common.CUSTOM_VISIT_IMAGE);

            //display image
            Picasso.get()
                    .load(uri)
                    .config(Bitmap.Config.RGB_565)
                    .fit().centerCrop()
                    .into(visitImage);

        }

        //visit us
        visitUs.setOnClickListener(v -> {

            //build link
            if (Paper.book().read(Common.CUSTOM_VISIT_URL) != null && !TextUtils.isEmpty(Paper.book().read(Common.CUSTOM_VISIT_URL)))
            {

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Paper.book().read(Common.CUSTOM_VISIT_URL)));
                startActivity(browserIntent);

            } else {

                Toast.makeText(getContext(), "Please update custom link", Toast.LENGTH_LONG).show();

            }

        });

        //init directories
        initDirectories();

        //set defaults
        setDefaultValues();

        //online
        onlineCard.setOnClickListener(v -> {

            //create dialog
            android.app.AlertDialog streamDialog = new android.app.AlertDialog.Builder(getContext()).create();
            LayoutInflater inflater = this.getLayoutInflater();
            View viewOptions = inflater.inflate(R.layout.live_stream_layout, null);

            //widgets
            final EditText customLink = viewOptions.findViewById(R.id.customLink);
            final RelativeLayout cancelButton = viewOptions.findViewById(R.id.cancelButton);
            final RelativeLayout connectButton = viewOptions.findViewById(R.id.connectButton);
            final TextView connectText = viewOptions.findViewById(R.id.connectText);
            final ProgressBar connectProgress = viewOptions.findViewById(R.id.connectProgress);

            //dialog props
            streamDialog.setView(viewOptions);
            streamDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
            streamDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            //populate edt
            if (Paper.book().read(Common.CUSTOM_ONLINE_LINK_STATUS, Common.CUSTOM_ONLINE_LINK_INACTIVE).equals(Common.CUSTOM_ONLINE_LINK_ACTIVE) && !TextUtils.isEmpty(Paper.book().read(Common.CUSTOM_ONLINE_LINK))){

                customLink.setText(Paper.book().read(Common.CUSTOM_ONLINE_LINK));

            }

            //grant access
            connectButton.setOnClickListener(v2 -> {

                //fetch deta
                String theLink = customLink.getText().toString().trim();

                if (!TextUtils.isEmpty(theLink)){

                    //start loading
                    connectButton.setEnabled(false);
                    cancelButton.setEnabled(false);
                    connectText.setVisibility(View.GONE);
                    connectProgress.setVisibility(View.VISIBLE);

                    new Thread(() -> {

                        try {

                            URL url = new URL(theLink);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            int code = connection.getResponseCode();

                            if(code == 200) {

                                getActivity().runOnUiThread(() -> {

                                    //stop loading
                                    connectButton.setEnabled(true);
                                    cancelButton.setEnabled(true);
                                    connectProgress.setVisibility(View.GONE);
                                    connectText.setVisibility(View.VISIBLE);

                                    //dismiss
                                    streamDialog.dismiss();

                                    //null
                                    Intent webIntent = null;

                                    //intent
                                    if (Paper.book().read(Common.CURRENT_USER_TYPE, Common.USER_TYPE_BASIC).equals(Common.USER_TYPE_ULTRA)) {
                                        webIntent = new Intent(getContext(), WebActivity.class);
                                    } else {
                                        webIntent = new Intent(getContext(), BasicWebActivity.class);
                                    }
                                    webIntent.putExtra(Common.WEB_PAGE_MODE, Common.PAGE_NO_SYNC_MODE);
                                    webIntent.putExtra(Common.WEB_PAGE_TYPE, Common.LOAD_FROM_ONLINE);
                                    webIntent.putExtra(Common.WEB_PAGE_INTENT, theLink);
                                    startActivity(webIntent);
                                    getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_out_left);

                                });

                            } else {

                                getActivity().runOnUiThread(() -> {
                                    //stop loading
                                    connectButton.setEnabled(true);
                                    cancelButton.setEnabled(true);
                                    connectProgress.setVisibility(View.GONE);
                                    connectText.setVisibility(View.VISIBLE);

                                    Toast.makeText(getContext(), "Page not found", Toast.LENGTH_LONG).show();
                                });

                            }

                        } catch (Exception e){

                            getActivity().runOnUiThread(() -> {
                                //stop loading
                                connectButton.setEnabled(true);
                                cancelButton.setEnabled(true);
                                connectProgress.setVisibility(View.GONE);
                                connectText.setVisibility(View.VISIBLE);

                                Toast.makeText(getContext(), "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                        }

                    }).start();

                } else {

                    Toast.makeText(getContext(), "Please, provide link", Toast.LENGTH_SHORT).show();

                }

            });

            //cancel
            cancelButton.setOnClickListener(v2 -> {

                //dismiss
                streamDialog.dismiss();

                //null
                Intent webIntent = null;

                //intent
                if (Paper.book().read(Common.CURRENT_USER_TYPE, Common.USER_TYPE_BASIC).equals(Common.USER_TYPE_ULTRA)) {
                    webIntent = new Intent(getContext(), WebActivity.class);
                } else {
                    webIntent = new Intent(getContext(), BasicWebActivity.class);
                }
                webIntent.putExtra(Common.WEB_PAGE_MODE, Common.PAGE_NO_SYNC_MODE);
                webIntent.putExtra(Common.WEB_PAGE_TYPE, Common.LOAD_FROM_ONLINE);
                webIntent.putExtra(Common.WEB_PAGE_INTENT, builtUrl);
                startActivity(webIntent);
                getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_out_left);

            });

            //show dialog
            streamDialog.show();

        });

        //local
        localCard.setOnClickListener(v -> {

            //null
            Intent webIntent = null;

            //intent
            if (Paper.book().read(Common.CURRENT_USER_TYPE, Common.USER_TYPE_BASIC).equals(Common.USER_TYPE_ULTRA)) {
                webIntent = new Intent(getContext(), WebActivity.class);
            } else {
                webIntent = new Intent(getContext(), BasicWebActivity.class);
            }
            webIntent.putExtra(Common.WEB_PAGE_MODE, Common.PAGE_NO_SYNC_MODE);
            webIntent.putExtra(Common.WEB_PAGE_TYPE, Common.LOAD_FROM_LOCAL);
            webIntent.putExtra(Common.WEB_PAGE_INTENT, "file:///" + localWebDirectory.getAbsolutePath() + "/index.html");
            startActivity(webIntent);
            getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_out_left);
        });

        //list local
        listLocalsCard.setOnClickListener(v -> {
            Intent allLocalIntent = new Intent(getContext(), AllLocals.class);
            startActivity(allLocalIntent);
            getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_out_left);
        });

        //show master
        masterUrlCard.setOnClickListener(v -> {

            //show licence dialog
            showLicenceDialog();

        });

        //download zip
        downloadCard.setOnClickListener(v -> {
            //build url
            String theUrl = Paper.book().read(Common.CURRENT_MASTER_DOMAIN) + "/" + companyId + "/" + licenceKey + "/Zip/App.zip";

            //build dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Input Download URL");
            final EditText input = new EditText(getContext());
            input.setInputType(0);
            input.setText(companyId + "/" + licenceKey + "/Zip");
            builder.setView(input);
            builder.setPositiveButton("DOWNLOAD", (dialog, which) -> {

                String downloadUrl = input.getText().toString().trim();
                if (!TextUtils.isEmpty(downloadUrl)) {

                    //init progress dialog
                    mDialog = new ProgressDialog(getContext());
                    mDialog.setMessage("Downloading file. . . .");
                    mDialog.setIndeterminate(false);
                    mDialog.setMax(100);
                    mDialog.setProgressStyle(1);
                    mDialog.setCancelable(true);

                    //initialize directory
                    File baseDir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME);
                    File theDir = new File(baseDir.getAbsolutePath(), "/" + Common.LICENCE_FOLDER_DOWNLOAD_NAME);
                    if (!theDir.exists()){
                        theDir.mkdir();
                    }

                    //start download
                    new DownloadFromUrl(new DownloadHelper() {
                        @Override
                        public void afterExecutionIsComplete() {

                            //check to see if download is complete
                            getActivity().runOnUiThread(() -> {

                                mDialog.dismiss();
                                mDialog.setProgress(0);
                                Toast.makeText(getContext(), "Download Complete", Toast.LENGTH_SHORT).show();

                                //downloaded file
                                File downloadDir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME + "/" + Common.LICENCE_FOLDER_DOWNLOAD_NAME + "/App.zip");

                                //extract
                                extractDownload(downloadDir);
                            });

                        }

                        @Override
                        public void whenExecutionStarts() {

                            getActivity().runOnUiThread(() -> {
                                mDialog.show();
                            });

                        }

                        @Override
                        public void whileInProgress(int i) {

                            getActivity().runOnUiThread(() -> {
                                //change text color by progress
                                mDialog.setProgress(i);
                            });

                        }

                    }).execute(theUrl, theDir.getAbsolutePath(), "App.zip");

                    return;
                } else {
                    Toast.makeText(getContext(), "Cant download from empty url . . .", Toast.LENGTH_LONG).show();
                }
            });
            builder.setNegativeButton("CANCEL", (dialog, which) -> dialog.cancel());
            builder.show();
        });
    }

    private void showLicenceDialog() {

        //create dialog
        android.app.AlertDialog licenceDialog = new android.app.AlertDialog.Builder(getContext()).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.licence_dialog, null);

        //widgets
        final TextView companyIDTXT = viewOptions.findViewById(R.id.companyIDTXT);
        final TextView licenceKeyTXT = viewOptions.findViewById(R.id.licenceKeyTXT);

        //dialog props
        licenceDialog.setView(viewOptions);
        licenceDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        licenceDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //initialize stuff
        companyIDTXT.setText(companyId);
        licenceKeyTXT.setText(licenceKey);


        //show dialog
        licenceDialog.show();

    }

    private void initDirectories() {

        dir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME);
        if (!dir.exists()) {
            dir.mkdir();
        }

        //local
        localWebDirectory = new File(dir.getAbsolutePath(), Common.LICENCED_FOLDER_NAME + "/" + companyId + "-" + licenceKey + "/" + Common.USER_WEBPAGE_FOLDER);
        if (!localWebDirectory.exists()) {
            localWebDirectory.mkdir();
        }


    }

    private void setDefaultValues() {

        //saved url
        try {
            File dir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME + "/" + Common.LICENCED_FOLDER_NAME + "/" + companyId + "-" + licenceKey + "/" + Common.USER_CONFIG_FOLDER);
            BufferedReader reader = new BufferedReader(new FileReader(dir.getAbsoluteFile() + "/configFile.txt"));

            String line = "";
            if ((line = reader.readLine()) != null)
                savedUrl = line;
            if ((line = reader.readLine()) != null)
                savedUsername = line;
            if ((line = reader.readLine()) != null)
                savedPassword = line;
            reader.close();

            //set text
            if (Paper.book().read(Common.CUSTOM_ONLINE_LINK_STATUS, Common.CUSTOM_ONLINE_LINK_INACTIVE).equals(Common.CUSTOM_ONLINE_LINK_ACTIVE)){
                builtUrl = Paper.book().read(Common.CUSTOM_ONLINE_LINK);
            } else {
                builtUrl = Paper.book().read(Common.CURRENT_MASTER_DOMAIN) + "/" + companyId + "/" + licenceKey + "/App/Application/index.html";
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void extractDownload(File downloadFile) {

        //init file directories
        File dir = new File(Environment.getExternalStorageDirectory(), Common.BASE_FOLDER_NAME);
        File liDir = new File(dir, Common.LICENCED_FOLDER_NAME);
        File destinationFolder = new File(liDir, companyId + "-" + licenceKey);

        if (downloadFile.exists()) {

            try {
                FileInputStream fin = new FileInputStream(downloadFile);
                ZipInputStream zin = new ZipInputStream(fin);

                byte b[] = new byte[1024];

                //check lir dir
                if (!liDir.exists()) {
                    liDir.mkdir();
                }

                //overwrite former
                if (!destinationFolder.exists()){
                    destinationFolder.mkdir();
                }

                unzip(downloadFile, destinationFolder);

            } catch (Exception e) {
                Log.d("DecompressFileName", downloadFile.toString());
                Log.d("Decompress", "unzip " + e.getMessage());
            }

        } else {

            Toast.makeText(getContext(), "File doesn't exist", Toast.LENGTH_SHORT).show();

        }

    }

    public static boolean unzip(File zipFile, File destinationDir) {
        ZipFile zip = null;
        try {
            destinationDir.mkdirs();
            zip = new ZipFile(zipFile);
            Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();
            while (zipFileEntries.hasMoreElements()) {
                ZipEntry entry = zipFileEntries.nextElement();
                String entryName = entry.getName();
                File destFile = new File(destinationDir, entryName);
                File destinationParent = destFile.getParentFile();
                if (destinationParent != null && !destinationParent.exists()) {
                    destinationParent.mkdirs();
                }
                if (!entry.isDirectory()) {
                    BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                    int currentByte;
                    byte data[] = new byte[8192];
                    FileOutputStream fos = new FileOutputStream(destFile);
                    BufferedOutputStream dest = new BufferedOutputStream(fos, 8192);
                    while ((currentByte = is.read(data, 0, 8192)) != -1) {
                        dest.write(data, 0, currentByte);
                    }
                    dest.flush();
                    dest.close();
                    is.close();
                }
            }
        } catch (Exception e) {
            Log.d("Extracting", e.getMessage());
            return false;
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                    zipFile.delete();

                } catch (IOException ignored) {
                }
            }
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        //custom image
        if (Paper.book().read(Common.CUSTOM_VISIT_IMAGE) != null && !TextUtils.isEmpty(Paper.book().read(Common.CUSTOM_VISIT_IMAGE))){

            String uri = Paper.book().read(Common.CUSTOM_VISIT_IMAGE);

            //display image
            Picasso.get()
                    .load(uri)
                    .config(Bitmap.Config.RGB_565)
                    .fit().centerCrop()
                    .into(visitImage);

        }
    }
}