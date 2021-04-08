package com.cloudappsync.ultra.Models;

public class Domains {

    private String id;
    private String name;
    private String web_domain;
    private String ftp_host;
    private String ftp_port;

    public Domains() {
    }

    public Domains(String id, String name, String web_domain, String ftp_host, String ftp_port) {
        this.id = id;
        this.name = name;
        this.web_domain = web_domain;
        this.ftp_host = ftp_host;
        this.ftp_port = ftp_port;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getWeb_domain() {
        return web_domain;
    }

    public String getFtp_host() {
        return ftp_host;
    }

    public String getFtp_port() {
        return ftp_port;
    }
}
