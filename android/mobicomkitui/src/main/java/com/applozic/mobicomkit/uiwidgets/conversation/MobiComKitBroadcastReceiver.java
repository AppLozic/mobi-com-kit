package com.applozic.mobicomkit.uiwidgets.conversation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.MobiComActivity;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.MobiComConversationFragment;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.MobiComQuickConversationFragment;
import com.applozic.mobicomkit.uiwidgets.instruction.InstructionUtil;

import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.contact.Contact;

/**
 * Created by devashish on 4/2/15.
 */
public class MobiComKitBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "MTBroadcastReceiver";

    private MobiComQuickConversationFragment quickConversationFragment;
    private MobiComConversationFragment conversationFragment;
    private BaseContactService baseContactService;

    public MobiComKitBroadcastReceiver(MobiComQuickConversationFragment quickConversationFragment, MobiComConversationFragment conversationFragment) {
        this.quickConversationFragment = quickConversationFragment;
        this.conversationFragment = conversationFragment;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Message message = null;
        String messageJson = intent.getStringExtra(MobiComKitConstants.MESSAGE_JSON_INTENT);
        if (!TextUtils.isEmpty(messageJson)) {
            message = (Message) GsonUtils.getObjectFromJson(messageJson, Message.class);
        }
        Log.i(TAG, "Received broadcast, action: " + action + ", message: " + message);

        String userId = message != null ? message.getContactIds() : "";
        if (message != null && !conversationFragment.isBroadcastedToGroup(message.getBroadcastGroupId()) && !message.isSentToMany()) {
            quickConversationFragment.addMessage(message);
        } else if (message != null && message.isSentToMany() && BroadcastService.INTENT_ACTIONS.SYNC_MESSAGE.toString().equals(intent.getAction())) {
            for (String toField : message.getTo().split(",")) {
                Message singleMessage = new Message(message);
                singleMessage.setBroadcastGroupId(null);
                singleMessage.setKeyString(message.getKeyString());
                singleMessage.setTo(toField);
                singleMessage.processContactIds(context);
                quickConversationFragment.addMessage(singleMessage);
            }
        }

        String keyString = intent.getStringExtra("keyString");

        if (BroadcastService.INTENT_ACTIONS.INSTRUCTION.toString().equals(action)) {
            InstructionUtil.showInstruction(context, intent.getIntExtra("resId", -1), intent.getBooleanExtra("actionable", false), R.color.instruction_color);
        } else if (BroadcastService.INTENT_ACTIONS.FIRST_TIME_SYNC_COMPLETE.toString().equals(action)) {
            quickConversationFragment.downloadConversations(true);
        } else if (BroadcastService.INTENT_ACTIONS.LOAD_MORE.toString().equals(action)) {
            quickConversationFragment.setLoadMore(intent.getBooleanExtra("loadMore", true));
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_SYNC_ACK_FROM_SERVER.toString().equals(action)) {
            if (userId.equals(conversationFragment.getCurrentUserId()) ||
                    conversationFragment.isBroadcastedToGroup(message.getBroadcastGroupId())) {
                conversationFragment.updateMessageKeyString(message);
            }
        } else if (BroadcastService.INTENT_ACTIONS.SYNC_MESSAGE.toString().equals(intent.getAction())) {
            if (userId.equals(conversationFragment.getCurrentUserId()) ||
                    conversationFragment.isBroadcastedToGroup(message.getBroadcastGroupId())) {
                conversationFragment.addMessage(message);
            }
            if (message.getBroadcastGroupId() == null) {
                quickConversationFragment.updateLastMessage(keyString, userId);
            }
        } else if (BroadcastService.INTENT_ACTIONS.DELETE_MESSAGE.toString().equals(intent.getAction())) {
            userId = intent.getStringExtra("contactNumbers");
            if (PhoneNumberUtils.compare(userId, MobiComActivity.currentOpenedContactNumber)) {
                conversationFragment.deleteMessageFromDeviceList(keyString);
            } else {
                //Todo: if it is sent to many and remove from all.
                quickConversationFragment.updateLastMessage(keyString, userId);
            }
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_DELIVERY.toString().equals(action)) {
            if (userId.equals(conversationFragment.getCurrentUserId())) {
                conversationFragment.updateDeliveryStatus(message);
            }
        } else if (BroadcastService.INTENT_ACTIONS.DELETE_CONVERSATION.toString().equals(action)) {
            //Todo: If conversation fragment is visible then hide it.
            String contactNumber = intent.getStringExtra("contactNumber");
            Contact contact = baseContactService.getContactById(contactNumber);
            conversationFragment.clearList();
            quickConversationFragment.removeConversation(contact);
        } else if (BroadcastService.INTENT_ACTIONS.UPLOAD_ATTACHMENT_FAILED.toString().equals(action) && message != null) {
            conversationFragment.updateUploadFailedStatus(message);
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_ATTACHMENT_DOWNLOAD_DONE.toString().equals(action) && message != null) {
            conversationFragment.updateDownloadStatus(message);
        }
    }
}
