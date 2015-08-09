package com.applozic.mobicomkit.api.account.user;

import android.content.Context;
import android.util.Log;

//import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.database.MobiComDatabaseHelper;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by devashish on 24/12/14.
 */
public class UserClientService extends MobiComKitClientService {

    private static final String TAG = "UserClientService";
    public static final String SHARED_PREFERENCE_VERSION_UPDATE_KEY = "mck.version.update";
    public static final String PHONE_NUMBER_UPDATE_URL = "/rest/ws/registration/phone/number/update";
    public static final String NOTIFY_CONTACTS_ABOUT_JOINING_MT = "/rest/ws/registration/notify/contacts";
    public static final String VERIFICATION_CONTACT_NUMBER_URL =  "/rest/ws/verification/number";
    public static final String VERIFICATION_CODE_CONTACT_NUMBER_URL =  "/rest/ws/verification/code";
    public static final String APP_VERSION_UPDATE_URL =  "/rest/ws/registration/version/update";
    public static final String SETTING_UPDATE_URL = "/rest/ws/setting/single/update";
    public static final String TIMEZONE_UPDATAE_URL = "/rest/ws/setting/updateTZ";
    public static final Short MOBICOMKIT_VERSION_CODE = 71;

    private HttpRequestUtils httpRequestUtils;

    public UserClientService(Context context) {
        super(context);
        this.httpRequestUtils = new HttpRequestUtils(context);
    }

    public String getPhoneNumberUpdateUrl(){ return getBaseUrl() + PHONE_NUMBER_UPDATE_URL; }

    public  String getNotifyContactsAboutJoiningMt() { return getBaseUrl() + NOTIFY_CONTACTS_ABOUT_JOINING_MT; }

    public  String getVerificationContactNumberUrl() { return getBaseUrl() + VERIFICATION_CONTACT_NUMBER_URL; }

    public  String getVerificationCodeContactNumberUrl() { return getBaseUrl() + VERIFICATION_CODE_CONTACT_NUMBER_URL; }

    public  String getAppVersionUpdateUrl() { return getBaseUrl() + APP_VERSION_UPDATE_URL; }

    public  String getSettingUpdateUrl() { return getBaseUrl() + SETTING_UPDATE_URL; }

    public  String getTimezoneUpdataeUrl() { return getBaseUrl() + TIMEZONE_UPDATAE_URL; }

    public void logout() {
        MobiComUserPreference.getInstance(context).clearAll();
        MobiComDatabaseHelper.getInstance(context).delDatabase();
    }

    public String updateTimezone(String osuUserKeyString) {
        //Note: This can be used if user decides to change the timezone
        String response = null;
        try {
            response = httpRequestUtils.getStringFromUrl(getTimezoneUpdataeUrl()+ "?suUserKeyString=" + osuUserKeyString +
                    "&timeZone=" + URLEncoder.encode(TimeZone.getDefault().getID(), "UTF-8"));
            Log.i(TAG, "Response from sendDeviceTimezoneToServer : " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public boolean sendVerificationCodeToServer(String verificationCode) {
        try {
            String response = httpRequestUtils.getResponse(credentials,getVerificationCodeContactNumberUrl()+ "?verificationCode=" + verificationCode, "application/json", "application/json");
            JSONObject json = new JSONObject(response);
            return json.has("code") && json.get("code").equals("200");
        } catch (Exception e) {
            Log.e("Verification Code", "Got Exception while submitting verification code to server: " + e);
        }
        return false;
    }

    public void updateCodeVersion(final String deviceKeyString) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = getAppVersionUpdateUrl() + "?appVersionCode=" + MOBICOMKIT_VERSION_CODE + "&deviceKeyString=" + deviceKeyString;
                String response = httpRequestUtils.getResponse(credentials, url, "text/plain", "text/plain");
                Log.i(TAG, "Version update response: " + response);
            }
        }).start();
    }

    public String updatePhoneNumber(String contactNumber) throws UnsupportedEncodingException {
        return httpRequestUtils.getResponse(credentials, getPhoneNumberUpdateUrl() + "?phoneNumber=" + URLEncoder.encode(contactNumber, "UTF-8"), "text/plain", "text/plain");
    }

    public void notifyFriendsAboutJoiningThePlatform() {
        String response = httpRequestUtils.getResponse(credentials, getNotifyContactsAboutJoiningMt(), "text/plain", "text/plain");
        Log.i(TAG, "Response for notify contact about joining MT: " + response);
    }

    public String sendPhoneNumberForVerification(String contactNumber, String countryCode, boolean viaSms) {
        try {
            String viaSmsParam = "";
            if (viaSms) {
                viaSmsParam = "&viaSms=true";
            }
            return httpRequestUtils.getResponse(credentials,getVerificationContactNumberUrl() + "?countryCode=" + countryCode + "&contactNumber=" + URLEncoder.encode(contactNumber, "UTF-8") + viaSmsParam, "application/json", "application/json");
        } catch (Exception e) {
            Log.e("Verification Code", "Got Exception while submitting contact number for verification to server: " + e);
        }
        return null;
    }

    public void updateSetting(final String key, final String value) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                    nameValuePairs.add(new BasicNameValuePair("key", key));
                    nameValuePairs.add(new BasicNameValuePair("value", value));
                    String response = httpRequestUtils.postData(credentials, getSettingUpdateUrl(), "text/plain", "text/plain", null, nameValuePairs);
                    Log.i(TAG, "Response from setting update : " + response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
