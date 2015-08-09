package com.applozic.mobicomkit.api.notification;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.applozic.mobicomkit.api.conversation.MessageIntentService;
import com.applozic.mobicomkit.api.conversation.MobiComConversationService;
import com.applozic.mobicomkit.api.conversation.MobiComMessageService;
import com.applozic.mobicomkit.api.people.ContactContent;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.contact.ContactService;

import java.util.ArrayList;
import java.util.List;


public class MobiComPushReceiver {

    public static final String MTCOM_PREFIX = "MT_";
    public static final List<String> notificationKeyList = new ArrayList<String>();

    static {
        notificationKeyList.add("MT_SYNC"); // 0
        notificationKeyList.add("MT_MARK_ALL_MESSAGE_AS_READ"); //1
        notificationKeyList.add("MT_DELIVERED"); //2
        notificationKeyList.add("MT_SYNC_PENDING"); //3
        notificationKeyList.add("MT_DELETE_MESSAGE"); //4
        notificationKeyList.add("MT_DELETE_MULTIPLE_MESSAGE"); //5
        notificationKeyList.add("MT_DELETE_MESSAGE_CONTACT");// 6
        notificationKeyList.add("MTEXTER_USER");//7
        notificationKeyList.add("MT_CONTACT_VERIFIED"); //8
        notificationKeyList.add("MT_CONTACT_UPDATED"); //9
        notificationKeyList.add("MT_DEVICE_CONTACT_SYNC");//10
        notificationKeyList.add("MT_EMAIL_VERIFIED");//11
        notificationKeyList.add("MT_DEVICE_CONTACT_MESSAGE");//12
        notificationKeyList.add("MT_CANCEL_CALL");//13
        notificationKeyList.add("MT_MESSAGE");//14
    }

    private static final String TAG = "MobiComPushReceiver";

    public static boolean isMobiComPushNotification(Context context, Intent intent) {
        //This is to identify collapse key sent in notification..
        String payLoad = intent.getStringExtra("collapse_key");
        Log.i(TAG, "Received notification: " + payLoad);

        if (payLoad != null && payLoad.contains(MTCOM_PREFIX) || notificationKeyList.contains(payLoad)) {
            return true;
        } else {
            for (String key : notificationKeyList) {
                payLoad = intent.getStringExtra(key);
                if (payLoad != null) {
                    return true;
                }
            }
            return false;
        }
    }

    public static void processMessage(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            // ToDo: do something for invalidkey ;
            // && extras.get("InvalidKey") != null
            String message = intent.getStringExtra("collapse_key");
            String deleteConversationForContact = intent.getStringExtra(notificationKeyList.get(6));
            String deleteSms = intent.getStringExtra(notificationKeyList.get(4));
            String multipleMessageDelete = intent.getStringExtra(notificationKeyList.get(5));
            String mtexterUser = intent.getStringExtra(notificationKeyList.get(7));
            String payloadForDelivered = intent.getStringExtra(notificationKeyList.get(2));

            MobiComMessageService messageService = new MobiComMessageService(context, MessageIntentService.class);

            if (!TextUtils.isEmpty(payloadForDelivered)) {
                messageService.updateDeliveryStatus(payloadForDelivered);
            }
            if (!TextUtils.isEmpty(deleteConversationForContact)) {
                MobiComConversationService conversationService = new MobiComConversationService(context);
                conversationService.deleteConversationFromDevice(deleteConversationForContact);
                BroadcastService.sendConversationDeleteBroadcast(context, BroadcastService.INTENT_ACTIONS.DELETE_CONVERSATION.toString(), deleteConversationForContact);
            }

            if (!TextUtils.isEmpty(mtexterUser)) {
                Log.i(TAG, "Received GCM message MTEXTER_USER: " + mtexterUser);
                if (mtexterUser.contains("{")) {
                    Gson gson = new Gson();
                    ContactContent contactContent = gson.fromJson(mtexterUser, ContactContent.class);
                    ContactService.addUsersToContact(context, contactContent.getContactNumber(), contactContent.getAppVersion(), true);
                } else {
                    String[] details = mtexterUser.split(",");
                    ContactService.addUsersToContact(context, details[0], Short.parseShort(details[1]), true);
                }
            }
            if (!TextUtils.isEmpty(multipleMessageDelete)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                MessageDeleteContent messageDeleteContent = gson.fromJson(multipleMessageDelete, MessageDeleteContent.class);

                for (String deletedSmsKeyString : messageDeleteContent.getDeleteKeyStrings()) {
                    processDeleteSingleMessageRequest(context, deletedSmsKeyString, messageDeleteContent.getContactNumber());
                }
            }

            if (!TextUtils.isEmpty(deleteSms)) {
                String contactNumbers = deleteSms.split(",").length > 1 ? deleteSms.split(",")[1] : null;
                processDeleteSingleMessageRequest(context, deleteSms.split(",")[0], contactNumbers);
            }

            if (notificationKeyList.get(1).equalsIgnoreCase(message)) {

            } else if (notificationKeyList.get(0).equalsIgnoreCase(message)) {
                messageService.syncMessages();
            } else if (notificationKeyList.get(3).equalsIgnoreCase(message)) {
                //  MessageStatUtil.sendMessageStatsToServer(context);
            }
        }
    }


    private static void processDeleteSingleMessageRequest(Context context, String deletedSmsKeyString, String contactNumber) {
        MobiComConversationService conversationService = new MobiComConversationService(context);
        contactNumber = conversationService.deleteMessageFromDevice(deletedSmsKeyString, contactNumber);
        BroadcastService.sendMessageDeleteBroadcast(context, BroadcastService.INTENT_ACTIONS.DELETE_MESSAGE.toString(), deletedSmsKeyString, contactNumber);
    }

    public static void processMessageAsync(final Context context, final Intent intent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                processMessage(context, intent);
            }
        }).start();
    }
}
