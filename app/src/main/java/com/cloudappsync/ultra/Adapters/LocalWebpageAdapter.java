package com.cloudappsync.ultra.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudappsync.ultra.AllLocals;
import com.cloudappsync.ultra.Interface.ItemClickListener;
import com.cloudappsync.ultra.R;

import java.util.List;

public class LocalWebpageAdapter extends RecyclerView.Adapter<LocalWebpageAdapter.WebPageViewHolder> {

    //data
    List<String> fileList;
    Context context;
    Activity activity;

    //constructor
    public LocalWebpageAdapter(Context context, Activity activity, List<String> theList) {
        this.fileList = theList;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public WebPageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(context).inflate(R.layout.local_webpage_item, parent, false);

        return new WebPageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final WebPageViewHolder holder, final int position) {

        String currentFolder = fileList.get(position);

        holder.folderName.setText(currentFolder);

        if (currentFolder.endsWith(".html") || currentFolder.endsWith(".java") || currentFolder.endsWith(".ts") || currentFolder.endsWith(".vue")){
            holder.fileIcon.setImageResource(R.drawable.ic_html);

            //nullify click
            holder.setItemClickListener((view, position1, isLongClick) -> {

            });

        } else

        if (currentFolder.endsWith(".css")){
            holder.fileIcon.setImageResource(R.drawable.ic_css);

            //nullify click
            holder.setItemClickListener((view, position1, isLongClick) -> {

            });

        } else

        if (currentFolder.endsWith(".js")){
            holder.fileIcon.setImageResource(R.drawable.ic_js);

            //nullify click
            holder.setItemClickListener((view, position1, isLongClick) -> {

            });

        } else

        if (currentFolder.endsWith(".jpg") || currentFolder.endsWith(".jpeg") || currentFolder.endsWith(".png") || currentFolder.endsWith(".gif") || currentFolder.endsWith(".jfif")){
            holder.fileIcon.setImageResource(R.drawable.ic_picture);

            //nullify click
            holder.setItemClickListener((view, position1, isLongClick) -> {

            });

        } else

        if (currentFolder.endsWith(".mp3") || currentFolder.endsWith(".wav") || currentFolder.endsWith(".m4a") || currentFolder.endsWith(".amr")){
            holder.fileIcon.setImageResource(R.drawable.ic_music);

            //nullify click
            holder.setItemClickListener((view, position1, isLongClick) -> {

            });

        } else

        if (currentFolder.endsWith(".doc") || currentFolder.endsWith(".docx") || currentFolder.endsWith(".xls") || currentFolder.endsWith(".xlsx") || currentFolder.endsWith(".pdf") || currentFolder.endsWith(".ppt") || currentFolder.endsWith(".pptx") || currentFolder.endsWith(".txt") || currentFolder.endsWith(".csv")){
            holder.fileIcon.setImageResource(R.drawable.ic_document);

            //nullify click
            holder.setItemClickListener((view, position1, isLongClick) -> {

            });

        } else

        if (currentFolder.endsWith(".mp4") || currentFolder.endsWith(".MP4") || currentFolder.endsWith(".3gp") || currentFolder.endsWith(".mkv") || currentFolder.endsWith(".avi") || currentFolder.endsWith(".vod") || currentFolder.endsWith(".flv")){
            holder.fileIcon.setImageResource(R.drawable.ic_video);

            //nullify click
            holder.setItemClickListener((view, position1, isLongClick) -> {

            });

        } else {
            holder.fileIcon.setImageResource(R.drawable.ic_folder);

            holder.setItemClickListener((view, position1, isLongClick) -> {


                if (context instanceof AllLocals) {
                    ((AllLocals)context).fetchChildDirectories(currentFolder);
                }

            });
        }
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public class WebPageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //interface
        private ItemClickListener itemClickListener;

        //widgets
        ImageView fileIcon;
        TextView folderName;

        public WebPageViewHolder(@NonNull View itemView) {
            super(itemView);

            fileIcon = itemView.findViewById(R.id.fileIcon);
            folderName = itemView.findViewById(R.id.folderName);

            itemView.setOnClickListener(this);

        }

        public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            itemClickListener.onClick(v, getAdapterPosition(), false);
        }
    }

}
