package com.bhuvan_kumar.Presto.model;

import android.net.Uri;

public class StoryModel {
    private String name;
    private Uri uri;
    private String path;
    private String filename;
    private double filesize;

    public StoryModel(String name, Uri uri, String path, String filename, double filesize) {
        this.name = name;
        this.uri = uri;
        this.path = path;
        this.filename = filename;
        this.filesize = filesize;
    }

    public StoryModel() {
    }


    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public double getFileSize() {
        return filesize;
    }

    public void setFileSize(double filesize) {
        this.filesize = filesize;
    }
}
