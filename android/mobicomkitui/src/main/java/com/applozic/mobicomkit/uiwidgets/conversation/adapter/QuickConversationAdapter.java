package com.applozic.mobicomkit.uiwidgets.conversation.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.alphanumbericcolor.AlphaNumberColorUtil;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.MobiComKitActivityInterface;
import com.applozic.mobicomkit.uiwidgets.instruction.InstructionUtil;

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
public class QuickConversationAdapter extends BaseAdapter {

    private static Map<Short, Integer> messageTypeColorMap = new HashMap<Short, Integer>();
    private ImageLoader contactImageLoader;
    private Context context;
    private List<Message> messageList;
    private BaseContactService contactService;
    private EmojiconHandler emojiconHandler;
    private long deviceTimeOffset = 0;

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
        this.context = context;
        this.emojiconHandler = emojiconHandler;
        this.contactService = new AppContactService(context);
        this.messageList = messageList;
        contactImageLoader = new ImageLoader(context, ImageUtils.getLargestScreenDimension((Activity) context)) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return contactService.downloadContactImage((Activity) context, (Contact) data);
            }
        };
        contactImageLoader.setLoadingImage(R.drawable.ic_contact_picture_180_holo_light);
        contactImageLoader.addImageCache(((FragmentActivity) context).getSupportFragmentManager(), 0.1f);
        contactImageLoader.setImageFadeIn(false);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        deviceTimeOffset = MobiComUserPreference.getInstance(context).getDeviceTimeOffset();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                    Contact contact2 = contactService.getContactById(items.get(1));
                    contactInfo = TextUtils.isEmpty(contactReceiver.getFirstName()) ? contactReceiver.getContactNumber() : contactReceiver.getFirstName() + ", "
                            + (TextUtils.isEmpty(contact2.getFirstName()) ? contact2.getContactNumber() : contact2.getFirstName()) + (items.size() > 2 ? " & others" : "");
                }
                smReceivers.setText(contactInfo);
            }

            if (alphabeticTextView != null && contactReceiver != null) {
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
            if (contactReceiver.isDrawableResources()) {
                int drawableResourceId = context.getResources().getIdentifier(contactReceiver.getrDrawableName(), "drawable", context.getPackageName());
                contactImage.setImageResource(drawableResourceId);
            } else {
                contactImageLoader.loadImage(contactReceiver, contactImage, alphabeticTextView);
            }
            if (attachedFile != null) {
                attachedFile.setText("");
                attachedFile.setVisibility(View.GONE);
            }

            if (attachmentIcon != null) {
                attachmentIcon.setVisibility(View.GONE);
            }
            if (message.isSentToMany()) {
                Group group = message.getBroadcastGroupId() != null ? GroupUtils.fetchGroup(context, message.getBroadcastGroupId()) : null;
            }

            if (message.hasAttachment()) {
                //Todo: handle it for fileKeyStrings when filePaths is empty
                String filePath = message.isAttachmentDownloaded() ? message.getFilePaths().get(0) :
                        message.getFileMetas() != null ? message.getFileMetas().get(0).getName() : "";
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
                contactImage.setImageResource(R.drawable.mobicom_ic_launcher);
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
                createdAtTime.setText(DateUtils.getFormattedDate(message.getCreatedAtTime() - deviceTimeOffset));
            }
        }

        return customView;
    }


    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public Message getItem(int position) {
        return messageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public int getItemViewType(int position) {
        return getItem(position).isTypeOutbox() ? 1 : 0;
    }

}

