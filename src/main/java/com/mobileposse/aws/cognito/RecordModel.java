package com.mobileposse.aws.cognito;

public class RecordModel {
    String key;
    String value;
    String syncCount;
    String syncSessionToken;

    public RecordModel() {}

    public RecordModel(String key, String value, String syncCount) {
        this.key=key;
        this.value=value;
        this.syncCount=syncCount;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSyncCount() {
        return syncCount;
    }

    public void setSyncCount(String syncCount) {
        this.syncCount = syncCount;
    }

    public String getSyncSessionToken() {
        return syncSessionToken;
    }

    public void setSyncSessionToken(String syncSessionToken) {
        this.syncSessionToken = syncSessionToken;
    }
}

