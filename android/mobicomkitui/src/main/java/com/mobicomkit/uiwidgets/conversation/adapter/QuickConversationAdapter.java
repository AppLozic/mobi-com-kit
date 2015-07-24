package com.mobicomkit.uiwidgets.conversation.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobicomkit.api.conversation.Message;
import com.mobicomkit.contact.AppContactService;
import com.mobicomkit.contact.BaseContactService;
import com.mobicomkit.uiwidgets.R;
import com.mobicomkit.uiwidgets.alphanumbericcolor.AlphaNumberColorUtil;
import com.mobicomkit.uiwidgets.conversation.activity.MobiComKitActivityInterface;
import com.mobicomkit.uiwidgets.instruction.InstructionUtil;

import com.applozic.mobicommons.commons.core.utils.DateUtils;
import com.applozic.mobicommons.commons.core.utils.Support;
import com.applozic.mobicommons.commons.image.ImageLoader;
import com.applozic.mobicommons.commons.image.ImageUtils;
import com.applozic.mobicommons.emoticon.EmojiconHandler;
import com.applozic.mobicommons.emoticon.EmoticonUtils;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.people.contact.Contact;
import com.applozic.mobicommons.people.contact.ContactUtils;
import com.applozic.mobicommons.people.group.Group;
import com.applozic.mobicommons.people.group.GroupUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by adarsh on 4/7/15.
 */
public class QuickConversationAdapter extends ArrayAdapter<Message> {

    private static Map<Short, Integer> messageTypeColorMap = new HashMap<Short, Integer>();
    private ImageLoader contactImageLoader;
    private Context context;
    private Contact contact;
    private Group group;
    private boolean individual;
    String mimeType = "";
    private BaseContactService contactService;
    private EmojiconHandler emojiconHandler;


    static {
        messageTypeColorMap.put(Message.MessageType.INBOX.getValue(), R.color.message_type_inbox);
        messageTypeColorMap.put(Message.MessageType.OUTBOX.getValue(), R.color.message_type_outbox);
        messageTypeColorMap.put(Message.MessageType.OUTBOX_SENT_FROM_DEVICE.getValue(), R.color.message_type_outbox_sent_from_device);
        messageTypeColorMap.put(Message.MessageType.MT_INBOX.getValue(), R.color.message_type_mt_inbox);
        messageTypeColorMap.put(Message.MessageType.MT_OUTBOX.getValue(), R.color.message_type_mt_outbox);
        messageTypeColorMap.put(Message.MessageType.CALL_INCOMING.getValue(), R.color.message_type_incoming_call);
        messageTypeColorMap.put(Message.MessageType.CALL_OUTGOING.getValue(), R.color.message_type_outgoing_call);
    }

    public QuickConversationAdapter(final Context context, List<Message> messageList, EmojiconHandler emojiconHandler) {
        super(context,  R.layout.mobicom_message_row_view,  messageList);
        this.context = context;
        this.emojiconHandler = emojiconHandler;
        this.contactService = new AppContactService(context);
        contactImageLoader = new ImageLoader(getContext(), ImageUtils.getLargestScreenDimension((Activity) getContext())) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return  contactService.downloadContactImage((Activity) getContext(),(Contact)data);
            }
        };
        contactImageLoader.setLoadingImage(R.drawable.ic_contact_picture_180_holo_light);
        contactImageLoader.addImageCache(((FragmentActivity) context).getSupportFragmentManager(), 0.1f);
        contactImageLoader.setImageFadeIn(false);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.mobicom_message_row_view, parent, false);
        TextView smTime = (TextView) customView.findViewById(R.id.smTime);
        smTime.setVisibility(View.GONE);
        final Message message = getItem(position);
        if (message != null) {

            TextView smReceivers = (TextView) customView.findViewById(R.id.smReceivers);
            TextView createdAtTime = (TextView) customView.findViewById(R.id.createdAtTime);
            TextView messageTextView = (TextView) customView.findViewById(R.id.message);
            ImageView contactImage = (ImageView) customView.findViewById(R.id.contactImage);
            TextView alphabeticTextView = (TextView) customView.findViewById(R.id.alphabeticImage);
            ImageView sentOrReceived = (ImageView) customView.findViewById(R.id.sentOrReceivedIcon);
            TextView attachedFile = (TextView) customView.findViewById(R.id.attached_file);
            final ImageView attachmentIcon = (ImageView) customView.findViewById(R.id.attachmentIcon);

            List<String> items = Arrays.asList(message.getTo().split("\\s*,\\s*"));
            List<String> userIds = null;
            if (!TextUtils.isEmpty(message.getContactIds())) {
                userIds = Arrays.asList(message.getContactIds().split("\\s*,\\s*"));
            }

            final Contact contactReceiver = contactService.getContactReceiver(items, userIds);

            if (contactReceiver != null) {
                String contactInfo = contactReceiver.getDisplayName();
                if (items.size() > 1) {
                    Contact contact2 = ContactUtils.getContact(getContext(), items.get(1));
                    contactInfo = TextUtils.isEmpty(contactReceiver.getFirstName()) ? contactReceiver.getContactNumber() : contactReceiver.getFirstName() + ", "
                            + (TextUtils.isEmpty(contact2.getFirstName()) ? contact2.getContactNumber() : contact2.getFirstName()) + (items.size() > 2 ? " & others" : "");
                }
                smReceivers.setText(contactInfo);
            }

            //Uri contactUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(contactReceiver.getContactId()));
            if (contactReceiver != null && new Support(context).isSupportNumber(contactReceiver.getContactNumber()) && (!message.isTypeOutbox())) {
                contactImage.setImageResource(R.drawable.ic_launcher);
            } else {
                contactImageLoader.loadImage(contactReceiver, contactImage, alphabeticTextView);
            }
            if (alphabeticTextView != null && contactReceiver!=null) {
                String contactNumber = contactReceiver.getContactNumber().toUpperCase();
                char firstLetter = contactReceiver.getDisplayName().toUpperCase().charAt(0);
                if (firstLetter != '+') {
                    alphabeticTextView.setText(String.valueOf(firstLetter));
                } else if (contactNumber.length() >= 2) {
                    alphabeticTextView.setText(String.valueOf(contactNumber.charAt(1)));
                }

                Character colorKey = AlphaNumberColorUtil.alphabetBackgroundColorMap.containsKey(firstLetter) ? firstLetter : null;
                alphabeticTextView.setTextColor(context.getResources().getColor(AlphaNumberColorUtil.alphabetTextColorMap.get(colorKey)));
                alphabeticTextView.setBackgroundResource(AlphaNumberColorUtil.alphabetBackgroundColorMap.get(colorKey));
            }
            if (attachedFile != null) {
                attachedFile.setText("");
                attachedFile.setVisibility(View.GONE);
            }

            if (attachmentIcon != null) {
                attachmentIcon.setVisibility(View.GONE);
            }
            if (message.isSentToMany()) {
                group = message.getBroadcastGroupId() != null ? GroupUtils.fetchGroup(context, message.getBroadcastGroupId()) : null;
            }

            if (message.hasAttachment() && message.getFilePaths() != null &&
                    !message.getFilePaths().isEmpty()) {
                //Todo: handle it for fileKeyStrings when filePaths is empty
                String filePath = message.getFilePaths().get(0);
                mimeType = FileUtils.getMimeType(filePath);
                attachmentIcon.setVisibility(View.VISIBLE);
                messageTextView.setText(filePath.substring(filePath.lastIndexOf("/") + 1) + " " + messageTextView.getText());
            } else {
                messageTextView.setText(EmoticonUtils.getSmiledText(context, message.getMessage(), emojiconHandler));
            }

            customView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    InstructionUtil.hideInstruction(context, R.string.instruction_open_conversation_thread);
                    ((MobiComKitActivityInterface) context).onQuickConversationFragmentItemClick(view, contactReceiver);
                }
            });

            if (contactReceiver != null && new Support(context).isSupportNumber(contactReceiver.getContactNumber()) && (!message.isTypeOutbox())) {
                contactImage.setImageResource(R.drawable.ic_launcher);
            }
            if (sentOrReceived != null) {
                if (message.isCall()) {
                    sentOrReceived.setImageResource(R.drawable.ic_action_call_holo_light);
                    messageTextView.setTextColor(context.getResources().getColor(message.isIncomingCall() ? R.color.incoming_call : R.color.outgoing_call));
                } else if (getItemViewType(position) == 0) {
                    sentOrReceived.setImageResource(R.drawable.mobicom_social_forward);
                } else {
                    sentOrReceived.setImageResource(R.drawable.mobicom_social_reply);
                }
            }
            if (createdAtTime != null) {
                createdAtTime.setText(DateUtils.getFormattedDate(message.getCreatedAtTime()));
            }
        }

        return customView;
    }

}

