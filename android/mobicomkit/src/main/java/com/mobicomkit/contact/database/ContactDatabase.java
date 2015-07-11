package com.mobicomkit.contact.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mobicomkit.api.account.user.MobiComUserPreference;
import com.mobicomkit.database.MobiComDatabaseHelper;

import net.mobitexter.mobiframework.people.contact.Contact;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adarsh on 9/7/15.
 */
public class ContactDatabase {

    private static final String TAG = "ContactDatabaseService";


    public static final String CONTACT = "contact";
    Context context = null;
    private MobiComUserPreference userPreferences;
    private MobiComDatabaseHelper dbHelper;

    public ContactDatabase(Context context) {
        this.context = context;
        this.userPreferences = MobiComUserPreference.getInstance(context);
        this.dbHelper = MobiComDatabaseHelper.getInstance(context);
    }

    /**
     * Form a single contact from cursor
     *
     * @param cursor
     * @return
     */
    public Contact getContact(Cursor cursor) {
        Contact contact = null;
        if (cursor.moveToNext() && cursor.getCount() > 0) {
            contact = new Contact();
            contact.setFullName(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.FULL_NAME)));
            contact.setUserId(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.USERID)));
            contact.setLocalImageUrl(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.CONTACT_IMAGE_LOCAL_URI)));
            contact.setImageURL(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.CONTACT_IMAGE_URL)));
            contact.setContactNumber(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.CONTACT_NO)));
        }
        return contact;
    }

    /**
     * Form a single contact details from cursor
     *
     * @param cursor
     * @return
     */
    public List<Contact> getContactList(Cursor cursor) {

        List<Contact> smsList = new ArrayList<Contact>();
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            do {
                smsList.add(getContact(cursor));
            } while (cursor.moveToNext());
        }
        return smsList;
    }

    public List<Contact> getAllContact() {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(CONTACT, null, null, null, null, null, MobiComDatabaseHelper.FULL_NAME + " asc");
        List<Contact> contactList = getContactList(cursor);
        cursor.close();
        dbHelper.close();
        return contactList;
    }

    public Contact getContactById(String id) {

        String structuredNameWhere = MobiComDatabaseHelper.USERID + " =?";
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(CONTACT, null, structuredNameWhere, new String[]{id}, null, null, null);
        Contact contact = getContact(cursor);
        cursor.close();
        dbHelper.close();
        return contact;

    }

    public void updateContact(Contact contact) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = prepareContactValues(contact);
        dbHelper.getWritableDatabase().update(CONTACT, contentValues, MobiComDatabaseHelper.USERID + "=?", new String[]{contact.getUserId()});
        dbHelper.close();
    }

    public void addContact(Contact contact) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = prepareContactValues(contact);
        //We need to decide
        dbHelper.getWritableDatabase().insert(CONTACT, null, contentValues);
        dbHelper.close();
    }

    public ContentValues prepareContactValues(Contact contact) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(MobiComDatabaseHelper.FULL_NAME, contact.getFullName());
        contentValues.put(MobiComDatabaseHelper.CONTACT_NO, contact.getContactNumber());
        contentValues.put(MobiComDatabaseHelper.CONTACT_IMAGE_URL, contact.getImageURL());
        contentValues.put(MobiComDatabaseHelper.CONTACT_IMAGE_LOCAL_URI, contact.getLocalImageUrl());
        contentValues.put(MobiComDatabaseHelper.USERID, contact.getUserId());
        contentValues.put(MobiComDatabaseHelper.EMAIL, contact.getEmailId());
        return contentValues;

    }

    public void addAllContact(List<Contact> contactList) {
        for (Contact contact : contactList) {
            addContact(contact);
        }
    }

    public void deleteContact(Contact contact) {
        deleteContactById(contact.getUserId());
    }

    public void deleteContactById(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(CONTACT, "userId=?", new String[]{id});
        dbHelper.close();
    }

    public void deleteAllContact(List<Contact> contacts) {
        for (Contact contact : contacts) {
            deleteContact(contact);
        }
    }


}


