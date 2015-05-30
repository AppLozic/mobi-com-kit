package com.mobicomkit.broadcast;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mobicomkit.api.MobiComKitConstants;
import com.mobicomkit.api.conversation.Message;

import net.mobitexter.mobiframework.json.GsonUtils;

/**
 * Created by devashish on 24/1/15.
 */
public class BroadcastService {

    private static final String TAG = "BroadcastService";

    public static String currentUserId = null;

    public static void sendFirstTimeSyncCompletedBroadcast(Context context) {
        Log.i(TAG, "Sending " + INTENT_ACTIONS.FIRST_TIME_SYNC_COMPLETE.toString() + " broadcast");
        Intent intent = new Intent();
        intent.setAction(INTENT_ACTIONS.FIRST_TIME_SYNC_COMPLETE.toString());
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        context.sendBroadcast(intent);
    }

    public static void sendLoadMoreBroadcast(Context context, boolean loadMore) {
        Log.i(TAG, "Sending " + INTENT_ACTIONS.LOAD_MORE.toString() + " broadcast");
        Intent intent = new Intent();
        intent.setAction(INTENT_ACTIONS.LOAD_MORE.toString());
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("loadMore", loadMore);
        context.sendBroadcast(intent);
    }

    public static void sendMessageUpdateBroadcast(Context context, String action, Message message) {
        Log.i(TAG, "Sending message update broadcast for " + action + ", " + message.getKeyString());
        Intent intentUpdate = new Intent();
        intentUpdate.setAction(action);
        intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
        intentUpdate.putExtra(MobiComKitConstants.MESSAGE_JSON_INTENT, GsonUtils.getJsonFromObject(message, Message.class));
        context.sendBroadcast(intentUpdate);
    }

    public static void sendMessageDeleteBroadcast(Context context, String action, String keyString, String contactNumbers) {
        Log.i(TAG, "Sending message delete broadcast for " + action);
        Intent intentDelete = new Intent();
        intentDelete.setAction(action);
        intentDelete.putExtra("keyString", keyString);
        intentDelete.putExtra("contactNumbers", contactNumbers);
        intentDelete.addCategory(Intent.CATEGORY_DEFAULT);
        context.sendBroadcast(intentDelete);
    }

    public static void sendConversationDeleteBroadcast(Context context, String action, String contactNumber) {
        Log.i(TAG, "Sending conversation delete broadcast for " + action);
        Intent intentDelete = new Intent();
        intentDelete.setAction(action);
        intentDelete.putExtra("contactNumber", contactNumber);
        intentDelete.addCategory(Intent.CATEGORY_DEFAULT);
        context.sendBroadcast(intentDelete);
    }

    public static void sendNumberVerifiedBroadcast(Context context, String action) {
        Log.i(TAG, "Sending number verified broadcast");
        Intent intentUpdate = new Intent();
        intentUpdate.setAction(action);
        intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
        context.sendBroadcast(intentUpdate);
    }


    public static void sendNotificationBroadcast(Context context, Message message) {
        Log.i(TAG, "Sending notification broadcast....");
        Intent notificationIntent = new Intent();
        notificationIntent.putExtra(MobiComKitConstants.MESSAGE_JSON_INTENT, GsonUtils.getJsonFromObject(message, Message.class));
        notificationIntent.setAction("com.mobicomkit.notification");
        context.sendBroadcast(notificationIntent);
    }

    public static enum INTENT_ACTIONS {
        LOAD_MORE, FIRST_TIME_SYNC_COMPLETE, MESSAGE_SYNC_ACK_FROM_SERVER,
        SYNC_MESSAGE, DELETE_MESSAGE, DELETE_CONVERSATION, MESSAGE_DELIVERY, INSTRUCTION,
        UPLOAD_ATTACHMENT_FAILED, MESSAGE_ATTACHMENT_DOWNLOAD_DONE, SMS_ATTACHMENT_DOWNLOAD_FAILD,
        CONTACT_VERIFIED, NOTIFY_USER
    }
}
