package com.cloudappsync.ultra.Models;

public class FileHistory {

    private int id;
    private String file_path;
    private String file_dir;
    private String file_name;
    private String file_url;
    private String file_time;
    private String file_status;
    private int download_id;

    public FileHistory() {
    }

    public FileHistory(int id, String file_path, String file_dir, String file_name, String file_url, String file_time, String file_status, int download_id) {
        this.id = id;
        this.file_path = file_path;
        this.file_dir = file_dir;
        this.file_name = file_name;
        this.file_url = file_url;
        this.file_time = file_time;
        this.file_status = file_status;
        this.download_id = download_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    public String getFile_dir() {
        return file_dir;
    }

    public void setFile_dir(String file_dir) {
        this.file_dir = file_dir;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getFile_url() {
        return file_url;
    }

    public void setFile_url(String file_url) {
        this.file_url = file_url;
    }

    public String getFile_time() {
        return file_time;
    }

    public void setFile_time(String file_time) {
        this.file_time = file_time;
    }

    public String getFile_status() {
        return file_status;
    }

    public void setFile_status(String file_status) {
        this.file_status = file_status;
    }

    public int getDownload_id() {
        return download_id;
    }

    public void setDownload_id(int download_id) {
        this.download_id = download_id;
    }
}
