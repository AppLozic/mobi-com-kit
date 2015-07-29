package com.applozic.mobicomkit.contact;

import android.content.Context;
import android.graphics.Bitmap;

import com.applozic.mobicommons.people.contact.Contact;
import com.applozic.mobicommons.people.contact.ContactUtils;

import java.util.List;

/**
 * Created by adarsh on 7/7/15.
 */
public class DeviceContactService implements BaseContactService {

    private Context context;

    public DeviceContactService(Context context) {
        this.context = context;
    }

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
        Contact contact = ContactUtils.getContact(context, contactId);
        if (contact != null) {
            contact.processContactNumbers(context);
        }
        return contact;
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

    @Override
    public Contact getContactReceiver(List<String> items, List<String> userIds) {
        if (items != null && !items.isEmpty()) {
            return ContactUtils.getContact(context, items.get(0));
        }

        return null;
    }

    @Override
    public boolean isContactExists(String contactId) {
        //Todo: write implementation for device contacts
        return false;
    }

}
