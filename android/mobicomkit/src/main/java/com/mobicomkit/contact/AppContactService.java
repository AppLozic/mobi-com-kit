package com.mobicomkit.contact;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.mobicomkit.api.MobiComKitClientService;
import com.mobicomkit.api.attachment.FileClientService;
import com.mobicomkit.api.attachment.FileMeta;
import com.mobicomkit.contact.database.ContactDatabase;

import net.mobitexter.mobiframework.commons.image.ImageUtils;
import net.mobitexter.mobiframework.people.contact.Contact;

import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.util.List;

/**
 * Created by adarsh on 7/7/15.
 */
public class AppContactService implements BaseContactService {

    private static final String TAG ="AppContactService" ;
    ContactDatabase contactDatabase;
    Context context;


    public AppContactService(Context context) {
        this.context = context;
        this.contactDatabase = new ContactDatabase(context);

    }

    @Override
    public void add(Contact contact) {
        contactDatabase.addContact(contact);
    }

    @Override
    public void addAll(List<Contact> contactList) {
        contactDatabase.addAllContact(contactList);
    }

    @Override
    public void deleteContact(Contact contact) {
        contactDatabase.deleteContact(contact);
    }

    @Override
    public void deleteContactById(String contactId) {
        contactDatabase.deleteContactById(contactId);

    }


    @Override
    public List<Contact> getAll() {
        return contactDatabase.getAllContact();
    }

    @Override
    public Contact getContactById(String contactId) {
        return contactDatabase.getContactById(contactId);
    }

    @Override
    public void updateContact(Contact contact) {
         contactDatabase.updateContact(contact);
    }


    @Override
    public Bitmap downloadContactImage(Context context, Contact contact) {
            try {
                Bitmap attachedImage = null;
                String contactImageURL = contact.getImageURL();
                String contentType = "image";
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                String imageLocalPath = contact.getLocalImageUrl();
                if (imageLocalPath != null) {
                    try {
                        attachedImage = BitmapFactory.decodeFile(imageLocalPath);
                    } catch (Exception ex) {
                        Log.e(TAG, "File not found on local storage: " + ex.getMessage());
                    }
                }
                if (attachedImage == null) {
                    HttpURLConnection connection = new MobiComKitClientService(context).openHttpConnection(contactImageURL);
                    if (connection.getResponseCode() == 200) {
                        attachedImage = BitmapFactory.decodeStream(connection.getInputStream());
                        imageLocalPath = new FileClientService(context).saveImageToInternalStorage(attachedImage, contact.getUserId(), context, "image");
                        contact.setLocalImageUrl(imageLocalPath);
                        updateContact(contact);
                    } else {
                        Log.w(TAG, "Download is failed response code is ...." + connection.getResponseCode());
                    }
                }
                // Calculate inSampleSize
                options.inSampleSize = ImageUtils.calculateInSampleSize(options, 100, 50);

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


}

