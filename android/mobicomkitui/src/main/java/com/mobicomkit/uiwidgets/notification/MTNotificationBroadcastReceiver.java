package com.mobicomkit.uiwidgets.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.mobicomkit.api.MobiComKitConstants;
import com.mobicomkit.api.conversation.Message;
import com.mobicomkit.api.notification.NotificationService;
import com.mobicomkit.contact.AppContactService;
import com.mobicomkit.uiwidgets.R;

import net.mobitexter.mobiframework.json.GsonUtils;
import net.mobitexter.mobiframework.people.contact.Contact;

/**
 * Created by adarsh on 3/5/15.
 */
public class MTNotificationBroadcastReceiver extends BroadcastReceiver {


    private static final String TAG = "MTBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String messageJson = intent.getStringExtra(MobiComKitConstants.MESSAGE_JSON_INTENT);
        Log.i(TAG, "Received broadcast, action: " + action + ", message: " + messageJson);
        if (!TextUtils.isEmpty(messageJson)) {
            final Message message = (Message) GsonUtils.getObjectFromJson(messageJson, Message.class);
            final NotificationService notificationService =
                    new NotificationService(R.drawable.ic_launcher, context, R.string.wearable_action_label, R.string.wearable_action_title, R.drawable.mobicom_ic_action_send);

            final Contact contact = new AppContactService(context).getContactWithFallback(message.getContactIds());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    notificationService.notifyUser(contact, message);
                }
            }).start();
        }
    }
}
