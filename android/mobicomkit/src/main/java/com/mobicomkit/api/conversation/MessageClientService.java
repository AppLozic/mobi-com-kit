package com.mobicomkit.api.conversation;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mobicomkit.api.HttpRequestUtils;
import com.mobicomkit.api.MobiComKitClientService;
import com.mobicomkit.api.MobiComKitServer;
import com.mobicomkit.api.account.user.MobiComUserPreference;
import com.mobicomkit.api.attachment.FileClientService;
import com.mobicomkit.api.attachment.FileMeta;
import com.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.mobicomkit.api.conversation.schedule.ScheduledMessageUtil;
import com.mobicomkit.broadcast.BroadcastService;
import com.mobicomkit.sync.SmsSyncRequest;
import com.mobicomkit.sync.SyncMessageFeed;

import net.mobitexter.mobiframework.json.AnnotationExclusionStrategy;
import net.mobitexter.mobiframework.json.ArrayAdapterFactory;
import net.mobitexter.mobiframework.json.GsonUtils;
import net.mobitexter.mobiframework.people.contact.Contact;
import net.mobitexter.mobiframework.people.group.Group;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by devashish on 26/12/14.
 */
public class MessageClientService extends MobiComKitClientService {

    public static final int SMS_SYNC_BATCH_SIZE = 5;
    public static final String DEVICE_KEY = "deviceKeyString";
    public static final String LAST_SYNC_KEY = "lastSyncTime";
    public static final String FILE_META = "fileMeta";
    private static final String TAG = "MessageClientService";
    public static List<Message> recentProcessedMessage = new ArrayList<Message>();
    public static List<Message> recentMessageSentToServer = new ArrayList<Message>();
    private Context context;
    private MessageDatabaseService messageDatabaseService;
    private HttpRequestUtils httpRequestUtils;

    public MessageClientService(Context context) {
        super(context);
        this.context = context;
        this.messageDatabaseService = new MessageDatabaseService(context);
        this.httpRequestUtils = new HttpRequestUtils(context);
    }

    public String updateDeliveryStatus(Message message, String contactNumber, String countryCode) {
        try {
            String argString = "?smsKeyString=" + message.getKeyString() + "&contactNumber=" + URLEncoder.encode(contactNumber, "UTF-8") + "&deviceKeyString=" + message.getDeviceKeyString()
                    + "&countryCode=" + countryCode;
            String URL = MobiComKitServer.UPDATE_DELIVERY_FLAG_URL + argString;
            return httpRequestUtils.getStringFromUrl(URL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateDeliveryStatus(String messageKeyString, String userId, String receiverNumber) {
        try {
            //Note: messageKeyString comes as null for the welcome message as it is inserted directly.
            if (TextUtils.isEmpty(messageKeyString)) {
                return;
            }
            httpRequestUtils.getStringFromUrl(MobiComKitServer.MTEXT_DELIVERY_URL + "smsKeyString=" + messageKeyString
                    + "&userId=" + userId + "&contactNumber=" + URLEncoder.encode(receiverNumber, "UTF-8"));
        } catch (Exception ex) {
            Log.e(TAG, "Exception while updating delivery report for MT message", ex);
        }
    }

    public void syncPendingMessages() {
        syncPendingMessages(true);
    }

    public synchronized void syncPendingMessages(boolean broadcast) {
        List<Message> pendingMessages = messageDatabaseService.getPendingMessages();
        Log.i(TAG, "Found " + pendingMessages.size() + " pending messages to sync.");
        for (Message message : pendingMessages) {
            Log.i(TAG, "Syncing pending message: " + message);
            sendPendingMessageToServer(message, broadcast);
        }
    }

    public boolean syncMessagesWithServer(List<Message> messageList) {
        Log.i(TAG, "Total messages to sync: " + messageList.size());
        List<Message> messages = new ArrayList<Message>(messageList);
        do {
            try {
                SmsSyncRequest smsSyncRequest = new SmsSyncRequest();
                if (messages.size() > SMS_SYNC_BATCH_SIZE) {
                    List<Message> subList = new ArrayList(messages.subList(0, SMS_SYNC_BATCH_SIZE));
                    smsSyncRequest.setSmsList(subList);
                    messages.removeAll(subList);
                } else {
                    smsSyncRequest.setSmsList(new ArrayList<Message>(messages));
                    messages.clear();
                }

                String response = syncMessages(smsSyncRequest);
                Log.i(TAG, "response from sync sms url::" + response);
                String[] keyStrings = null;
                if (!TextUtils.isEmpty(response) && !response.equals("error")) {
                    keyStrings = response.trim().split(",");
                }
                if (keyStrings != null) {
                    int i = 0;
                    for (Message message : smsSyncRequest.getSmsList()) {
                        if (!TextUtils.isEmpty(keyStrings[i])) {
                            message.setKeyString(keyStrings[i]);
                            messageDatabaseService.createMessage(message);
                        }
                        i++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "exception" + e);
                return false;
            }
        } while (messages.size() > 0);
        return true;
    }

    public void sendPendingMessageToServer(Message message, boolean broadcast) {
        String keyString = sendMessage(message);
        if (TextUtils.isEmpty(keyString) || keyString.contains("<html>")) {
            return;
        }
        recentMessageSentToServer.add(message);

        if (broadcast) {
            BroadcastService.sendMessageUpdateBroadcast(context, BroadcastService.INTENT_ACTIONS.MESSAGE_SYNC_ACK_FROM_SERVER.toString(), message);
        }

        messageDatabaseService.updateMessageSyncStatus(message, keyString);
    }

    public void sendMessageToServer(Message message) throws Exception {
        sendMessageToServer(message, null);
    }

    public void sendMessageToServer(Message message, Class intentClass) throws Exception {
        processMessage(message);
        if (message.getScheduledAt() != null && message.getScheduledAt() != 0 && intentClass != null) {
            new ScheduledMessageUtil(context, intentClass).createScheduleMessage(message, context);
        }
    }

    public void processMessage(Message message) throws Exception {
        if (recentMessageSentToServer.contains(message)) {
            return;
        }

        recentMessageSentToServer.add(message);

        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);
        message.setSent(Boolean.TRUE);
        message.setSendToDevice(Boolean.FALSE);
        message.setSuUserKeyString(userPreferences.getSuUserKeyString());
        message.processContactIds(context);
        BroadcastService.sendMessageUpdateBroadcast(context, BroadcastService.INTENT_ACTIONS.SYNC_MESSAGE.toString(), message);

        long messageId = -1;

        List<String> fileKeys = new ArrayList<String>();
        if (message.isUploadRequired()) {

            messageId = messageDatabaseService.createMessage(message);

            for (String filePath : message.getFilePaths()) {
                try {
                    String fileMetaResponse = new FileClientService(context).uploadBlobImage(filePath);
                    if (fileMetaResponse == null) {
                        messageDatabaseService.updateCanceledFlag(messageId, 1);
                        BroadcastService.sendMessageUpdateBroadcast(context, BroadcastService.INTENT_ACTIONS.UPLOAD_ATTACHMENT_FAILED.toString(), message);
                        return;
                    }
                    JsonParser jsonParser = new JsonParser();
                    List<FileMeta> metaFileList = new ArrayList<FileMeta>();
                    JsonObject jsonObject = jsonParser.parse(fileMetaResponse).getAsJsonObject();
                    if (jsonObject.has(FILE_META)) {
                        Gson gson = new Gson();
                        metaFileList.add(gson.fromJson(jsonObject.get(FILE_META), FileMeta.class));
                    }
                    for (FileMeta fileMeta : metaFileList) {
                        fileKeys.add(fileMeta.getKeyString());
                    }
                    message.setFileMetas(metaFileList);
                } catch (Exception ex) {
                    Log.e(TAG, "Error uploading file to server: " + filePath);
                    recentMessageSentToServer.remove(message);
                    messageDatabaseService.updateCanceledFlag(messageId, 1);
                    BroadcastService.sendMessageUpdateBroadcast(context, BroadcastService.INTENT_ACTIONS.UPLOAD_ATTACHMENT_FAILED.toString(), message);
                    return;
                }
            }

            message.setFileMetaKeyStrings(fileKeys);
        }

        //Todo: set filePaths
        String keyString = new MessageClientService(context).sendMessage(message);
        if (TextUtils.isEmpty(keyString)) {
            keyString = UUID.randomUUID().toString();
            message.setSentToServer(false);
        }

        message.setKeyString(keyString);

        if (!TextUtils.isEmpty(keyString)) {
            //Todo: Handle server message add failure due to internet disconnect.
        } else {
            //Todo: If message type is mtext, tell user that internet is not working, else send update with db id.
        }
        BroadcastService.sendMessageUpdateBroadcast(context, BroadcastService.INTENT_ACTIONS.MESSAGE_SYNC_ACK_FROM_SERVER.toString(), message);

        if (messageId != -1) {
            messageDatabaseService.updateMessageFileMetas(messageId, message);
        } else {
            messageDatabaseService.createMessage(message);
        }

        if (recentMessageSentToServer.size() > 20) {
            recentMessageSentToServer.subList(0, 10).clear();
        }
    }

    public String syncMessages(SmsSyncRequest smsSyncRequest) throws Exception {
        String data = GsonUtils.getJsonFromObject(smsSyncRequest, SmsSyncRequest.class);
        return httpRequestUtils.postData(credentials, MobiComKitServer.SYNC_SMS_URL, "application/json", null, data);
    }

    public String sendMessage(Message message) {
        String jsonFromObject = GsonUtils.getJsonFromObject(message, message.getClass());
        Log.i(TAG, "Sending message to server: " + jsonFromObject);
        return httpRequestUtils.postData(credentials, MobiComKitServer.SEND_MESSAGE_URL, "application/json", null, jsonFromObject);
    }

    public SyncMessageFeed getMessageFeed(String deviceKeyString, String lastSyncKeyString) {
        String url = MobiComKitServer.SERVER_SYNC_URL + "?"
                + DEVICE_KEY + "=" + deviceKeyString
                + MobiComKitServer.ARGUMRNT_SAPERATOR + LAST_SYNC_KEY
                + "=" + lastSyncKeyString;
        try {
            Log.i(TAG, "Calling message feed url: " + url);
            String response = httpRequestUtils.getResponse(credentials, url, "application/json", "application/json");
            Log.i(TAG, "Response: " + response);
            Gson gson = new GsonBuilder().registerTypeAdapterFactory(new ArrayAdapterFactory())
                    .setExclusionStrategies(new AnnotationExclusionStrategy()).create();
            return gson.fromJson(response, SyncMessageFeed.class);
        } catch (Exception e) {
            // showAlert("Unable to Process request .Please Contact Support");
            return null;
        }
    }

    public void deleteConversationThreadFromServer(Contact contact) {
        if (TextUtils.isEmpty(contact.getFormattedContactNumber())) {
            return;
        }
        try {
            String url = MobiComKitServer.MESSAGE_THREAD_DELETE_URL + "?contactNumber=" + URLEncoder.encode(contact.getFormattedContactNumber(), "UTF-8");
            httpRequestUtils.getResponse(credentials, url, "text/plain", "text/plain");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void deleteMessage(Message message, Contact contact) {
        String contactNumberParameter = "";
        if (contact != null && !TextUtils.isEmpty(contact.getFormattedContactNumber())) {
            try {
                contactNumberParameter = "&to=" + URLEncoder.encode(contact.getContactNumber(), "UTF-8") + "&contactNumber=" + URLEncoder.encode(contact.getFormattedContactNumber(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        if (message.isSentToServer()) {
            httpRequestUtils.getResponse(credentials, MobiComKitServer.MESSAGE_DELETE_URL + "?key=" + message.getKeyString() + contactNumberParameter, "text/plain", "text/plain");
        }
    }

    public String getMessages(Contact contact, Group group, Long startTime, Long endTime) throws UnsupportedEncodingException {
        String contactNumber = (contact != null ? contact.getFormattedContactNumber() : "");
        String params = "";
        if (TextUtils.isEmpty(contactNumber) && contact != null && !TextUtils.isEmpty(contact.getUserId())) {
            params = "userId=" + contact.getUserId() + "&";
        }
        params += TextUtils.isEmpty(contactNumber) ? "" : ("contactNumber=" + URLEncoder.encode(contactNumber, "utf-8") + "&");
        params += (endTime != null && endTime.intValue() != 0) ? "endTime=" + endTime + "&" : "";
        params += (startTime != null && startTime.intValue() != 0) ? "startTime=" + startTime + "&" : "";
        params += (group != null && group.getGroupId() != null) ? "broadcastGroupId=" + group.getGroupId() + "&" : "";
        params += "startIndex=0&pageSize=50";

        return httpRequestUtils.getResponse(credentials, MobiComKitServer.MESSAGE_LIST_URL + "?" + params
                , "application/json", "application/json");
    }

    public String deleteMessage(Message message) {
        return deleteMessage(message.getKeyString());
    }

    public String deleteMessage(String keyString) {
        return httpRequestUtils.getResponse(credentials, MobiComKitServer.MESSAGE_DELETE_URL + "?key=" + keyString, "text/plain", "text/plain");
    }

    public void updateMessageDeliveryReport(final Message message, final String contactNumber) throws Exception {
        message.setDelivered(Boolean.TRUE);
        messageDatabaseService.updateMessageDeliveryReport(message.getKeyString(), contactNumber);

        BroadcastService.sendMessageUpdateBroadcast(context, BroadcastService.INTENT_ACTIONS.MESSAGE_DELIVERY.toString(), message);
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateDeliveryStatus(message, contactNumber, MobiComUserPreference.getInstance(context).getCountryCode());
            }
        }).start();

        if (MobiComUserPreference.getInstance(context).isWebHookEnable()) {
            processWebHook(message);
        }
    }

    public void processWebHook(final Message message) {
       /* new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = "";
                    String response = HttpRequestUtils.getStringFromUrl(url);
                    AppUtil.myLogger(TAG, "Got response from webhook url: " + response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();*/
    }
}
