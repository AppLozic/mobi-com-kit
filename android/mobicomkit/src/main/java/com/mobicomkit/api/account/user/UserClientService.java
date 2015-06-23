package com.mobicomkit.api.account.user;

import android.content.Context;
import android.util.Log;

//import com.mobicomkit.api.HttpRequestUtils;
import com.mobicomkit.api.HttpRequestUtils;
import com.mobicomkit.api.MobiComKitClientService;
import com.mobicomkit.api.MobiComKitServer;

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
    private HttpRequestUtils httpRequestUtils;

    public UserClientService(Context context) {
        super(context);
        this.httpRequestUtils = new HttpRequestUtils(context);
    }

    public String updateTimezone(String osuUserKeyString) {
        //Note: This can be used if user decides to change the timezone
        String response = null;
        try {
            response = httpRequestUtils.getStringFromUrl(new MobiComKitServer(context).getTimezoneUpdataeUrl()+ "?suUserKeyString=" + osuUserKeyString +
                    "&timeZone=" + URLEncoder.encode(TimeZone.getDefault().getID(), "UTF-8"));
            Log.i(TAG, "Response from sendDeviceTimezoneToServer : " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public boolean sendVerificationCodeToServer(String verificationCode) {
        try {
            String response = httpRequestUtils.getResponse(credentials,new  MobiComKitServer(context).getVerificationCodeContactNumberUrl()+ "?verificationCode=" + verificationCode, "application/json", "application/json");
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
                String url = new MobiComKitServer(context).getAppVersionUpdateUrl() + "?appVersionCode=" + MobiComKitServer.MOBICOMKIT_VERSION_CODE + "&deviceKeyString=" + deviceKeyString;
                String response = httpRequestUtils.getResponse(credentials, url, "text/plain", "text/plain");
                Log.i(TAG, "Version update response: " + response);
            }
        }).start();
    }

    public String updatePhoneNumber(String contactNumber) throws UnsupportedEncodingException {
        return httpRequestUtils.getResponse(credentials, new MobiComKitServer(context).getPhoneNumberUpdateUrl() + "?phoneNumber=" + URLEncoder.encode(contactNumber, "UTF-8"), "text/plain", "text/plain");
    }

    public void notifyFriendsAboutJoiningThePlatform() {
        String response = httpRequestUtils.getResponse(credentials, new MobiComKitServer(context).getNotifyContactsAboutJoiningMt(), "text/plain", "text/plain");
        Log.i(TAG, "Response for notify contact about joining MT: " + response);
    }

    public String sendPhoneNumberForVerification(String contactNumber, String countryCode, boolean viaSms) {
        try {
            String viaSmsParam = "";
            if (viaSms) {
                viaSmsParam = "&viaSms=true";
            }
            return httpRequestUtils.getResponse(credentials,new MobiComKitServer(context).getVerificationContactNumberUrl() + "?countryCode=" + countryCode + "&contactNumber=" + URLEncoder.encode(contactNumber, "UTF-8") + viaSmsParam, "application/json", "application/json");
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
                    String response = httpRequestUtils.postData(credentials, new MobiComKitServer(context).getSettingUpdateUrl(), "text/plain", "text/plain", null, nameValuePairs);
                    Log.i(TAG, "Response from setting update : " + response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
