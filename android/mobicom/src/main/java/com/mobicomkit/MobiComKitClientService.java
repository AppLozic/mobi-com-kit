package com.mobicomkit;

import android.content.Context;
import android.util.Base64;

import com.mobicomkit.user.MobiComUserPreference;

import org.apache.http.auth.UsernamePasswordCredentials;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by devashish on 27/12/14.
 */
public class MobiComKitClientService {

    protected Context context;
    protected UsernamePasswordCredentials credentials;

    public MobiComKitClientService() {

    }

    public MobiComKitClientService(Context context) {
        this.context = context;
        this.credentials = getCredentials(context);
    }

    public UsernamePasswordCredentials getCredentials(Context context) {
        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);
        if (!userPreferences.isRegistered()) {
            return null;
        }
        return new UsernamePasswordCredentials(userPreferences.getEmailIdValue(), userPreferences.getDeviceKeyString());
    }

    public HttpURLConnection openHttpConnection(String urlString) throws IOException {
        HttpURLConnection httpConn;

        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();

        if (!(conn instanceof HttpURLConnection))
            throw new IOException("Not an HTTP connection");

        try {
            httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            String userCredentials = credentials.getUserName() + ":" + credentials.getPassword();
            String basicAuth = "Basic " + Base64.encodeToString(userCredentials.getBytes(), Base64.NO_WRAP);
            httpConn.setRequestProperty("Authorization", basicAuth);
            httpConn.connect();
            //Shifting this Code to individual class..this is needed so that caller can decide ..what should be done with the error
//            response = httpConn.getResponseCode();
//            if (response == HttpURLConnection.HTTP_OK) {
//                in = httpConn.getInputStream();
//
//            }

        } catch (Exception ex) {
            throw new IOException("Error connecting");
        }
        return httpConn;
    }
}
