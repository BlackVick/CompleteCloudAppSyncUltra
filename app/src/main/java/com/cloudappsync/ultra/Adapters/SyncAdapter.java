package com.cloudappsync.ultra.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudappsync.ultra.*;
import com.cloudappsync.ultra.Interface.DownloadHelper;
import com.cloudappsync.ultra.Interface.ItemClickListener;
import com.cloudappsync.ultra.Models.FileHistory;
import com.cloudappsync.ultra.Utilities.Common;
import com.cloudappsync.ultra.Utilities.Database;
import com.cloudappsync.ultra.Utilities.DownloadFromUrl;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;

import java.io.File;
import java.util.List;

import io.paperdb.Paper;

public class SyncAdapter extends RecyclerView.Adapter<SyncAdapter.SyncViewHolder> {

    //data
    List<FileHistory> fileList;
    Context context;
    Activity activity;

    //constructor
    public SyncAdapter(Context context, Activity activity, List<FileHistory> theList) {
        this.fileList = theList;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public SyncViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(context).inflate(R.layout.sync_file_item, parent, false);

        return new SyncViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final SyncViewHolder holder, final int position) {

        FileHistory currentFile = fileList.get(position);

        //bind view
        holder.fileName.setText(currentFile.getFile_name());
        holder.filePath.setText(currentFile.getFile_path());
        holder.fileDownloadProgress.setProgress(100);
        holder.fileProgressText.setText("100 %");

    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public class SyncViewHolder extends RecyclerView.ViewHolder {

        //widgets
        public TextView fileName, filePath, fileProgressText;
        public ProgressBar fileDownloadProgress;

        public SyncViewHolder(@NonNull View itemView) {
            super(itemView);

            fileName = itemView.findViewById(R.id.fileName);
            filePath = itemView.findViewById(R.id.filePath);
            fileProgressText = itemView.findViewById(R.id.fileProgressText);
            fileDownloadProgress = itemView.findViewById(R.id.fileDownloadProgress);

        }

    }

}
