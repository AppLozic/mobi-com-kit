package com.mobicomkit.api.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;

import com.mobicomkit.api.MobiComKitClientService;
import com.mobicomkit.api.MobiComKitConstants;
import com.mobicomkit.api.conversation.Message;
import com.mobicomkit.broadcast.NotificationBroadcastReceiver;

import net.mobitexter.mobiframework.json.GsonUtils;
import net.mobitexter.mobiframework.people.contact.Contact;

import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * Created with IntelliJ IDEA.
 * User: devashish
 * Date: 17/3/13
 * Time: 7:36 PM
 */
public class NotificationService {

    private static final int NOTIFICATION_ID = 1000;
    private Context context;
    private int iconResourceId;
    private int wearable_action_title;
    private int wearable_action_label;
    private int wearable_send_icon;


    public NotificationService( int iconResourceID, Context context,int wearable_action_label,int wearable_action_title,int wearable_send_icon){
        this.context =  context;
        this.iconResourceId = iconResourceID;
        this.wearable_action_label= wearable_action_label;
        this.wearable_action_title= wearable_action_title;
        this.wearable_send_icon = wearable_send_icon;


    }

    public void notifyUser(Contact contact, Message sms) {

        Intent intent = new Intent();
        intent.putExtra(MobiComKitConstants.MESSAGE_JSON_INTENT, GsonUtils.getJsonFromObject(sms, Message.class));
        intent.setAction(NotificationBroadcastReceiver.LAUNCH_APP);
        intent.putExtra(MobiComKitConstants.ACTIVITY_TO_OPEN, "com.mobicomkit.uiwidgets.conversation.activity.SlidingPaneActivity");
        intent.setClass(context,NotificationBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) (System.currentTimeMillis() & 0xfffffff), intent, 0);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(iconResourceId)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), iconResourceId))
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle(contact.getFullName() != null ? contact.getFullName() : sms.getContactIds())
                        .setContentText(sms.getMessage())
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setAutoCancel(true);
        if (sms.hasAttachment()) {
            try {
                InputStream in;
                HttpURLConnection httpConn = new MobiComKitClientService(context).openHttpConnection(sms.getFileMetas().get(0).getThumbnailUrl());
                int response = httpConn.getResponseCode();
                if (response == HttpURLConnection.HTTP_OK) {
                    in = httpConn.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(in);
                    mBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        WearableNotificationWithVoice notificationWithVoice =
                new WearableNotificationWithVoice(mBuilder,wearable_action_title,
                        wearable_action_label,wearable_send_icon,sms.getContactIds().hashCode());
        notificationWithVoice.setCurrentContext(context);
        notificationWithVoice.setPendingIntent(pendingIntent);

        try {
            notificationWithVoice.sendNotification();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}