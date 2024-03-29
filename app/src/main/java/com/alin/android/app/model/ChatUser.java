package com.alin.android.app.model;

import java.io.Serializable;
import java.util.Date;

public class ChatUser implements Serializable {
    Long uid;
    String name;
    String lastChatMessage;
    Date lastChatTime;
    boolean isGroupKey = false;

    public ChatUser() {
        super();
    }

    public ChatUser(String name, boolean isGroupKey) {
        this.name = name;
        this.isGroupKey = isGroupKey;
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

    public String getLastChatMessage() {
        return lastChatMessage;
    }

    public void setLastChatMessage(String lastChatMessage) {
        this.lastChatMessage = lastChatMessage;
    }

    public Date getLastChatTime() {
        return lastChatTime;
    }

    public void setLastChatTime(Date lastChatTime) {
        this.lastChatTime = lastChatTime;
    }

    public boolean getIsGroupKey() {
        return isGroupKey;
    }

    public void setIsGroupKey(boolean isGroupKey) {
        this.isGroupKey = isGroupKey;
    }
}
