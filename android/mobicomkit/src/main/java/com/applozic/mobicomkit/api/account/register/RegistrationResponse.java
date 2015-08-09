package com.applozic.mobicomkit.api.account.register;

import com.applozic.mobicomkit.api.JsonMarker;

/**
 * @author devashish
 */
public class RegistrationResponse extends JsonMarker {

    private String message;
    private String deviceKeyString;
    private String suUserKeyString;
    private String contactNumber;
    private Long lastSyncTime;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public Long getLastSyncTime() {
        return lastSyncTime == null ? 0L : lastSyncTime;
    }

    public void setLastSyncTime(Long lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    @Override
    public String toString() {
        return "RegistrationResponse{" +
                "message='" + message + '\'' +
                ", deviceKeyString='" + deviceKeyString + '\'' +
                ", suUserKeyString='" + suUserKeyString + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", lastSyncTime='" + lastSyncTime + '\'' +
                '}';
    }
}
