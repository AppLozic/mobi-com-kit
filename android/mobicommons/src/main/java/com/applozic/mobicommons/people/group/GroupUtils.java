package com.applozic.mobicommons.people.group;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.applozic.mobicommons.people.contact.ContactUtils;

/**
 * Created by devashish on 17/12/14.
 */
public class GroupUtils {

    public static Group fetchGroup(Context context, Long groupId, String groupName) {
        Group group = new Group(groupId, groupName);

        String where = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "=" + groupId
                + " AND "
                + ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE + "='"
                + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'";
        String[] projection = new String[]{ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID, ContactsContract.Data.DISPLAY_NAME};
        Cursor cursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, projection, where, null,
                ContactsContract.Data.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
        while (cursor.moveToNext()) {
            group.getContacts().add(ContactUtils.getContact(context, cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID))));
        }
        cursor.close();

        return group;
    }

    public static Group fetchGroup(Context context, Long groupId) {
        String groupName = "";
        //Todo: fetch group name

        return fetchGroup(context, groupId, groupName);
    }
}
