package com.applozic.mobicomkit.uiwidgets.conversation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MobiComConversationService;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.MobiComKitActivityInterface;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.ConversationFragment;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.MobiComQuickConversationFragment;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.MultimediaOptionFragment;
import com.applozic.mobicomkit.uiwidgets.people.activity.MobiComKitPeopleActivity;

import com.applozic.mobicommons.commons.core.utils.Support;
import com.applozic.mobicommons.commons.image.ImageUtils;
import com.applozic.mobicommons.file.FilePathFinder;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.contact.Contact;
import com.applozic.mobicommons.people.group.Group;
import com.applozic.mobicommons.people.group.GroupUtils;


public class ConversationUIService {

    public static final int REQUEST_CODE_FULL_SCREEN_ACTION = 301;
    public static final int REQUEST_CODE_CONTACT_GROUP_SELECTION = 101;
    public static final int INSTRUCTION_DELAY = 5000;
    private static final String TAG = "ConversationUIService";
    private FragmentActivity fragmentActivity;
    private BaseContactService baseContactService;

    public ConversationUIService(FragmentActivity fragmentActivity) {
        this.fragmentActivity = fragmentActivity;
        this.baseContactService = new AppContactService(fragmentActivity);
    }

    public MobiComQuickConversationFragment getQuickConversationFragment() {
        return (MobiComQuickConversationFragment) UIService.getActiveFragment(fragmentActivity);
    }

    public ConversationFragment getConversationFragment() {
        return (ConversationFragment) UIService.getActiveFragment(fragmentActivity);
    }

    public void onQuickConversationFragmentItemClick(View view, Contact contact) {
        TextView textView = (TextView) view.findViewById(R.id.unreadSmsCount);
        textView.setVisibility(View.GONE);
        openConversationFragment(contact);
    }

    public void openConversationFragment(final Contact contact) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                //Todo: load fragment from backstack if available and avoid creating new fragment.
                ConversationFragment conversationFragment = new ConversationFragment();
                ((MobiComKitActivityInterface) fragmentActivity).addFragment(conversationFragment);
                conversationFragment.loadConversation(contact);
            }
        });
    }

    public void openConversationFragment(Group group) {
        ConversationFragment conversationFragment = new ConversationFragment();
        ((MobiComKitActivityInterface) fragmentActivity).addFragment(conversationFragment);
        conversationFragment.loadConversation(group);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if ((requestCode == MultimediaOptionFragment.REQUEST_CODE_ATTACH_PHOTO ||
                requestCode == MultimediaOptionFragment.REQUEST_CODE_TAKE_PHOTO)
                && resultCode == Activity.RESULT_OK) {
            Uri selectedFileUri = (intent == null ? null : intent.getData());
            if (selectedFileUri == null) {
                selectedFileUri = getConversationFragment().getMultimediaOptionFragment().getCapturedImageUri();
                ImageUtils.addImageToGallery(FilePathFinder.getPath(fragmentActivity, selectedFileUri), fragmentActivity);
            }

            if (selectedFileUri == null) {
                Bitmap photo = (Bitmap) intent.getExtras().get("data");
                selectedFileUri = ImageUtils.getImageUri(fragmentActivity, photo);
            }
            getConversationFragment().loadFile(selectedFileUri);
            Log.i(TAG, "File uri: " + selectedFileUri);
        }

        if (requestCode == REQUEST_CODE_CONTACT_GROUP_SELECTION && resultCode == Activity.RESULT_OK) {
            checkForStartNewConversation(intent);
        }
    }

    public void deleteConversationThread(final Contact contact, String name) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(fragmentActivity).
                setPositiveButton(R.string.delete_conversation, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new MobiComConversationService(fragmentActivity).deleteAndBroadCast(contact, true);
                    }
                });
        alertDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        alertDialog.setTitle(fragmentActivity.getString(R.string.dialog_delete_conversation_title).replace("[name]", name));
        alertDialog.setMessage(fragmentActivity.getString(R.string.dialog_delete_conversation_confir).replace("[name]", name));
        alertDialog.setCancelable(true);
        alertDialog.create().show();
    }

    public void updateLatestMessage(Message message, String formattedContactNumber) {
        if (!BroadcastService.isQuick()) {
            return;
        }
        getQuickConversationFragment().updateLatestMessage(message, formattedContactNumber);
    }

    public void removeConversation(Message message, String formattedContactNumber) {
        if (!BroadcastService.isQuick()) {
            return;
        }
        getQuickConversationFragment().removeConversation(message, formattedContactNumber);
    }

    public void addMessage(Message message) {
        if (!BroadcastService.isQuick()) {
            return;
        }

        MobiComQuickConversationFragment fragment = (MobiComQuickConversationFragment) UIService.getActiveFragment(fragmentActivity);
        fragment.addMessage(message);

    }

    public void updateLastMessage(String keyString, String userId) {
        if (!BroadcastService.isQuick()) {
            return;
        }
        getQuickConversationFragment().updateLastMessage(keyString, userId);
    }

    public boolean isBroadcastedToGroup(Long broadcastGroupId) {
        if (!BroadcastService.isIndividual()) {
            return false;
        }
        return getConversationFragment().isBroadcastedToGroup(broadcastGroupId);
    }

    public void syncMessages(Message message, String keyString) {
        String userId = message.getContactIds();

        if (BroadcastService.isIndividual()) {
            ConversationFragment conversationFragment = getConversationFragment();
            if (userId.equals(conversationFragment.getCurrentUserId()) ||
                    conversationFragment.isBroadcastedToGroup(message.getBroadcastGroupId())) {
                conversationFragment.addMessage(message);
            }
        }

        if (message.getBroadcastGroupId() == null) {
            updateLastMessage(keyString, userId);
        }
    }

    public void downloadConversations(boolean showInstruction) {
        if (!BroadcastService.isQuick()) {
            return;
        }
        getQuickConversationFragment().downloadConversations(showInstruction);
    }

    public void setLoadMore(boolean loadMore) {
        if (!BroadcastService.isQuick()) {
            return;
        }
        getQuickConversationFragment().setLoadMore(loadMore);
    }

    public void updateMessageKeyString(Message message) {
        if (!BroadcastService.isIndividual()) {
            return;
        }
        String userId = message.getContactIds();
        ConversationFragment conversationFragment = getConversationFragment();
        if (userId.equals(conversationFragment.getContact().getUserId()) ||
                conversationFragment.isBroadcastedToGroup(message.getBroadcastGroupId())) {
            conversationFragment.updateMessageKeyString(message);
        }
    }

    public void deleteMessage(Message message, String keyString, String formattedContactNumber) {
        if (PhoneNumberUtils.compare(formattedContactNumber, BroadcastService.currentUserId)) {
            getConversationFragment().deleteMessageFromDeviceList(keyString);
        } else {
            updateLastMessage(keyString, formattedContactNumber);
        }
    }

    public void updateDeliveryStatus(Message message, String formattedContactNumber) {
        if (!BroadcastService.isIndividual()) {
            return;
        }
        ConversationFragment conversationFragment = getConversationFragment();
        if (formattedContactNumber.equals(conversationFragment.getContact().getContactIds())) {
            conversationFragment.updateDeliveryStatus(message);
        }
    }

    public void deleteConversation(Contact contact) {
        if (BroadcastService.isIndividual()) {
            getConversationFragment().clearList();
        }
        if (BroadcastService.isQuick()) {
            getQuickConversationFragment().removeConversation(contact);
        }
    }

    public void updateUploadFailedStatus(Message message) {
        if (!BroadcastService.isIndividual()) {
            return;
        }
        getConversationFragment().updateUploadFailedStatus(message);
    }

    public void updateDownloadStatus(Message message) {
        if (!BroadcastService.isIndividual()) {
            return;
        }
        getConversationFragment().updateDownloadStatus(message);
    }

    public void startContactActivityForResult(Intent intent, Message message, String messageContent) {
        if (message != null) {
            intent.putExtra(MobiComKitPeopleActivity.FORWARD_MESSAGE, GsonUtils.getJsonFromObject(message, message.getClass()));
        }
        if (messageContent != null) {
            intent.putExtra(MobiComKitPeopleActivity.SHARED_TEXT, messageContent);
        }

        fragmentActivity.startActivityForResult(intent, REQUEST_CODE_CONTACT_GROUP_SELECTION);
    }

    public void startContactActivityForResult() {
        Log.d("Inside ", "StartContactForResult");
        startContactActivityForResult(null, null);
    }

    public void startContactActivityForResult(Message message, String messageContent) {
        //Todo: Change this to driver list fragment or activity
        Intent intent = new Intent(fragmentActivity, MobiComKitPeopleActivity.class);

        startContactActivityForResult(intent, message, messageContent);
    }

    public void checkForStartNewConversation(Intent intent) {
        Contact contact = null;
        Group group = null;

        if (Intent.ACTION_SEND.equals(intent.getAction()) && intent.getType() != null) {
            if ("text/plain".equals(intent.getType())) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    startContactActivityForResult(null, sharedText);
                }
            } else if (intent.getType().startsWith("image/")) {
                //Todo: use this for image forwarding
            }
        }

        final Uri uri = intent.getData();
        if (uri != null) {
            //Note: This is used only for the device contacts
            Long contactId = intent.getLongExtra("contactId", 0);
            if (contactId == 0) {
                //Todo: show warning that the user doesn't have any number stored.
                return;
            }
            contact = baseContactService.getContactById(String.valueOf(contactId));
    }

        Long groupId = intent.getLongExtra("groupId", -1);
        String groupName = intent.getStringExtra("groupName");
        if (groupId != -1) {
            group = GroupUtils.fetchGroup(fragmentActivity, groupId, groupName);
        }

        String contactNumber = intent.getStringExtra("contactNumber");
        Log.d("UIService:","value is ="+contactNumber);

        boolean firstTimeMTexterFriend = intent.getBooleanExtra("firstTimeMTexterFriend", false);
        if (!TextUtils.isEmpty(contactNumber)) {
            contact = baseContactService.getContactById(contactNumber);
            if (BroadcastService.isIndividual()) {
                getConversationFragment().setFirstTimeMTexterFriend(firstTimeMTexterFriend);
            }
        }

        String userId = intent.getStringExtra("userId");
        Log.d("userId","UserID="+userId);
        if (!TextUtils.isEmpty(userId)) {
            contact = baseContactService.getContactById(userId);
        }

        String messageJson = intent.getStringExtra(MobiComKitConstants.MESSAGE_JSON_INTENT);
        if (!TextUtils.isEmpty(messageJson)) {
            Message message = (Message) GsonUtils.getObjectFromJson(messageJson, Message.class);
            contact = baseContactService.getContactById(message.getTo());
        }

        boolean support = intent.getBooleanExtra(Support.SUPPORT_INTENT_KEY, false);
        if (support) {
            contact = new Support(fragmentActivity).getSupportContact();
        }

        if (contact != null) {
            openConversationFragment(contact);
        }

        if (group != null) {
            openConversationFragment(group);
        }

        String forwardMessage = intent.getStringExtra(MobiComKitPeopleActivity.FORWARD_MESSAGE);
        if (!TextUtils.isEmpty(forwardMessage)) {
            Message messageToForward = (Message) GsonUtils.getObjectFromJson(forwardMessage, Message.class);
            getConversationFragment().forwardMessage(messageToForward);
        }

        String sharedText = intent.getStringExtra(MobiComKitPeopleActivity.SHARED_TEXT);
        if (!TextUtils.isEmpty(sharedText)) {
            getConversationFragment().sendMessage(sharedText);
        }
    }

    public void processLocation() {
    }
}