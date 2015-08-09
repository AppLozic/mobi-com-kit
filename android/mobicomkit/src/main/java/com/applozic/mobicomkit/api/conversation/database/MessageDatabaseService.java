package com.applozic.mobicomkit.api.conversation.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.attachment.FileMeta;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.database.MobiComDatabaseHelper;

import com.applozic.mobicommons.commons.core.utils.DBUtils;
import com.applozic.mobicommons.people.contact.Contact;
import com.applozic.mobicommons.people.group.Group;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Manish
 * Date: 6/9/12
 * Time: 8:40 PM
 */
public class MessageDatabaseService {

    private static final String TAG = "MessageDatabaseService";

    private static final String MIN_CREATED_AT_KEY = "mck.sms.createdAt.min";
    private static final String MAX_CREATED_AT_KEY = "mck.sms.createdAt.max";
    public static List<Message> recentlyAddedMessage = new ArrayList<Message>();
    Context context = null;
    private MobiComUserPreference userPreferences;
    private MobiComDatabaseHelper dbHelper;

    public MessageDatabaseService(Context context) {
        this.context = context;
        this.userPreferences = MobiComUserPreference.getInstance(context);
        this.dbHelper = MobiComDatabaseHelper.getInstance(context);
    }

    public static Message getMessage(Cursor cursor) {
        Message message = new Message();
        message.setMessageId(cursor.getLong(cursor.getColumnIndex("id")));
        message.setKeyString(cursor.getString(cursor.getColumnIndex("keyString")));
        message.setType(cursor.getShort(cursor.getColumnIndex("type")));
        message.setSource(cursor.getShort(cursor.getColumnIndex("source")));
        Long storeOnDevice = cursor.getLong(cursor.getColumnIndex("storeOnDevice"));
        message.setStoreOnDevice(storeOnDevice != null && storeOnDevice.intValue() == 1);
        String contactNumbers = cursor.getString(cursor.getColumnIndex("contactNumbers"));
        message.setContactIds(contactNumbers);
        message.setCreatedAtTime(cursor.getLong(cursor.getColumnIndex("createdAt")));
        Long delivered = cursor.getLong(cursor.getColumnIndex("delivered"));
        message.setDelivered(delivered != null && delivered.intValue() == 1);

        Long canceled = cursor.getLong(cursor.getColumnIndex("canceled"));
        message.setCanceled(canceled != null && canceled.intValue() == 1);

        Long read = cursor.getLong(cursor.getColumnIndex("read"));
        message.setRead(read != null && read.intValue() == 1);

        Long scheduledAt = cursor.getLong(cursor.getColumnIndex("scheduledAt"));
        message.setScheduledAt(scheduledAt == null || scheduledAt.intValue() == 0 ? null : scheduledAt);
        message.setMessage(cursor.getString(cursor.getColumnIndex("message")));
        Long sentToServer = cursor.getLong(cursor.getColumnIndex("sentToServer"));
        message.setSentToServer(sentToServer != null && sentToServer.intValue() == 1);
        message.setTo(cursor.getString(cursor.getColumnIndex("toNumbers")));
        int timeToLive = cursor.getInt(cursor.getColumnIndex("timeToLive"));
        message.setTimeToLive(timeToLive != 0 ? timeToLive : null);
        String fileMetaKeyStrings = cursor.getString(cursor.getColumnIndex("fileMetaKeyStrings"));
        if (!TextUtils.isEmpty(fileMetaKeyStrings)) {
            message.setFileMetaKeyStrings(Arrays.asList(fileMetaKeyStrings.split(",")));
        }
        String filePaths = cursor.getString(cursor.getColumnIndex("filePaths"));
        if (!TextUtils.isEmpty(filePaths)) {
            message.setFilePaths(Arrays.asList(filePaths.split(",")));
        }
        long broadcastGroupId = cursor.getLong(cursor.getColumnIndex("timeToLive"));
        message.setBroadcastGroupId(broadcastGroupId != 0 ? broadcastGroupId : null);
        if (cursor.getString(cursor.getColumnIndex("metaFileKeyString")) == null) {
            //file is not present...  Don't set anything ...
        } else {
            FileMeta fileMeta = new FileMeta();
            fileMeta.setKeyString(cursor.getString(cursor.getColumnIndex("metaFileKeyString")));
            fileMeta.setBlobKeyString(cursor.getString(cursor.getColumnIndex("blobKeyString")));
            fileMeta.setThumbnailUrl(cursor.getString(cursor.getColumnIndex("thumbnailUrl")));
            fileMeta.setSize(cursor.getInt(cursor.getColumnIndex("size")));
            fileMeta.setName(cursor.getString(cursor.getColumnIndex("name")));
            fileMeta.setContentType(cursor.getString(cursor.getColumnIndex("contentType")));
            List<FileMeta> list = new ArrayList<FileMeta>();
            list.add(fileMeta);
            message.setFileMetas(list);
        }

        return message;
    }

    public static List<Message> getMessageList(Cursor cursor) {
        List<Message> smsList = new ArrayList<Message>();
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            do {
                smsList.add(getMessage(cursor));
            } while (cursor.moveToNext());
        }
        return smsList;
    }

    public List<Message> getMessages(Long startTime, Long endTime, Contact contact, Group group) {
        String structuredNameWhere = "";
        List<String> structuredNameParamsList = new ArrayList<String>();

        if (group != null && group.getGroupId() != null) {
            structuredNameWhere += "broadcastGroupId = ? AND ";
            structuredNameParamsList.add(String.valueOf(group.getGroupId()));
        }
        if (contact != null && !TextUtils.isEmpty(contact.getContactIds())) {
            structuredNameWhere += "contactNumbers = ? AND ";
            structuredNameParamsList.add(contact.getContactIds());
        }
        if (startTime != null) {
            structuredNameWhere += "createdAt >= ? AND ";
            structuredNameParamsList.add(String.valueOf(startTime));
        }
        if (endTime != null) {
            structuredNameWhere += "createdAt < ? AND ";
            structuredNameParamsList.add(String.valueOf(endTime));
        }

        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);
        if (!userPreferences.isDisplayCallRecordEnable()) {
            structuredNameWhere += "type != ? AND type != ? AND ";
            structuredNameParamsList.add(String.valueOf(Message.MessageType.CALL_INCOMING.getValue()));
            structuredNameParamsList.add(String.valueOf(Message.MessageType.CALL_OUTGOING.getValue()));
        }

        if (!TextUtils.isEmpty(structuredNameWhere)) {
            structuredNameWhere = structuredNameWhere.substring(0, structuredNameWhere.length() - 5);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query("sms", null, structuredNameWhere, structuredNameParamsList.toArray(new String[structuredNameParamsList.size()]), null, null, "createdAt asc");
        List<Message> messageList = MessageDatabaseService.getMessageList(cursor);
        cursor.close();
        dbHelper.close();
        return messageList;
    }

    public List<Message> getPendingMessages() {
        String structuredNameWhere = "";
        List<String> structuredNameParamsList = new ArrayList<String>();
        structuredNameWhere += "sentToServer = ? and canceled = ? ";
        structuredNameParamsList.add("0");
        structuredNameParamsList.add("0");
        Cursor cursor = dbHelper.getWritableDatabase().query("sms", null, structuredNameWhere, structuredNameParamsList.toArray(new String[structuredNameParamsList.size()]), null, null, "createdAt asc");
        List<Message> messageList = getMessageList(cursor);
        cursor.close();
        dbHelper.close();
        return messageList;
    }

    public long getMinCreatedAtFromMessageTable() {
        SQLiteDatabase db = dbHelper.getInstance(context).getWritableDatabase();
        final Cursor cursor = db.rawQuery("select min(createdAt) as createdAt from sms", null);
        long createdAt = 0;
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            createdAt = cursor.getLong(0);
        }
        cursor.close();
        dbHelper.close();
        return createdAt;
    }

    public Message getMessage(String contactNumber, String message) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String structuredNameWhere = "";
        List<String> structuredNameParamsList = new ArrayList<String>();

        structuredNameWhere += "contactNumbers = ? AND message = ?";
        structuredNameParamsList.add(contactNumber);
        structuredNameParamsList.add(message);

        Cursor cursor = db.query("sms", null, structuredNameWhere, structuredNameParamsList.toArray(new String[structuredNameParamsList.size()]), null, null, null);

        Message message1 = null;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            message1 = getMessage(cursor);
        }

        cursor.close();
        dbHelper.close();
        return message1;
    }

    public Message getMessage(String keyString) {
        if (TextUtils.isEmpty(keyString)) {
            return null;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String structuredNameWhere = "";
        List<String> structuredNameParamsList = new ArrayList<String>();

        structuredNameWhere += "keyString = ?";
        structuredNameParamsList.add(keyString);

        Cursor cursor = db.query("sms", null, structuredNameWhere, structuredNameParamsList.toArray(new String[structuredNameParamsList.size()]), null, null, null);

        Message message = null;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            message = getMessage(cursor);
        }

        cursor.close();
        dbHelper.close();
        return message;
    }

    public List<Message> getScheduledMessages(Long time) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (!DBUtils.isTableExists(db, MobiComDatabaseHelper.SCHEDULE_SMS_TABLE_NAME)) {
            dbHelper.close();
            return new ArrayList<Message>();
        }

        List<Message> messages = new ArrayList<Message>();
        Cursor cursor;
        if (time != null) {
            cursor = db.query(MobiComDatabaseHelper.SCHEDULE_SMS_TABLE_NAME, null, MobiComDatabaseHelper.TIMESTAMP + " <= ?", new String[]{
                    time + ""}, null, null, null);
        } else {
            cursor = db.query(MobiComDatabaseHelper.SCHEDULE_SMS_TABLE_NAME, null, null, null, null, null, null);
        }

        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            do {
                String createdTime = cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.TIMESTAMP));
                //SMS Creation From DB......
                Message message = new Message();
                message.setCreatedAtTime(Long.valueOf(createdTime));

                message.setScheduledAt(cursor.getLong(cursor.getColumnIndex(MobiComDatabaseHelper.TIMESTAMP)));

                message.setMessage(cursor
                        .getString(cursor.getColumnIndex(MobiComDatabaseHelper.SMS)));
                message.setType(cursor
                        .getShort(cursor.getColumnIndex(MobiComDatabaseHelper.SMS_TYPE)));
                message.setSource(cursor.getShort(cursor.getColumnIndex("source")));
                message.setContactIds(cursor
                        .getString(cursor.getColumnIndex(MobiComDatabaseHelper.CONTACTID)));
                message.setTo(cursor
                        .getString(cursor.getColumnIndex(MobiComDatabaseHelper.TO_FIELD)));
                message.setKeyString(cursor
                        .getString(cursor.getColumnIndex(MobiComDatabaseHelper.SMS_KEY_STRING)));
                message.setStoreOnDevice("1".equals(cursor.getString(cursor.getColumnIndex(MobiComDatabaseHelper.STORE_ON_DEVICE_COLUMN))));

                if (cursor.getColumnIndex(MobiComDatabaseHelper.TIME_TO_LIVE) != -1) {
                    int timeToLive = cursor.getInt(cursor.getColumnIndex(MobiComDatabaseHelper.TIME_TO_LIVE));
                    message.setTimeToLive(timeToLive == 0 ? null : timeToLive);
                }

                messages.add(message);
            } while (cursor.moveToNext());
        }
        cursor.close();
        dbHelper.close();
        return messages;
    }

    public void deleteScheduledMessages(long time) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(MobiComDatabaseHelper.SCHEDULE_SMS_TABLE_NAME, MobiComDatabaseHelper.TIMESTAMP + " <= ? ", new String[]{time + ""});
        dbHelper.close();
    }

    public boolean deleteScheduledMessage(String messageKeyString) {
        SQLiteDatabase db = dbHelper.getInstance(context).getWritableDatabase();
        boolean deleted = db.delete(MobiComDatabaseHelper.SCHEDULE_SMS_TABLE_NAME, MobiComDatabaseHelper.SMS_KEY_STRING + "='" + messageKeyString + "'", null) > 0;
        dbHelper.close();
        return deleted;
    }

    public boolean isMessageTableEmpty() {
        dbHelper = MobiComDatabaseHelper.getInstance(context);
        boolean empty = DBUtils.isTableEmpty(dbHelper.getWritableDatabase(), "sms");
        dbHelper.close();
        return empty;
    }

    public synchronized void updateMessageFileMetas(long messageId, final Message message) {
        ContentValues values = new ContentValues();
        values.put("keyString", message.getKeyString());
        if (message.getFileMetaKeyStrings() != null && !message.getFileMetaKeyStrings().isEmpty()) {
            values.put("fileMetaKeyStrings", TextUtils.join(",", message.getFileMetaKeyStrings()));
        }
        if (message.getFileMetas() != null && !message.getFileMetas().isEmpty()) {
            FileMeta fileMeta = message.getFileMetas().get(0);
            if (fileMeta != null) {
                values.put("thumbnailUrl", fileMeta.getThumbnailUrl());
                values.put("size", fileMeta.getSize());
                values.put("name", fileMeta.getName());
                values.put("contentType", fileMeta.getContentType());
                values.put("metaFileKeyString", fileMeta.getKeyString());
                values.put("blobKeyString", fileMeta.getBlobKeyString());
            }
        }
        dbHelper.getWritableDatabase().update("sms", values, "id=" + messageId, null);
        dbHelper.close();
    }

    public synchronized long createMessage(final Message message) {
        long id = -1;
        if (recentlyAddedMessage.contains(message)) {
            return -1;
        }
        if (message.hasAttachment() && message.getMessageId() != null) {
            if (!recentlyAddedMessage.contains(message))
                recentlyAddedMessage.add(message);
            return message.getMessageId();
        }
        id = createSingleMessage(message);
        message.setMessageId(id);
        if (message.isSentToMany()) {
            String[] toList = message.getTo().trim().replace("undefined,", "").split(",");
            for (String tofield : toList) {
                Message singleMessage = new Message(message);
                singleMessage.setKeyString(message.getKeyString());
                singleMessage.setBroadcastGroupId(null);
                singleMessage.setTo(tofield);
                singleMessage.processContactIds(context);
                singleMessage.setMessageId(createSingleMessage(singleMessage));
            }
        }

        recentlyAddedMessage.add(message);
        if (recentlyAddedMessage.size() > 20) {
            recentlyAddedMessage.subList(0, 10).clear();
        }
        return id;
    }

    public synchronized long createSingleMessage(final Message message) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        long id = -1;
        boolean duplicateCheck = true;
        SharedPreferences prefs = context.getSharedPreferences(MobiComKitClientService.getApplicationKey(context), Context.MODE_PRIVATE);
        long minCreatedAt = prefs.getLong(MIN_CREATED_AT_KEY, 0);
        long maxCreatedAt = prefs.getLong(MAX_CREATED_AT_KEY, Long.MAX_VALUE);

        if (message.getCreatedAtTime() < minCreatedAt) {
            duplicateCheck = false;
            prefs.edit().putLong(MIN_CREATED_AT_KEY, message.getCreatedAtTime()).commit();
        }
        if (message.getCreatedAtTime() > maxCreatedAt) {
            duplicateCheck = false;
            prefs.edit().putLong(MAX_CREATED_AT_KEY, message.getCreatedAtTime()).commit();
        }

        if (duplicateCheck) {
            Cursor cursor;
            //Todo: add broadcastGroupId in the query if group is not null
            if (message.isSentToServer() && !TextUtils.isEmpty(message.getKeyString())) {
                cursor = database.rawQuery(
                        "SELECT COUNT(*) FROM sms WHERE keyString = ? and contactNumbers = ?",
                        new String[]{message.getKeyString(), message.getContactIds()});
            } else {
                cursor = database.rawQuery(
                        "SELECT COUNT(*) FROM sms WHERE sentToServer=0 and contactNumbers = ? and message = ? and createdAt between " + (message.getCreatedAtTime() - 120000) + " and " + (message.getCreatedAtTime() + 120000),
                        new String[]{message.getContactIds(), message.getMessage()});
            }

            cursor.moveToFirst();

            if (cursor.getInt(0) > 0) {
                cursor.close();
                dbHelper.close();
                return -1;
            }
        }

        try {
            ContentValues values = new ContentValues();
            values.put("toNumbers", message.getTo());
            values.put("message", message.getMessage());
            values.put("createdAt", message.getCreatedAtTime());
            values.put("storeOnDevice", message.isStoreOnDevice());
            values.put("delivered", message.getDelivered());
            values.put("scheduledAt", message.getScheduledAt());
            values.put("type", message.getType());
            values.put("contactNumbers", message.getContactIds());
            values.put("sentToServer", message.isSentToServer());
            values.put("keyString", message.getKeyString());
            values.put("source", message.getSource());
            values.put("timeToLive", message.getTimeToLive());
            values.put("broadcastGroupId", message.getBroadcastGroupId());
            values.put("canceled", message.isCanceled());
            values.put("read", message.isRead() ? 1 : 0);

            if (message.getFileMetaKeyStrings() != null && !message.getFileMetaKeyStrings().isEmpty()) {
                values.put("fileMetaKeyStrings", TextUtils.join(",", message.getFileMetaKeyStrings()));
            }
            if (message.getFilePaths() != null && !message.getFilePaths().isEmpty()) {
                values.put("filePaths", TextUtils.join(",", message.getFilePaths()));
            }
            //TODO:Right now we are supporting single image attachment...making entry in same table
            if (message.getFileMetas() != null && !message.getFileMetas().isEmpty()) {
                FileMeta fileMeta = message.getFileMetas().get(0);
                if (fileMeta != null) {
                    values.put("thumbnailUrl", fileMeta.getThumbnailUrl());
                    values.put("size", fileMeta.getSize());
                    values.put("name", fileMeta.getName());
                    values.put("contentType", fileMeta.getContentType());
                    values.put("metaFileKeyString", fileMeta.getKeyString());
                    values.put("blobKeyString", fileMeta.getBlobKeyString());
                }
            }
            id = database.insert("sms", null, values);
        } catch (SQLiteConstraintException ex) {
            Log.e(TAG, "Duplicate entry in sms table, sms: " + message);
        } finally {
            dbHelper.close();
        }

        return id;
    }

    public void updateSmsType(String smsKeyString, Message.MessageType messageType) {
        ContentValues values = new ContentValues();
        values.put("type", messageType.getValue());
        dbHelper.getWritableDatabase().update("sms", values, "keyString='" + smsKeyString + "'", null);
        dbHelper.close();
    }

    public void updateMessageDeliveryReport(String messageKeyString, String contactNumber) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("delivered", "1");
        if (TextUtils.isEmpty(contactNumber)) {
            database.update("sms", values, "keyString='" + messageKeyString + "'", null);
        } else {
            database.update("sms", values, "keyString='" + messageKeyString + "' and contactNumbers='" + contactNumber + "'", null);
        }
        dbHelper.close();
    }

    public void updateMessageSyncStatus(Message message, String keyString) {
        try {
            ContentValues values = new ContentValues();
            values.put("keyString", keyString);
            values.put("sentToServer", "1");
            dbHelper.getWritableDatabase().update("sms", values, "id=" + message.getMessageId(), null);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            dbHelper.close();
        }
    }

    public void updateInternalFilePath(String keyString, String filePath) {
        ContentValues values = new ContentValues();
        values.put("filePaths", filePath);
        dbHelper.getWritableDatabase().update("sms", values, "keyString='" + keyString + "'", null);
        dbHelper.close();

    }

    public void updateCanceledFlag(long smsId, int value) {
        ContentValues values = new ContentValues();
        values.put("canceled", value);
        dbHelper.getWritableDatabase().update("sms", values, "id=" + smsId, null);
        dbHelper.close();
    }

    public void updateSmsReadFlag(long smsId, boolean read) {
        ContentValues values = new ContentValues();
        values.put("read", read ? 1 : 0);
        dbHelper.getWritableDatabase().update("sms", values, "id=" + smsId, null);
        dbHelper.close();
    }

    public int getUnreadSmsCount(Contact contact) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        final Cursor cursor = db.rawQuery("SELECT COUNT(read) FROM sms WHERE read = 0 AND contactNumbers = " + "'" + contact.getContactNumber() + "'", null);
        cursor.moveToFirst();
        int unreadSms = 0;
        if (cursor.getCount() > 0) {
            unreadSms = cursor.getInt(0);
        }
        cursor.close();
        dbHelper.close();
        return unreadSms;
    }

    public List<Message> getLatestMessage(String contactNumbers) {
        List<Message> messages = new ArrayList<Message>();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from sms where contactNumbers = " + "'" + contactNumbers + "'" + " order by createdAt desc limit 1", null);
        if (cursor.moveToFirst()) {
            messages = MessageDatabaseService.getMessageList(cursor);
        }
        cursor.close();
        dbHelper.close();
        return messages;
    }

    public List<Message> getMessages(Long createdAt) {
        String createdAtClause = "";
        if (createdAt != null && createdAt > 0) {
            createdAtClause = " and m1.createdAt < " + createdAt;
        }

        String messageTypeClause = "";
        String messageTypeJoinClause = "";
        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);
        if (!userPreferences.isDisplayCallRecordEnable()) {
            messageTypeClause = " and m1.type != " + Message.MessageType.CALL_INCOMING.getValue() + " and m1.type != " + Message.MessageType.CALL_OUTGOING.getValue();
            messageTypeJoinClause = " and m1.type = m2.type";
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        /*final Cursor cursor = db.rawQuery("select * from sms where createdAt in " +
                "(select max(createdAt) from sms group by contactNumbers) order by createdAt desc", null);*/
        final Cursor cursor = db.rawQuery("select m1.* from sms m1 left outer join sms m2 on (m1.createdAt < m2.createdAt"
                + " and m1.contactNumbers like m2.contactNumbers " + messageTypeJoinClause + " ) where m2.createdAt is null " + createdAtClause + messageTypeClause
                + " order by m1.createdAt desc", null);

        /*final Cursor cursor = db.rawQuery("SELECT t1.* FROM sms t1" +
                "  JOIN (SELECT contactNumbers, MAX(createdAt) createdAt FROM sms GROUP BY contactNumbers) t2" +
                "  ON t1.contactNumbers = t2.contactNumbers AND t1.createdAt = t2.createdAt order by createdAt desc", null);*/
        List<Message> messageList = getMessageList(cursor);
        cursor.close();
        dbHelper.close();
        return messageList;
    }

    public String deleteMessage(Message message, String contactNumber) {
        String contactNumbers = contactNumber;
        String contactNumberClause = TextUtils.isEmpty(contactNumber) ? "" : " and contactNumbers='" + contactNumber + "'";
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = database.rawQuery("select contactNumbers from sms where keyString=" + "'" + message.getKeyString() + "'"
                + contactNumberClause, null);
        try {
            if (cursor.moveToFirst()) {
                contactNumbers = cursor.getString(cursor.getColumnIndex("contactNumbers"));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        database.delete("sms", "keyString" + "='" + message.getKeyString() + "'" + contactNumberClause, null);
        dbHelper.close();
        return contactNumbers;
    }

    public void deleteConversation(String contactNumber) {
        Log.i(TAG, "Deleting conversation for contactNumber: " + contactNumber);
        int deletedRows = dbHelper.getWritableDatabase().delete("sms", "contactNumbers=?", new String[]{contactNumber});
        dbHelper.close();
        Log.i(TAG, "Delete " + deletedRows + " messages.");
    }
}