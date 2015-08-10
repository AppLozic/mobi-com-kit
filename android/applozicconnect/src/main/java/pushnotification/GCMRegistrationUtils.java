package pushnotification;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.applozic.mobicomkit.api.account.register.RegisterUserClientService;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;

import java.io.IOException;

public class GCMRegistrationUtils extends Handler {

    private static final String GCM_SENDER_ID = "195932243324";
    private static final String TAG = "GCMRegistrationUtils";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final Activity mActivity;

    public GCMRegistrationUtils(Activity activity) {
        super();
        mActivity = activity;
    }

    @Override
    public void handleMessage(final Message msg) {
        super.handleMessage(msg);
        if (msg.what == 1) {
            final String pushnotificationId = msg.obj.toString();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        new RegisterUserClientService(mActivity).updatePushNotificationId(pushnotificationId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            Log.i(TAG, "Handler: Background registration failed");
        }
    }

    // To Register for push notification service
    public void setUpGcmNotification() {
        // Check device for Play Services APK. If check succeeds, proceed with
        // GCM registration.
        if (checkPlayServices()) {
            String regid = MobiComUserPreference.getInstance(mActivity).getDeviceRegistrationId();
            if (TextUtils.isEmpty(regid)) {
                registerInBackground(this);
            }
            Log.i(TAG, "push regid: " + regid);
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If it doesn't, display a dialog that allows users
     * to download the APK from the Google Play Store or enable it in the device's system settings.
     */

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mActivity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, mActivity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.e(TAG, "This device is not supported for Google Play Services");
                mActivity.finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Registers the application with GCM servers asynchronously. Stores the registration ID and app versionCode in the
     * application's shared preferences.
     */
    private void registerInBackground(final Handler handler) {

        new Thread(new Runnable() {

            int retryCount = 0;

            @Override
            public void run() {
                Log.i(TAG, "Registering In Background Thread");
                try {
                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(mActivity);
                    String regid = gcm.register(GCM_SENDER_ID);

                    Message msg = new Message();
                    msg.what = 1; // success
                    msg.obj = regid;
                    handler.sendMessage(msg);
                } catch (IOException ex) {
                    // Retry three times....
                    retryCount++;
                    if (retryCount < 3) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                        }
                        run();
                    } else {
                        Log.i(TAG, "Error :" + ex.getMessage() + "\n");
                        Message msg = new Message();
                        msg.what = 0; // failure
                        handler.sendMessage(msg);
                    }
                }
            }
        }).start();
    }
}