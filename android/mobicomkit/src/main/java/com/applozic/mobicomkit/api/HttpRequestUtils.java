package com.applozic.mobicomkit.api;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;


/**
 * Created by devashish on 28/11/14.
 */
public class HttpRequestUtils {

    private Context context;

    private static final String TAG = "HttpRequestUtils";

    private static String SOURCE_HEADER = "Source";

    private static String SOURCE_HEADER_VALUE = "1";

    public static String APPLICATION_KEY_HEADER = "Application-Key";

    public static String USERID_HEADER = "UserId-Enabled";

    public static String USERID_HEADER_VALUE = "true";

    public HttpRequestUtils(Context context) {
        this.context = context;
    }

    private void log(String message) {
        Log.i(TAG, message);
    }

    public InputStream getInputStreamFromUrl(String url)
            throws Exception {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(new HttpGet(url));
        return response.getEntity().getContent();
    }

    public String postData(UsernamePasswordCredentials credentials, String url, String contentType, String accept, String data) {
        return postData(credentials, url, contentType, accept, data, null);
    }

    public String postData(UsernamePasswordCredentials credentials, String url, String contentType, String accept, String data, List<NameValuePair> nameValuePairs) {
        Log.i(TAG, "Calling url: " + url);
        HttpPost request = new HttpPost();
        try {
            request.setURI(new URI(url));
            addGlobalHeaders(request);
            if (nameValuePairs != null && !nameValuePairs.isEmpty()) {
                request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            }
            if (!TextUtils.isEmpty(contentType)) {
                request.addHeader("Content-Type", contentType);
            }
            if (!TextUtils.isEmpty(accept)) {
                request.addHeader("Accept", accept);
            }

            request.addHeader(new BasicScheme().authenticate(credentials, request));
            HttpClient httpclient = new DefaultHttpClient();

            if (nameValuePairs != null && !nameValuePairs.isEmpty()) {
                request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            }

            if (!TextUtils.isEmpty(data)) {
                request.setEntity(new StringEntity(data, "UTF-8"));
            }
            HttpEntity httpEntity = httpclient.execute(request).getEntity();
            BufferedReader br = new BufferedReader(new InputStreamReader(httpEntity.getContent(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "Response: " + sb.toString());
            return sb.toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "Http call failed");
        return null;
    }

    public String getStringFromUrl(String url) throws Exception {
        BufferedReader br;
        br = new BufferedReader(new InputStreamReader(getInputStreamFromUrl(url), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        try {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public String postJsonToServer(String url, String data) throws Exception {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);
        httppost.addHeader("Content-Type", "application/json");
        addGlobalHeaders(httppost);
        HttpEntity entity = new StringEntity(data, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpResponse = httpclient.execute(httppost);
        String response = EntityUtils.toString(httpResponse.getEntity());
        log("response for post call is:" + response);
        return response;
    }

    public String getStringFromUrlWithPost(String url, String data) throws Exception {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);
        httppost.addHeader("Content-Type", "application/xml");
        addGlobalHeaders(httppost);
        HttpEntity entity = new StringEntity(data, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpResponse = httpclient.execute(httppost);
        String response = EntityUtils.toString(httpResponse.getEntity());
        log("response for post call is: " + response);
        return response;
    }

    public String getResponse(UsernamePasswordCredentials credentials, String url, String contentType, String accept) {
        HttpGet request = new HttpGet();
        try {
            request.setURI(new URI(url));
            if (!TextUtils.isEmpty(contentType)) {
                request.addHeader("Content-Type", contentType);
            }
            if (!TextUtils.isEmpty(accept)) {
                request.addHeader("Accept", accept);
            }

            request.addHeader(new BasicScheme().authenticate(credentials, request));
            addGlobalHeaders(request);

            HttpClient httpclient = new DefaultHttpClient();
            HttpEntity httpEntity = httpclient.execute(request).getEntity();
            if (httpEntity == null) {
                return null;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(httpEntity.getContent(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sb.toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addGlobalHeaders(HttpRequest request) {
        request.addHeader(APPLICATION_KEY_HEADER, MobiComKitClientService.getApplicationKey(context));
        request.addHeader(SOURCE_HEADER, SOURCE_HEADER_VALUE);
        request.addHeader(USERID_HEADER, USERID_HEADER_VALUE);
    }

}
