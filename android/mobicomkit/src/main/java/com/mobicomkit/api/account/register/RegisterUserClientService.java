package com.mobicomkit.api.account.register;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.mobicomkit.api.HttpRequestUtils;
import com.mobicomkit.api.MobiComKitClientService;
import com.mobicomkit.api.MobiComKitServer;
import com.mobicomkit.api.account.user.MobiComUserPreference;
import com.mobicomkit.api.account.user.User;
import com.mobicomkit.exception.Error404;
import com.mobicomkit.exception.InvalidAppId;
import com.mobicomkit.exception.NoInternetConnection;

import net.mobitexter.mobiframework.commons.core.utils.ContactNumberUtils;

import java.net.InetAddress;
import java.util.TimeZone;

/**
 * Created by devashish on 2/2/15.
 */
public class RegisterUserClientService extends MobiComKitClientService {

    private static final String TAG = "RegisterUserClient";
    private static final CharSequence INVALID_APP_ID = "INVALID_APPLICATIONID";

    public RegisterUserClientService(Context context) {
        this.context = context;
    }


    public boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name

            if (ipAddr.equals("")) {
                return false;
            } else {
                return true;
            }

        } catch (Exception e) {
            return false;
        }

    }

    public RegistrationResponse createAccount(User user) throws Exception {
        MobiComUserPreference mobiComUserPreference = MobiComUserPreference.getInstance(context);

        Gson gson = new Gson();
        user.setAppVersionCode(MobiComKitServer.MOBICOMKIT_VERSION_CODE);
        user.setApplicationId(MobiComKitServer.APPLICATION_KEY_HEADER_VALUE);
        user.setRegistrationId(mobiComUserPreference.getDeviceRegistrationId());

        if (!isInternetAvailable()) {
            throw new NoInternetConnection("No Internet Connection");
        }
        String response = HttpRequestUtils.postJsonToServer(MobiComKitServer.CREATE_ACCOUNT_URL, gson.toJson(user));

        Log.i(TAG, "Registration response is: " + response);

        if (response.contains("<html")) {
            throw new Error404("Error 404 : Server is down");
//            return null;
        }
        if (response.contains(INVALID_APP_ID)) {
            throw new InvalidAppId("Invalid Application Id");
        }
        RegistrationResponse registrationResponse = gson.fromJson(response, RegistrationResponse.class);

        //mobiComUserPreference.setCountryCode(user.getCountryCode());
        mobiComUserPreference.setUserId(user.getUserId());
        mobiComUserPreference.setContactNumber(user.getContactNumber());
        mobiComUserPreference.setEmailVerified(user.isEmailVerified());
        mobiComUserPreference.setDeviceKeyString(registrationResponse.getDeviceKeyString());
        mobiComUserPreference.setEmailIdValue(user.getEmailId());
        mobiComUserPreference.setSuUserKeyString(registrationResponse.getSuUserKeyString());
        mobiComUserPreference.setLastSyncTime(String.valueOf(registrationResponse.getLastSyncTime()));
        return registrationResponse;
    }

    public RegistrationResponse createAccount(String email, String userId, String phoneNumber, String pushNotificationId) throws Exception {
        User user = new User();
        user.setEmailId(email);
        user.setUserId(userId);
        user.setDeviceType(Short.valueOf("1"));
        user.setPrefContactAPI(Short.valueOf("2"));
        user.setTimezone(TimeZone.getDefault().getID());
        user.setRegistrationId(pushNotificationId);
        MobiComUserPreference mobiComUserPreference = MobiComUserPreference.getInstance(context);

        user.setCountryCode(mobiComUserPreference.getCountryCode());
        user.setContactNumber(ContactNumberUtils.getPhoneNumber(phoneNumber, mobiComUserPreference.getCountryCode()));

        return createAccount(user);
    }

    public void updatePushNotificationId(final String pushNotificationId) throws Exception {
        MobiComUserPreference pref = MobiComUserPreference.getInstance(context);
        //Note: In case if gcm registration is done before login then only updating in pref

        if (!TextUtils.isEmpty(pushNotificationId)) {
            pref.setDeviceRegistrationId(pushNotificationId);
        }

        if (pref.isRegistered()) {
            createAccount(pref.getEmailIdValue(), pref.getUserId(), pref.getContactNumber(), pushNotificationId);
        }
    }
}
