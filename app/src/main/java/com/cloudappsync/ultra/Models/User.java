package com.cloudappsync.ultra.Models;

import androidx.annotation.Nullable;

public class User {

    private String master_url;
    private String ftp_host;
    private String ftp_user;
    private String ftp_password;
    private String ftp_port;
    private String company_id;
    private String licence_key;

    public User() {
    }

    public User(String master_url, String ftp_host, String ftp_user, String ftp_password, String ftp_port, String company_id, String licence_key) {
        this.master_url = master_url;
        this.ftp_host = ftp_host;
        this.ftp_user = ftp_user;
        this.ftp_password = ftp_password;
        this.ftp_port = ftp_port;
        this.company_id = company_id;
        this.licence_key = licence_key;
    }

    public String getMaster_url() {
        return master_url;
    }

    public void setMaster_url(String master_url) {
        this.master_url = master_url;
    }

    public String getFtp_host() {
        return ftp_host;
    }

    public void setFtp_host(String ftp_host) {
        this.ftp_host = ftp_host;
    }

    public String getFtp_user() {
        return ftp_user;
    }

    public void setFtp_user(String ftp_user) {
        this.ftp_user = ftp_user;
    }

    public String getFtp_password() {
        return ftp_password;
    }

    public void setFtp_password(String ftp_password) {
        this.ftp_password = ftp_password;
    }

    public String getFtp_port() {
        return ftp_port;
    }

    public void setFtp_port(String ftp_port) {
        this.ftp_port = ftp_port;
    }

    public String getCompany_id() {
        return company_id;
    }

    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }

    public String getLicence_key() {
        return licence_key;
    }

    public void setLicence_key(String licence_key) {
        this.licence_key = licence_key;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        boolean result = false;
        if (obj == null || obj.getClass() != getClass()) {
            result = false;
        } else {
            User user = (User) obj;
            if (this.ftp_user.equals(user.getFtp_user()) && this.company_id.equals(user.getCompany_id())  && this.licence_key.equals(user.getLicence_key())) {
                result = true;
            }
        }
        return result;
    }

}
