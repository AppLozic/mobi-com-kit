package net.mobitexter.mobiframework.commons.core.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import net.mobitexter.mobiframework.R;

/**
 * This class contains static utility methods.
 */
public class Utils {

    private static final String TAG = "Utils";

    // Prevents instantiation.
    private Utils() {
    }

    /**
     * Enables strict mode. This should only be called when debugging the application and is useful
     * for finding some potential bugs or best practice violations.
     */
    @TargetApi(11)
    public static void enableStrictMode() {
        // Strict mode is only available on gingerbread or later
        if (Utils.hasGingerbread()) {

            // Enable all thread strict mode policies
            StrictMode.ThreadPolicy.Builder threadPolicyBuilder =
                    new StrictMode.ThreadPolicy.Builder()
                            .detectAll()
                            .penaltyLog();

            // Enable all VM strict mode policies
            StrictMode.VmPolicy.Builder vmPolicyBuilder =
                    new StrictMode.VmPolicy.Builder()
                            .detectAll()
                            .penaltyLog();

            // Honeycomb introduced some additional strict mode features
            if (Utils.hasHoneycomb()) {
                // Flash screen when thread policy is violated
                threadPolicyBuilder.penaltyFlashScreen();
                // For each activity class, set an instance limit of 1. Any more instances and
                // there could be a memory leak.
               /* vmPolicyBuilder
                        .setClassInstanceLimit(ContactActivity.class, 1);*/
            }

            // Use builders to enable strict mode policies
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.setVmPolicy(vmPolicyBuilder.build());
        }
    }

    /**
     * Uses static final constants to detect if the device's platform version is Gingerbread or
     * later.
     */
    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    /**
     * Uses static final constants to detect if the device's platform version is Honeycomb or
     * later.
     */
    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    /**
     * Uses static final constants to detect if the device's platform version is Honeycomb MR1 or
     * later.
     */
    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    /**
     * Uses static final constants to detect if the device's platform version is ICS or
     * later.
     */
    public static boolean hasICS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static boolean hasKitkat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Could not get package name: " + e);
        }
        return -1;
    }

    public static void toggleSoftKeyBoard(Activity activity, boolean hide) {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            return;
        }
        if (hide) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } else {
            inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public static boolean isNetworkAvailable(final Activity activity) {
        if (activity == null) {
            return true;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity, R.string.internet_connection_not_available, Toast.LENGTH_LONG).show();
                }
            });
        }
        return activeNetworkInfo != null;
    }

    public static String getDeviceInformation() {
        StringBuilder deviceString = new StringBuilder();
        deviceString.append("Phone Model = ")
                .append(Build.MODEL).append("::Android Version = ").append(Build.VERSION.SDK_INT).append(":::MANUFACTURER  = ").append(Build.MANUFACTURER).append("::Overall PRODUCT = ").append(Build.PRODUCT);

        return deviceString.toString();
    }

    public static Uri getUserImageUri(Context context) {
        if (Build.VERSION.SDK_INT > 14) {
            String[] mProjection = new String[]
                    {
                            ContactsContract.Profile._ID,
                            ContactsContract.Profile.DISPLAY_NAME_PRIMARY,
                            ContactsContract.Profile.LOOKUP_KEY,
                            ContactsContract.Profile.PHOTO_THUMBNAIL_URI
                    };
            Cursor mProfileCursor =
                    context.getContentResolver().query(
                            ContactsContract.Profile.CONTENT_URI,
                            mProjection, null, null, null);

            try {
                if (mProfileCursor.moveToFirst()) {
                    String photo = mProfileCursor.getString(mProfileCursor.getColumnIndex(ContactsContract.Profile.PHOTO_THUMBNAIL_URI));
                    if (!TextUtils.isEmpty(photo)) {
                        return Uri.parse(photo);
                    }
                }
            } finally {
                if (mProfileCursor != null) {
                    mProfileCursor.close();
                }
            }
        }
        return null;
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }
}
