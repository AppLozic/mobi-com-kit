package com.mobicomkit.contact;

import android.content.Context;
import android.graphics.Bitmap;

import net.mobitexter.mobiframework.people.contact.Contact;

import java.util.List;

/**
 * Created by adarsh on 7/7/15.
 */
public class DeviceContactService implements BaseContactService {

    @Override
    public void add(Contact contact) {

    }

    @Override
    public void addAll(List<Contact> contactList) {

    }

    @Override
    public void deleteContact(Contact contact) {

    }

    @Override
    public void deleteContactById(String contactId) {

    }

    @Override
    public List<Contact> getAll() {
        return null;
    }

    @Override
    public Contact getContactById(String contactId) {
        return null;
    }

    @Override
    public Contact getContactWithFallback(String contactId) {
        return null;
    }

    @Override
    public void updateContact(Contact contact) {

    }

    @Override
    public void upsert(Contact contact) {

    }

    @Override
    public Bitmap downloadContactImage(Context context, Contact contact) {
        return null;
    }

}
