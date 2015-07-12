package com.mobicomkit.contact;

import android.content.Context;
import android.graphics.Bitmap;

import net.mobitexter.mobiframework.people.contact.Contact;

import java.util.List;

/**
 * Created by adarsh on 7/7/15.
 */
public interface BaseContactService {

    void add(Contact contact);

    void addAll(List<Contact> contactList);

    void deleteContact(Contact contact);

    void deleteContactById(String contactId);

    List<Contact> getAll();

    Contact getContactById(String contactId);

    Contact getContactWithFallback(String contactId);

    void updateContact(Contact contact);

    void upsert(Contact contact);

    Bitmap downloadContactImage(Context context, Contact contact);

}
