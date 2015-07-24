package com.applozic.mobicomkit.api.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Action;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationCompat.WearableExtender;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;

/**
 * @author adarsh
 */
public class WearableNotificationWithVoice {

    Class<?> notificationHandler;
    Context mContext;
    int actionIconResId;
    Builder notificationBuilder;
    int replyLabelResourceId;
    int actionTitleId;
    int notificationId;
    PendingIntent pendingIntent;

    public static final String EXTRA_VOICE_REPLY = "extra_voice_reply";

    /**
     * @param notificationBuilder
     * @param actionTitleId
     * @param replyLabelResourceId
     * @param actionIcon
     * @param notificationId
     */
    public WearableNotificationWithVoice(Builder notificationBuilder,
                                         int actionTitleId, int replyLabelResourceId, int actionIcon, int notificationId) {
        this.notificationBuilder = notificationBuilder;
        this.replyLabelResourceId = replyLabelResourceId;
        this.actionIconResId = actionIcon;
        this.actionTitleId = actionTitleId;
        this.notificationId = notificationId;
    }

    public void setNotificationHandler(Class<?> replyActivityClass) {
        this.notificationHandler = replyActivityClass;
    }

    public void setCurrentContext(Context currentContext) {
        this.mContext = currentContext;
    }

    public void setPendingIntent(PendingIntent pendingIntent) {
        this.pendingIntent = pendingIntent;
    }

    /**
     * This method is just like a wrapper class method for usual notification class which add voice actions
     * for wearable devices
     *
     * @throws RuntimeException
     */
    public void sendNotification() throws Exception {
        if (pendingIntent == null && notificationHandler == null) {
            throw new RuntimeException("Either pendingIntent or handler class requires.");
        }
        Action action = buildWearableAction();
        Notification notification = notificationBuilder.extend(new WearableExtender().addAction(action)).build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        notificationManager.notify(notificationId, notification);
    }

    private Action buildWearableAction() {
        String replyLabel = mContext.getString(replyLabelResourceId);
        RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_VOICE_REPLY).setLabel(replyLabel).build();
        // Create an intent for the reply action
        if (pendingIntent == null) {
            Intent replyIntent = new Intent(mContext, notificationHandler);
            pendingIntent = PendingIntent.getActivity(mContext, 0, replyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
        // Create the reply action and add the remote input
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(actionIconResId,
                mContext.getString(actionTitleId), pendingIntent).addRemoteInput(remoteInput).build();
        return action;
    }

}