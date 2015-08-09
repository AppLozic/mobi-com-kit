package com.applozic.mobicomkit.api.conversation;

import android.content.Context;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.JsonMarker;
import com.google.gson.annotations.SerializedName;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.attachment.FileMeta;

import com.applozic.mobicommons.commons.core.utils.ContactNumberUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Message extends JsonMarker {

    private Long createdAtTime = new Date().getTime();
    private String to;
    private String message;
    private String keyString;
    private String deviceKeyString;
    private String suUserKeyString;
    private boolean sent;
    private Boolean delivered;
    private Short type = MessageType.MT_OUTBOX.getValue();
    private boolean storeOnDevice;
    private String contactIds = "";
    private Long broadcastGroupId;
    private boolean sendToDevice;
    private Long scheduledAt;
    private Short source = Source.MT_MOBILE_APP.getValue();
    private Integer timeToLive;
    private boolean sentToServer = true;
    private List<String> fileMetaKeyStrings;
    private List<String> filePaths;
    private String pairedMessageKeyString;
    private long sentMessageTimeAtServer;

    public long getSentMessageTimeAtServer() {
        return sentMessageTimeAtServer;
    }

    public void setSentMessageTimeAtServer(long sentMessageTimeAtServer) {
        this.sentMessageTimeAtServer = sentMessageTimeAtServer;
    }


    private boolean canceled = false;

    private List<FileMeta> fileMetas;

    @SerializedName("id")
    private Long messageId;

    private boolean read = false;
    private boolean attDownloadInProgress;

    public Message() {

    }

    public Message(String to, String body) {
        this.to = to;
        this.message = body;
    }

    //copy constructor
    public Message(Message message) {
        //this.setKeyString(message.getKeyString());
        this.setMessage(message.getMessage());
        this.setContactIds(message.getContactIds());
        this.setCreatedAtTime(message.getCreatedAtTime());
        this.setDeviceKeyString(message.getDeviceKeyString());
        this.setSendToDevice(message.isSendToDevice());
        this.setTo(message.getTo());
        this.setType(message.getType());
        this.setSent(message.isSent());
        this.setDelivered(message.getDelivered());
        this.setStoreOnDevice(message.isStoreOnDevice());
        this.setScheduledAt(message.getScheduledAt());
        this.setSentToServer(message.isSentToServer());
        this.setSource(message.getSource());
        this.setTimeToLive(message.getTimeToLive());
        this.setFileMetas(message.getFileMetas());
        this.setFileMetaKeyStrings(message.getFileMetaKeyStrings());
        this.setFilePaths(message.getFilePaths());
        this.setBroadcastGroupId(message.getBroadcastGroupId());
        this.setRead(message.isRead());
    }

    public boolean isAttDownloadInProgress() {
        return attDownloadInProgress;
    }

    public void setAttDownloadInProgress(boolean attDownloadInProgress) {
        this.attDownloadInProgress = attDownloadInProgress;
    }

    public boolean isRead() {
        return read || isTypeOutbox() || getScheduledAt() != null;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isSelfDestruct() {
        return getTimeToLive() != null;
    }

    public boolean isUploadRequired() {
        return hasAttachment() && (fileMetas == null || fileMetas.isEmpty());
    }

    public boolean hasAttachment() {
        return ((filePaths != null && !filePaths.isEmpty()) || (fileMetas != null && !fileMetas.isEmpty()));
    }

    public boolean isAttachmentUploadInProgress() {
        return filePaths != null && !filePaths.isEmpty() && (getFileMetaKeyStrings() == null || getFileMetaKeyStrings().isEmpty());
    }

    public boolean isAttachmentDownloaded() {
        return filePaths != null && !filePaths.isEmpty();
    }

    public boolean isCall() {
        return MessageType.CALL_INCOMING.getValue().equals(type) || MessageType.CALL_OUTGOING.getValue().equals(type);
    }

    public boolean isOutgoingCall() {
        return MessageType.CALL_OUTGOING.getValue().equals(type);
    }

    public boolean isIncomingCall() {
        return MessageType.CALL_INCOMING.getValue().equals(type);
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public boolean isDummyEmptyMessage() {
        return getCreatedAtTime() != null && getCreatedAtTime() == 0 && TextUtils.isEmpty(getMessage());
    }

    public boolean isLocalMessage() {
        return TextUtils.isEmpty(getKeyString()) && isSentToServer();
    }

    public String getKeyString() {
        return keyString;
    }

    ;

    public void setKeyString(String keyString) {
        this.keyString = keyString;
    }

    ;

    public Long getCreatedAtTime() {
        return createdAtTime;
    }

    public void setCreatedAtTime(Long createdAtTime) {
        this.createdAtTime = createdAtTime;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessage() {
        return message == null ? "" : message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public Boolean getDelivered() {
        return delivered != null ? delivered : false;
    }

    public void setDelivered(Boolean delivered) {
        this.delivered = delivered;
    }

    public boolean isStoreOnDevice() {
        return storeOnDevice;
    }

    public void setStoreOnDevice(boolean storeOnDevice) {
        this.storeOnDevice = storeOnDevice;
    }

    public String getDeviceKeyString() {
        return deviceKeyString;
    }

    public void setDeviceKeyString(String deviceKeyString) {
        this.deviceKeyString = deviceKeyString;
    }

    public String getSuUserKeyString() {
        return suUserKeyString;
    }

    public void setSuUserKeyString(String suUserKeyString) {
        this.suUserKeyString = suUserKeyString;
    }

    public Short getType() {
        return type;
    }

    public void setType(Short type) {
        this.type = type;
    }

    public void processContactIds(Context context) {
        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);
        if (TextUtils.isEmpty(getContactIds())) {
            if (userPreferences.getCountryCode() != null) {
                setContactIds(ContactNumberUtils.getPhoneNumber(getTo(), userPreferences.getCountryCode()));
            } else {
                setContactIds(getTo());
            }
        }
    }

    public String getContactIds() {
        return contactIds;
    }

    public void setContactIds(String contactIds) {
        this.contactIds = contactIds;
    }

    public Long getBroadcastGroupId() {
        return broadcastGroupId;
    }

    public void setBroadcastGroupId(Long broadcastGroupId) {
        this.broadcastGroupId = broadcastGroupId;
    }

    public boolean isSendToDevice() {
        return sendToDevice;
    }

    public void setSendToDevice(boolean sendToDevice) {
        this.sendToDevice = sendToDevice;
    }

    public Long getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Long scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public boolean isSentToMany() {
        return !TextUtils.isEmpty(getTo()) && getTo().split(",").length > 1;
    }

    public boolean isSentToServer() {
        return sentToServer;
    }

    public void setSentToServer(boolean sentToServer) {
        this.sentToServer = sentToServer;
    }

    public boolean isTypeOutbox() {
        return MessageType.OUTBOX.getValue().equals(type) || MessageType.MT_OUTBOX.getValue().equals(type) ||
                MessageType.OUTBOX_SENT_FROM_DEVICE.getValue().equals(type) || MessageType.CALL_OUTGOING.getValue().equals(type);
    }

    public boolean isSentViaApp() {
        return MessageType.MT_OUTBOX.getValue().equals(this.type);
    }

    public boolean isSentViaCarrier() {
        return MessageType.OUTBOX.getValue().equals(type);
    }

    public Short getSource() {
        return source;
    }

    public void setSource(Short source) {
        this.source = source;
    }

    public Integer getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(Integer timeToLive) {
        this.timeToLive = timeToLive;
    }

    public List<String> getFileMetaKeyStrings() {
        return fileMetaKeyStrings;
    }

    public void setFileMetaKeyStrings(List<String> fileMetaKeyStrings) {
        this.fileMetaKeyStrings = fileMetaKeyStrings;
    }

    public List<String> getFilePaths() {
        return filePaths;
    }

    public void setFilePaths(List<String> filePaths) {
        this.filePaths = filePaths;
    }

    public String getPairedMessageKeyString() {
        return pairedMessageKeyString;
    }

    public void setPairedMessageKeyString(String pairedMessageKeyString) {
        this.pairedMessageKeyString = pairedMessageKeyString;
    }

    public List<FileMeta> getFileMetas() {
        return fileMetas;
    }

    public void setFileMetas(List<FileMeta> fileMetas) {
        this.fileMetas = fileMetas;
        if (getFileMetas() != null && !getFileMetas().isEmpty()) {
            fileMetaKeyStrings = new ArrayList<String>();
            for (FileMeta filemeta : getFileMetas()) {
                fileMetaKeyStrings.add(filemeta.getKeyString());
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;

        if (getKeyString() != null && message.getKeyString() != null && !getKeyString().equals(message.getKeyString())) {
            return false;
        }

        if ((isSentToMany() && !getContactIds().contains(message.getContactIds())) || (message.isSentToMany() && !message.getContactIds().contains(getContactIds()))) {
            return false;
        } else if (!isSentToMany() && !message.isSentToMany() && !PhoneNumberUtils.compare(to, message.to)) {
            return false;
        }

        if (!getMessage().equals(message.getMessage()) && !getMessage().contains(message.getMessage()) && !message.getMessage().contains(getMessage())) {
            return false;
        }

        if (getType() != null && message.getType() != null && ((isTypeOutbox() && !message.isTypeOutbox()) || (!isTypeOutbox() && message.isTypeOutbox()))) {
            return false;
        }

        if (getTimeToLive() != null && !getTimeToLive().equals(message.getTimeToLive())) {
            return false;
        } else if (getTimeToLive() != null && !getCreatedAtTime().equals(message.getCreatedAtTime())) {
            return false;
        }

        if ((getFilePaths() != null && !getFilePaths().equals(message.getFilePaths()))
                || (message.getFilePaths() != null && !message.getFilePaths().equals(getFilePaths()))) {
            return false;
        } else if (getFileMetaKeyStrings() != null && message.getFileMetaKeyStrings() != null && !getFileMetaKeyStrings().equals(message.getFileMetaKeyStrings())) {
            return false;
        }

        long createdTimeDifference = 60 * 1000;
        if (!getDelivered().equals(getDelivered())) {
            createdTimeDifference = 240 * 1000;
        }

        if (createdAtTime != null && message.getCreatedAtTime() != null &&
                Math.abs(createdAtTime - message.getCreatedAtTime()) > createdTimeDifference) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = getContactIds() != null ? getContactIds().hashCode() : 0;
        result = 31 * result + (getTo() != null ? getTo().hashCode() : 0);
        result = 31 * result + (getMessage() != null ? getMessage().hashCode() : 0);
        result = 31 * result + (getTimeToLive() != null ? getTimeToLive().hashCode() : 0);
        return result;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    @Override
    public String toString() {
        return "Message{" +
                "createdAtTime=" + createdAtTime +
                ", id=" + messageId +
                ", to='" + to + '\'' +
                ", message='" + message + '\'' +
                ", keyString='" + keyString + '\'' +
                ", deviceKeyString='" + deviceKeyString + '\'' +
                ", suUserKeyString='" + suUserKeyString + '\'' +
                ", sent=" + sent +
                ", delivered=" + delivered +
                ", type=" + type +
                ", storeOnDevice=" + storeOnDevice +
                ", contactIds='" + contactIds + '\'' +
                ", sendToDevice=" + sendToDevice +
                ", scheduledAt=" + scheduledAt +
                ", source=" + source +
                ", timeToLive=" + timeToLive +
                ", pairedMessageKeyString=" + pairedMessageKeyString +
                ", sentToServer=" + sentToServer +
                ", broadcastGroupId=" + broadcastGroupId +
                ", fileMetaKeyStrings=" + getFileMetaKeyStrings() +
                ", filePaths=" + filePaths +
                ", fileMetas=" + fileMetas +
                '}';
    }

    public static enum Source {

        DEVICE_NATIVE_APP(Short.valueOf("0")), WEB(Short.valueOf("1")), MT_MOBILE_APP(Short.valueOf("2")), API(Short.valueOf("3"));
        private Short value;

        private Source(Short c) {
            value = c;
        }

        public Short getValue() {
            return value;
        }
    }

    public static enum MessageType {

        INBOX(Short.valueOf("0")), OUTBOX(Short.valueOf("1")), DRAFT(Short.valueOf("2")),
        OUTBOX_SENT_FROM_DEVICE(Short.valueOf("3")), MT_INBOX(Short.valueOf("4")),
        MT_OUTBOX(Short.valueOf("5")), CALL_INCOMING(Short.valueOf("6")), CALL_OUTGOING(Short.valueOf("7"));
        private Short value;

        private MessageType(Short c) {
            value = c;
        }

        public Short getValue() {
            return value;
        }
    }
}