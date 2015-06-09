package com.mobicomkit.api.people;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.mobicomkit.api.HttpRequestUtils;
import com.mobicomkit.api.MobiComKitClientService;
import com.mobicomkit.api.MobiComKitServer;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * Created by devashish on 23/12/14.
 */
public class MTUserClientService extends MobiComKitClientService {

    public MTUserClientService(Context context) {
        super(context);
    }

    public ContactContent getContactContent(String contactNumber) {
        String response = null;
        try {
            response = new HttpRequestUtils(context).getResponse(credentials, MobiComKitServer.CHECK_FOR_MT_USER + "?requestSource=1&contactNumber=" + URLEncoder.encode(contactNumber, "UTF-8"), "text/plain", "application/json");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(response)) {
            Gson gson = new Gson();
            return gson.fromJson(response, ContactContent.class);
        }
        return null;
    }
}
