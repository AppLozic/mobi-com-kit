package com.applozic.mobicommons.people.contact;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;

import com.applozic.mobicommons.commons.core.utils.Support;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.commons.image.ImageLoader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by devashish on 28/11/14.
 */
public class ContactUtils {

    public static final String UNKNOWN_NUMBER = "UNKNOWN";
    private static final String TAG = "ContactUtils";

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static Bitmap loadContactPhoto(Uri contactUri, int imageSize, Activity activity) {
        if (activity == null) {
            return null;
        }
        final ContentResolver contentResolver = activity.getContentResolver();
        try {
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(contentResolver, contactUri);

            if (input == null) {
                return null;
            }
            return BitmapFactory.decodeStream(input);
        } catch (Exception ex) {
            //Note:Catching exception, in some cases the code is throwing error.
        }

        AssetFileDescriptor afd = null;
        String displayPhoto = ContactsContract.Contacts.Photo.CONTENT_DIRECTORY;
        if (Utils.hasICS()) {
            displayPhoto = ContactsContract.Contacts.Photo.DISPLAY_PHOTO;
        }

        try {

            Uri imageUri = Uri.withAppendedPath(contactUri, displayPhoto);
            afd = activity.getContentResolver().openAssetFileDescriptor(imageUri, "r");
            if (afd != null) {
                return ImageLoader.decodeSampledBitmapFromDescriptor(
                        afd.getFileDescriptor(), imageSize, imageSize);
            }
        } catch (FileNotFoundException ex) {
            Log.e(TAG, "Image not found error " + ex.getMessage());
        } finally {
            if (afd != null) {
                try {
                    afd.close();
                } catch (IOException ex) {
                    Log.e(TAG, "Image not found error " + ex.getMessage());
                }
            }
        }
        return null;
    }

    public static void startContactAddActivity(FragmentActivity activity) {
        final Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
        ActivityInfo activityInfo = intent.resolveActivityInfo(activity.getPackageManager(), intent.getFlags());
        if (intent.resolveActivity(activity.getPackageManager()) != null && activityInfo.exported) {
            activity.startActivity(intent);
        }
    }

    public static String getContactId(String phoneNumber, ContentResolver cr) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return UNKNOWN_NUMBER;
        }

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor phoneCursor = cr.query(uri,
                new String[]{ContactsContract.PhoneLookup._ID}, null, null, null);
        try {
            if (phoneCursor != null && phoneCursor.moveToNext()) {
                return phoneCursor.getString(0);
            }
        } finally {
            //Note: It can come null for some specific numbers
            //Read more: http://stackoverflow.com/questions/13823097/android-querying-contacts-sometimes-returns-empty-cursor
            if (phoneCursor != null) {
                phoneCursor.close();
            }
        }
        // Returning the empty Value for the New PhoneNumbers
        return "";
    }

    public static Contact getContactByEmailId(Context context, String emailId) {
        Contact contact = new Contact();
        String contactNumber = emailId;
        String contactId = null;
        String displayName = "";

        Cursor cursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Email.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Email.DATA + "=?",
                new String[]{emailId}, null);
        try {
            while (cursor.moveToNext()) {
                contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID));
                contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (contactId != null) {
            contactNumber = getContactNumberByContactId(context, contactId);
            contact.setContactId(Long.parseLong(contactId));
        }
        contact.processFullName(displayName);
        contact.setContactNumber(contactNumber);

        return contact;
    }

    public static Map<String, String> getPhoneNumbers(Context context, long contactId) {
        final String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE,
        };

        Cursor phoneCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection,
                ContactsContract.Data.CONTACT_ID + "=?", new String[]{String.valueOf(contactId)}, null);
        Map<String, String> phoneNumbers = new LinkedHashMap<String, String>();
        try {
            while (phoneCursor.moveToNext()) {
                String type = (String) ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(), phoneCursor.getInt(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)), "");
                //Todo: Get label for custom label.
                phoneNumbers.put(phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)), type);
            }
        } finally {
            phoneCursor.close();
        }
        return phoneNumbers;
    }

    public static Long getContactId(ContentResolver contentResolver, Uri uriContact) {
        Long contactId = null;

        // getting contacts ID
        Cursor cursorId = contentResolver.query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorId.moveToFirst()) {
            contactId = cursorId.getLong(cursorId.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursorId.close();
        return contactId;
    }

    public static Contact getContact(Context context, String contactId, String number) {
        Contact contact = new Contact();
        contact.setContactNumber(number);
        Support support = new Support(context);
        if (support.isSupportNumber(number)) {
            return support.getSupportContact();
        } else if (TextUtils.isEmpty(contactId) || UNKNOWN_NUMBER.equals(contactId)) {
            contact.processContactNumbers(context);
            return contact;
        }
        contact.setContactId(Long.valueOf(contactId));

        String structuredNameWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] structuredNameWhereParams = new String[]{contactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE};
        Cursor cursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, structuredNameWhere, structuredNameWhereParams, null);
        try {
            if (cursor.moveToFirst()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String prefix = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.PREFIX));
                String fullName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME));
                String firstName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
                String middleName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME));
                String lastName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
                String suffix = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.SUFFIX));

                prefix = prefix == null ? "" : prefix;
                firstName = firstName == null ? "" : firstName;
                middleName = middleName == null ? "" : middleName;
                lastName = lastName == null ? "" : lastName;
                suffix = suffix == null ? "" : suffix;
                contact.setFirstName(TextUtils.isEmpty(prefix) ? firstName : (prefix + " " + firstName));
                contact.setMiddleName(middleName);
                contact.setLastName(TextUtils.isEmpty(suffix) ? lastName : (lastName + " " + suffix));
                //contact.setContactId(Long.valueOf(id));
                contact.setFullName(fullName);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        contact.processContactNumbers(context);
        return contact;
    }

    public static Contact getContact(Context context, String number) {
        if (TextUtils.isEmpty(number)) {
            return new Contact();
        }

        String contactId = "";
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        Cursor phoneCursor = context.getContentResolver().query(uri,
                new String[]{ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER}, null, null, null);
        try {
            if (phoneCursor != null && phoneCursor.moveToNext()) {
                contactId = phoneCursor.getString(0);
                number = phoneCursor.getString(1);
            }
        } finally {
            if (phoneCursor != null) {
                phoneCursor.close();
            }
        }

        return getContact(context, contactId, number);
    }

    public static Contact getContact(Context context, Long contactId) {
        String number = getContactNumberByContactId(context, String.valueOf(contactId));
        return getContact(context, String.valueOf(contactId), number);
    }

    public static String getContactNumberByContactId(Context context, String contactId) {
        String contactNumber = null;
        Cursor phoneCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER}, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "="
                        + contactId, null, ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY
                        + " DESC");
        try {
            if (phoneCursor.getCount() > 0) {
                phoneCursor.moveToNext();
                contactNumber = phoneCursor.getString(0);
            }
        } finally {
            phoneCursor.close();
        }
        return contactNumber;
    }

    public static List<Contact> getContacts(ContentResolver cr, Context ctx) {
        String[] PROJECTION = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME};
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, PROJECTION, null, null,
                null);
        List<Contact> contactList = new ArrayList<Contact>();

        try {
            while (cursor.moveToNext()) {
                try {
                    String fullName = cursor.getString(1);
                    List<String> phoneList = new ArrayList<String>();
                    List<String> emailList = new ArrayList<String>();
                    // getEmails
                    Cursor emailCursor;
                    Long contactId = cursor.getLong(0);
                    emailCursor = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            new String[]{ContactsContract.CommonDataKinds.Email.DATA1}, ContactsContract.CommonDataKinds.Email.CONTACT_ID + "="
                                    + contactId, null, null);
                    try {
                        int colIndex = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA1);
                        while (emailCursor.moveToNext()) {
                            emailList.add(emailCursor.getString(colIndex));
                        }
                    } finally {
                        emailCursor.close();
                    }

                    Cursor phoneCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER}, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "="
                                    + contactId, null, ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY
                                    + " DESC");
                    try {
                        while (phoneCursor.moveToNext()) {
                            phoneList.add(phoneCursor.getString(0));
                        }
                    } finally {
                        phoneCursor.close();
                    }
                    // only adding to contactlist if it has the phone number
                    if (phoneList.size() > 0) {
                        Contact contact = new Contact(fullName, emailList, phoneList, contactId);
                        contact.processContactNumbers(ctx);
                        contactList.add(contact);
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "Exception while fetching contacts", ex);
                }
            }

        } finally {
            cursor.close();
        }

        return contactList;
    }
}
