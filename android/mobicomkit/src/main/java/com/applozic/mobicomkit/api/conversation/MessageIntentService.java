package com.applozic.mobicomkit.api.conversation;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.conversation.schedule.MessageSenderTimerTask;
import com.applozic.mobicomkit.api.conversation.schedule.ScheduleMessageService;

import com.applozic.mobicommons.json.GsonUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

/**
 * Created by devashish on 15/12/13.
 */
public class MessageIntentService extends IntentService {

    private static final String TAG = "MessageIntentService";
    private Map<String, Thread> runningTaskMap = new HashMap<String, Thread>();

    public static final String UPLOAD_CANCEL = "cancel_upload";

    public MessageIntentService() {
        super("MessageIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getStringExtra(UPLOAD_CANCEL) != null) {
            //TODO: not completed yet ....
            Thread thread = runningTaskMap.get(intent.getStringExtra(UPLOAD_CANCEL));
            if (thread != null) {
                thread.interrupt();
            } else {
                Log.w(TAG, "Thread not found..." + runningTaskMap);
            }
            return;
        }
        final Message message = (Message) GsonUtils.getObjectFromJson(intent.getStringExtra(MobiComKitConstants.MESSAGE_JSON_INTENT), Message.class);
        Thread thread = new Thread(new MessegeSender(message));
        thread.start();

        if (message.hasAttachment()) {
            runningTaskMap.put(getMapKey(message), thread);
        }
    }

    private String getMapKey(Message message) {
        return message.getFilePaths().get(0) + message.getContactIds();
    }

    private class MessegeSender implements Runnable {
        private Message message;

        public MessegeSender(Message message) {
            this.message = message;
        }

        @Override
        public void run() {
            try {
                new MessageClientService(MessageIntentService.this).sendMessageToServer(message, ScheduleMessageService.class);
                if (message.hasAttachment() && !message.isAttachmentUploadInProgress()) {
                    runningTaskMap.remove(getMapKey(message));
                }
                int groupSmsDelayInSec = MobiComUserPreference.getInstance(MessageIntentService.this).getGroupSmsDelayInSec();
                boolean isDelayRequire = (groupSmsDelayInSec > 0 && message.isSentViaCarrier() && message.isSentToMany());
                if (message.getScheduledAt() == null) {
                    String[] toList = message.getTo().trim().replace("undefined,", "").split(",");

                    for (String tofield : toList) {
                        if (isDelayRequire && !message.getTo().startsWith(tofield)) {
                            new Timer().schedule(new MessageSenderTimerTask(new MobiComMessageService(MessageIntentService.this, MessageIntentService.class), message, tofield), groupSmsDelayInSec * 1000);
                        } else {
                            new MobiComMessageService(MessageIntentService.this, MessageIntentService.class).processMessage(message, tofield);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
