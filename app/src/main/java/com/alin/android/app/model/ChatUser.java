package com.alin.android.app.model;

public class ChatUser {
    Long uid;

    String name;

    public ChatUser() {
        super();
    }

    public ChatUser(Long uid, String name) {
        super();
        this.uid = uid;
        this.name = name;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
