package com.applozic.mobicomkit.api.conversation;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.api.conversation.selfdestruct.DisappearingMessageTask;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.sync.SyncMessageFeed;

import com.applozic.mobicommons.commons.core.utils.Support;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.people.contact.Contact;
import com.applozic.mobicommons.personalization.PersonalizedMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

/**
 * Created by devashish on 24/3/15.
 */
public class MobiComMessageService {

    public static final long DELAY = 60000L;
    private static final String TAG = "MobiComMessageService";
    public static Map<String, Uri> map = new HashMap<String, Uri>();
    public static Map<String, Message> mtMessages = new LinkedHashMap<String, Message>();
    protected Context context;
    protected MobiComConversationService conversationService;
    protected MessageDatabaseService messageDatabaseService;
    protected MessageClientService messageClientService;
    protected Class messageIntentServiceClass;
    protected BaseContactService baseContactService;

    public MobiComMessageService(Context context, Class messageIntentServiceClass) {
        this.context = context;
        this.messageDatabaseService = new MessageDatabaseService(context);
        this.messageClientService = new MessageClientService(context);
        this.conversationService = new MobiComConversationService(context);
        this.messageIntentServiceClass = messageIntentServiceClass;
        //Todo: this can be changed to DeviceContactService for device contacts usage.
        this.baseContactService = new AppContactService(context);
    }

    public Message processMessage(final Message messageToProcess, String tofield) {
        Message message = prepareMessage(messageToProcess, tofield);

        if (message.getType().equals(Message.MessageType.MT_INBOX.getValue())) {
            addMTMessage(message);
            //TODO: in case of isStoreOn device is false ..have to handle fall back
        } else if (message.getType().equals(Message.MessageType.MT_OUTBOX.getValue())) {
            Uri uri = null;
            Log.i(TAG, "Got mt outbox message");
            String mapKey = message.getKeyString() + "," + message.getContactIds();
            map.put(mapKey, uri);
            mtMessages.put(mapKey, message);
        }
        Log.i(TAG, "Sending message: " + message);
        return message;
    }

    public Message prepareMessage(Message messageToProcess, String tofield) {
        Message message = new Message(messageToProcess);
        message.setMessageId(messageToProcess.getMessageId());
        message.setKeyString(messageToProcess.getKeyString());
        message.setPairedMessageKeyString(messageToProcess.getPairedMessageKeyString());

        if (message.getMessage() != null && PersonalizedMessage.isPersonalized(message.getMessage())) {
            Contact contact =  baseContactService.getContactById(tofield);
            if (contact != null) {
                message.setMessage(PersonalizedMessage.prepareMessageFromTemplate(message.getMessage(), contact));
            }
        }
        return message;
    }

    public Contact addMTMessage(Message message) {
        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);

        message.processContactIds(context);
        Contact receiverContact =  baseContactService.getContactById(message.getTo());

        if (message.getMessage() != null && PersonalizedMessage.isPersonalized(message.getMessage())) {
            message.setMessage(PersonalizedMessage.prepareMessageFromTemplate(message.getMessage(), receiverContact));
        }

        messageDatabaseService.createMessage(message);

        Contact contact = baseContactService.getContactById(message.getTo());
        BroadcastService.sendMessageUpdateBroadcast(context, BroadcastService.INTENT_ACTIONS.SYNC_MESSAGE.toString(), message);

        //Check if we are........container is already opened...don't send broadcast
        if (BroadcastService.currentUserId == null) {
            MobiComUserPreference.getInstance(context).setNewMessageFlag(true);
            BroadcastService.sendNotificationBroadcast(context, message);
        }

        Log.i(TAG, "Updating delivery status: " + message.getPairedMessageKeyString() + ", " + userPreferences.getUserId() + ", " + userPreferences.getContactNumber());
        messageClientService.updateDeliveryStatus(message.getPairedMessageKeyString(), userPreferences.getUserId(), userPreferences.getContactNumber());
        return contact;
    }

    public synchronized void syncMessages() {
        Log.i(TAG, "Starting syncMessages");

        final MobiComUserPreference userpref = MobiComUserPreference.getInstance(context);
        SyncMessageFeed syncMessageFeed = messageClientService.getMessageFeed(userpref.getDeviceKeyString(), userpref.getLastSyncTime());
        Log.i(TAG, "Got sync response " + syncMessageFeed);
        // if regIdInvalid in syncrequest, tht means device reg with c2dm is no
        // more valid, do it again and make the sync request again
        if (syncMessageFeed != null && syncMessageFeed.isRegIdInvalid()
                && Utils.hasFroyo()) {
            Log.i(TAG, "Going to call GCM device registration");
            //Todo: Replace it with mobicomkit gcm registration
            // C2DMessaging.register(context);
        }
        if (syncMessageFeed != null && syncMessageFeed.getMessages() != null) {
            List<Message> messageList = syncMessageFeed.getMessages();
            Log.i(TAG, "got messages : " + messageList.size());

            for (final Message message : messageList) {
                Log.i(TAG, "calling  syncMessages : " + message.getTo() + " " + message.getMessage());
                String[] toList = message.getTo().trim().replace("undefined,", "").split(",");

                for (String tofield : toList) {
                    processMessage(message, tofield);
                    MobiComUserPreference.getInstance(context).setLastInboxSyncTime(message.getCreatedAtTime());
                }
                MessageClientService.recentProcessedMessage.add(message);
                BroadcastService.sendMessageUpdateBroadcast(context, BroadcastService.INTENT_ACTIONS.SYNC_MESSAGE.toString(), message);
                messageDatabaseService.createMessage(message);
            }
            userpref.setLastSyncTime(String.valueOf(syncMessageFeed.getLastSyncTime()));
        }
    }

    public synchronized void syncMessagesWithServer(String syncMessage) {
        Toast.makeText(context, syncMessage, Toast.LENGTH_LONG).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                syncMessages();
            }
        }).start();
    }

    public void putMtextToDatabase(String payloadForMtextReceived) {
        JSONObject json = null;
        try {
            json = new JSONObject(payloadForMtextReceived);

            String smsKeyString = json.getString("keyString");
            String receiverNumber = json.getString("contactNumber");
            String body = json.getString("message");
            Integer timeToLive = json.isNull("timeToLive") ? null : Integer.parseInt(json.getString("timeToLive"));
            Message mTextMessageReceived = new Message();
            mTextMessageReceived.setTo(json.getString("senderContactNumber"));
            mTextMessageReceived.setCreatedAtTime(System.currentTimeMillis());
            mTextMessageReceived.setMessage(body);
            mTextMessageReceived.setSendToDevice(Boolean.FALSE);
            mTextMessageReceived.setSent(Boolean.TRUE);
            mTextMessageReceived.setDeviceKeyString(MobiComUserPreference.getInstance(context).getDeviceKeyString());
            mTextMessageReceived.setType(Message.MessageType.MT_INBOX.getValue());
            mTextMessageReceived.setSource(Message.Source.MT_MOBILE_APP.getValue());
            mTextMessageReceived.setTimeToLive(timeToLive);

            if (json.has("fileMetaKeyStrings")) {
                JSONArray fileMetaKeyStringsJSONArray = json.getJSONArray("fileMetaKeyStrings");
                List<String> fileMetaKeyStrings = new ArrayList<String>();
                for (int i = 0; i < fileMetaKeyStringsJSONArray.length(); i++) {
                    JSONObject fileMeta = fileMetaKeyStringsJSONArray.getJSONObject(i);
                    fileMetaKeyStrings.add(fileMeta.toString());
                }
                mTextMessageReceived.setFileMetaKeyStrings(fileMetaKeyStrings);
            }

            mTextMessageReceived.processContactIds(context);

            mTextMessageReceived.setTo(mTextMessageReceived.getTo());
            Contact receiverContact = baseContactService.getContactById(receiverNumber);

            if (mTextMessageReceived.getMessage() != null && PersonalizedMessage.isPersonalized(mTextMessageReceived.getMessage())) {
                mTextMessageReceived.setMessage(PersonalizedMessage.prepareMessageFromTemplate(mTextMessageReceived.getMessage(), receiverContact));
            }

            try {
                messageClientService.sendMessageToServer(mTextMessageReceived);
            } catch (Exception ex) {
                Log.i(TAG, "Received message error " + ex.getMessage());
            }
            messageClientService.updateDeliveryStatus(smsKeyString, null, receiverNumber);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
    }

    public void addWelcomeMessage(String welcome_message) {
        Message message = new Message();
        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);
        message.setContactIds(new Support(context).getSupportNumber());
        message.setTo(new Support(context).getSupportNumber());
        message.setMessage(welcome_message);
        message.setStoreOnDevice(Boolean.TRUE);
        message.setSendToDevice(Boolean.FALSE);
        message.setType(Message.MessageType.MT_INBOX.getValue());
        message.setDeviceKeyString(userPreferences.getDeviceKeyString());
        message.setSource(Message.Source.MT_MOBILE_APP.getValue());
        conversationService.sendMessage(message, messageIntentServiceClass);
    }

    public void updateDeliveryStatus(String key) {
        //Todo: Check if this is possible? In case the delivery report reaches before the sms is reached, then wait for the sms.
        Log.i(TAG, "Got the delivery report for key: " + key);
        String keyParts[] = key.split((","));
        Message message = messageDatabaseService.getMessage(keyParts[0]);
        if (message != null) {
            message.setDelivered(Boolean.TRUE);
            //Todo: Server need to send the contactNumber of the receiver in case of group messaging and update
            //delivery report only for that number
            messageDatabaseService.updateMessageDeliveryReport(keyParts[0], null);
            BroadcastService.sendMessageUpdateBroadcast(context, BroadcastService.INTENT_ACTIONS.MESSAGE_DELIVERY.toString(), message);
            if (message.getTimeToLive() != null && message.getTimeToLive() != 0) {
                Timer timer = new Timer();
                timer.schedule(new DisappearingMessageTask(context, new MobiComConversationService(context), message), message.getTimeToLive() * 60 * 1000);
            }
        } else {
            Log.i(TAG, "Message is not present in table, keyString: " + keyParts[0]);
        }
        map.remove(key);
        mtMessages.remove(key);
    }

    public void createEmptyMessages(List<Contact> contactList) {
        for (Contact contact : contactList) {
            createEmptyMessage(contact);
        }

        BroadcastService.sendLoadMoreBroadcast(context, true);
    }

    public void createEmptyMessage(Contact contact) {
        Message sms = new Message();
        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);
        sms.setContactIds(contact.getFormattedContactNumber());
        sms.setTo(contact.getContactNumber());
        sms.setCreatedAtTime(0L);
        sms.setStoreOnDevice(Boolean.TRUE);
        sms.setSendToDevice(Boolean.FALSE);
        sms.setType(Message.MessageType.MT_OUTBOX.getValue());
        sms.setDeviceKeyString(userPreferences.getDeviceKeyString());
        sms.setSource(Message.Source.MT_MOBILE_APP.getValue());
        messageDatabaseService.createMessage(sms);
    }
}
