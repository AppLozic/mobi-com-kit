package com.applozic.mobicomkit.api.attachment;

/**
 * Created by adarsh on 4/10/14.
 */
public class FileMeta {

    private String keyString;
    private String suUserKeyString;
    private String blobKeyString;
    private String name;
    private int size;
    private String contentType;
    private String thumbnailUrl;
    private Long createdAtTime;

    public String getKeyString() {
        return keyString;
    }

    public void setKeyString(String keyString) {
        this.keyString = keyString;
    }

    public String getSuUserKeyString() {
        return suUserKeyString;
    }

    public void setSuUserKeyString(String suUserKeyString) {
        this.suUserKeyString = suUserKeyString;
    }

    public String getBlobKeyString() {
        return blobKeyString;
    }

    public void setBlobKeyString(String blobKeyString) {
        this.blobKeyString = blobKeyString;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCreatedAtTime() {
        return createdAtTime;
    }

    public void setCreatedAtTime(Long createdAtTime) {
        this.createdAtTime = createdAtTime;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getSizeInReadableFormat() {
        String value = "0 KB";
        if (size / 1024 >= 1024) {
            value = String.valueOf(Math.round(size / (1024 * 1024))) + " MB";
        } else {
            value = String.valueOf(Math.round(size / 1024)) + " KB";
        }
        return value;
    }

    @Override
    public String toString() {
        return "FileMeta{" +
                "keyString='" + keyString + '\'' +
                ", suUserKeyString='" + suUserKeyString + '\'' +
                ", blobKeyString='" + blobKeyString + '\'' +
                ", name='" + name + '\'' +
                ", size=" + size +
                ", contentType='" + contentType + '\'' +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", createdAtTime=" + createdAtTime +
                '}';
    }


}
