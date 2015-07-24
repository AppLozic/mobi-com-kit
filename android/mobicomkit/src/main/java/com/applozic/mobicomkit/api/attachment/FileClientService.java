package com.applozic.mobicomkit.api.attachment;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.MobiComKitClientService;

import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.commons.image.ImageUtils;
import com.applozic.mobicommons.file.FileUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.Date;

/**
 * Created by devashish on 26/12/14.
 */
public class FileClientService extends MobiComKitClientService {

    //Todo: Make the base folder configurable using either strings.xml or properties file
    public static final String MOBI_TEXTER_IMAGES_FOLDER = "/image";
    public static final String MOBI_TEXTER_VIDEOS_FOLDER = "/video";
    public static final String MOBI_TEXTER_OTHER_FILES_FOLDER = "/other";
    public static final String MOBI_TEXTER_THUMBNAIL_SUFIX = "/Thumbnail";
    public static final String FILE_UPLOAD_URL = "/rest/ws/file/url";
    public static final String IMAGE_DIR = "image";
    private static final String TAG = "FileClientService";
    private HttpRequestUtils httpRequestUtils;
    private static final String MAIN_FOLDER_META_DATA = "main_folder_name";

    public FileClientService(Context context) {
        super(context);
        this.httpRequestUtils = new HttpRequestUtils(context);
    }


    public String getFileUploadUrl() {
        return getBaseUrl() + FILE_UPLOAD_URL;
    }

    public static File getFilePath(String fileName, Context context, String contentType, boolean isThumbnail) {
        File filePath;
        File dir;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String folder = Utils.getMetaDataValue(context, MAIN_FOLDER_META_DATA) + MOBI_TEXTER_OTHER_FILES_FOLDER;

            if (contentType.startsWith("image")) {
                folder = Utils.getMetaDataValue(context, MAIN_FOLDER_META_DATA) + MOBI_TEXTER_IMAGES_FOLDER;
            } else if (contentType.startsWith("video")) {
                folder = Utils.getMetaDataValue(context, MAIN_FOLDER_META_DATA) + MOBI_TEXTER_VIDEOS_FOLDER;
            }
            if (isThumbnail) {
                folder = folder + MOBI_TEXTER_THUMBNAIL_SUFIX;
            }
            dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + folder);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } else {
            ContextWrapper cw = new ContextWrapper(context);
            // path to /data/data/yourapp/app_data/imageDir
            dir = cw.getDir(IMAGE_DIR, Context.MODE_PRIVATE);
        }
        // Create image name
        //String extention = "." + contentType.substring(contentType.indexOf("/") + 1);
        filePath = new File(dir, fileName);
        return filePath;
    }

    public static String saveImageToInternalStorage(Bitmap bitmapImage, String fileName, Context context, String contentType) {
        File filePath = getFilePath(fileName, context, contentType, true);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filePath.getAbsolutePath();
    }

    public static File getFilePath(String fileName, Context context, String contentType) {
        return getFilePath(fileName, context, contentType, false);
    }

    public Bitmap loadThumbnailImage(Context context, FileMeta fileMeta, int reqWidth, int reqHeight) {
        try {
            Bitmap attachedImage = null;
            String thumbnailUrl = fileMeta.getThumbnailUrl();
            String contentType = fileMeta.getContentType();
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            String imageLocalPath = getFilePath(fileMeta.getName(), context, fileMeta.getContentType(), true).getAbsolutePath();
            if (imageLocalPath != null) {
                try {
                    attachedImage = BitmapFactory.decodeFile(imageLocalPath);
                } catch (Exception ex) {
                    Log.e(TAG, "File not found on local storage: " + ex.getMessage());
                }
            }
            if (attachedImage == null) {
                HttpURLConnection connection = new MobiComKitClientService(context).openHttpConnection(thumbnailUrl);
                if (connection.getResponseCode() == 200) {
                    // attachedImage = BitmapFactory.decodeStream(connection.getInputStream(),null,options);
                    attachedImage = BitmapFactory.decodeStream(connection.getInputStream());
                    imageLocalPath = saveImageToInternalStorage(attachedImage, fileMeta.getName(), context, contentType);

                } else {
                    Log.w(TAG, "Download is failed response code is ...." + connection.getResponseCode());
                }
            }
            // Calculate inSampleSize
            options.inSampleSize = ImageUtils.calculateInSampleSize(options, 200, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            attachedImage = BitmapFactory.decodeFile(imageLocalPath, options);
            return attachedImage;
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            Log.e(TAG, "File not found on server: " + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "Exception fetching file from server: " + ex.getMessage());
        }

        return null;
    }

    public String uploadBlobImage(String path) throws UnsupportedEncodingException, AuthenticationException {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(getUploadKey());

        BasicScheme scheme = new BasicScheme();
        httppost.addHeader(scheme.authenticate(credentials, httppost));
        httpRequestUtils.addGlobalHeaders(httppost);

        try {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            FileBody fileBody = new FileBody(new File(path), ContentType.create(FileUtils.getMimeType(path)), fileName);
            builder.addPart("files[]", fileBody);
            HttpEntity entity = builder.build();
            httppost.setEntity(entity);
            HttpResponse response = httpclient.execute(httppost);
            Log.d(TAG, "Image uploaded: " + response.getStatusLine());

            return EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            Log.d(TAG, "Image not uploaded: Exception:" + e.toString());
        }
        return null;
    }

    public String getUploadKey() {
        return httpRequestUtils.getResponse(credentials, getFileUploadUrl() + "?" + new Date().getTime(), "text/plain", "text/plain");
    }
}
