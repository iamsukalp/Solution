package com.appsters.flexx.Model;

public class ArticleModel {

    String title;
    String url;
    String user;
    long timestamp;
    long fileType;
    String userId;
    String description;
    String subject;


    public ArticleModel(){}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getFileType() {
        return fileType;
    }

    public void setFileType(long fileType) {
        this.fileType = fileType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ArticleModel(String title, String url, String user, long timestamp, long fileType, String userId, String description, String subject) {
        this.title = title;
        this.url = url;
        this.user = user;
        this.timestamp = timestamp;
        this.fileType = fileType;
        this.userId = userId;
        this.description = description;
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
