package com.applozic.mobicomkit.uiwidgets.conversation.adapter;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.attachment.AttachmentManager;
import com.applozic.mobicomkit.api.attachment.AttachmentView;
import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.api.attachment.FileMeta;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MobiComConversationService;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.alphanumbericcolor.AlphaNumberColorUtil;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.FullScreenImageActivity;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.MobiComKitActivityInterface;
import com.applozic.mobicomkit.uiwidgets.instruction.InstructionUtil;

import com.applozic.mobicommons.commons.core.utils.ContactNumberUtils;
import com.applozic.mobicommons.commons.core.utils.DateUtils;
import com.applozic.mobicommons.commons.core.utils.Support;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.commons.image.ImageLoader;
import com.applozic.mobicommons.commons.image.ImageUtils;
import com.applozic.mobicommons.emoticon.EmojiconHandler;
import com.applozic.mobicommons.emoticon.EmoticonUtils;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.contact.Contact;
import com.applozic.mobicommons.people.contact.ContactUtils;
import com.applozic.mobicommons.people.group.Group;
import com.applozic.mobicommons.people.group.GroupUtils;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ConversationAdapter extends ArrayAdapter<Message> {
    private static final int FILE_THRESOLD_SIZE = 400;
    private static Map<Short, Integer> messageTypeColorMap = new HashMap<Short, Integer>();
    private ImageLoader contactImageLoader;
    private Context context;
    private Contact contact;
    private Group group;
    private boolean individual;
    private boolean quick;
    private Drawable sentIcon;
    private Drawable deliveredIcon;
    private Drawable pendingIcon;
    private Drawable scheduledIcon;
    private ImageLoader imageThumbnailLoader;
    private TextView downloadSizeTextView;
    private EmojiconHandler emojiconHandler;
    private FileClientService fileClientService;
    private MessageDatabaseService messageDatabaseService;
    private BaseContactService contactService;


    static {
        messageTypeColorMap.put(Message.MessageType.INBOX.getValue(), R.color.message_type_inbox);
        messageTypeColorMap.put(Message.MessageType.OUTBOX.getValue(), R.color.message_type_outbox);
        messageTypeColorMap.put(Message.MessageType.OUTBOX_SENT_FROM_DEVICE.getValue(), R.color.message_type_outbox_sent_from_device);
        messageTypeColorMap.put(Message.MessageType.MT_INBOX.getValue(), R.color.message_type_mt_inbox);
        messageTypeColorMap.put(Message.MessageType.MT_OUTBOX.getValue(), R.color.message_type_mt_outbox);
        messageTypeColorMap.put(Message.MessageType.CALL_INCOMING.getValue(), R.color.message_type_incoming_call);
        messageTypeColorMap.put(Message.MessageType.CALL_OUTGOING.getValue(), R.color.message_type_outgoing_call);
    }

    private Class<?> messageIntentClass;
    private MobiComConversationService conversationService;

    public ConversationAdapter(final Context context, int textViewResourceId, List<Message> messageList, Group group, Class messageIntentClass, EmojiconHandler emojiconHandler) {
        this(context, textViewResourceId, messageList, null, group, false, messageIntentClass, emojiconHandler);
    }

    public ConversationAdapter(final Context context, int textViewResourceId, List<Message> messageList, Contact contact, boolean quick, Class messageIntentClass, EmojiconHandler emojiconHandler) {
        this(context, textViewResourceId, messageList, contact, null, quick, messageIntentClass, emojiconHandler);
    }

    public ConversationAdapter(final Context context, int textViewResourceId, List<Message> messageList, Contact contact, Group group, boolean quick, Class messageIntentClass, EmojiconHandler emojiconHandler) {
        super(context, textViewResourceId, messageList);
        this.messageIntentClass = messageIntentClass;
        this.context = context;
        this.contact = contact;
        this.group = group;
        this.emojiconHandler = emojiconHandler;
        this.contactService = new AppContactService(context);
        this.individual = (contact != null || group != null);
        this.quick = quick;
        this.fileClientService = new FileClientService(context);
        this.messageDatabaseService = new MessageDatabaseService(context);
        this.conversationService = new MobiComConversationService(context);
        contactImageLoader = new ImageLoader(getContext(), ImageUtils.getLargestScreenDimension((Activity) getContext())) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return ContactUtils.loadContactPhoto((Uri) data, getImageSize(), (Activity) getContext());
            }
        };
        imageThumbnailLoader = new ImageLoader(getContext(), ImageUtils.getLargestScreenDimension((Activity) getContext())) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return fileClientService.loadThumbnailImage(getContext(), (FileMeta) data, getImageLayoutParam(false).height, getImageLayoutParam(false).width);
            }
        };
        imageThumbnailLoader.setImageFadeIn(false);
        contactImageLoader.setLoadingImage(R.drawable.ic_contact_picture_180_holo_light);
        contactImageLoader.addImageCache(((FragmentActivity) context).getSupportFragmentManager(), 0.1f);

        contactImageLoader.setImageFadeIn(false);
        imageThumbnailLoader.addImageCache(((FragmentActivity) context).getSupportFragmentManager(), 0.1f);

        sentIcon = getContext().getResources().getDrawable(R.drawable.ic_action_message_sent);
        deliveredIcon = getContext().getResources().getDrawable(R.drawable.ic_action_message_delivered);
        pendingIcon = getContext().getResources().getDrawable(R.drawable.ic_action_message_pending);
        scheduledIcon = getContext().getResources().getDrawable(R.drawable.ic_action_message_schedule);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View customView;

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (!quick) {
            if (getItemViewType(position) == 0) {
                customView = inflater.inflate(R.layout.mobicom_received_message_list_view, parent, false);
            } else {
                customView = inflater.inflate(R.layout.mobicom_sent_message_list_view, parent, false);
            }
        } else {
            customView = inflater.inflate(R.layout.mobicom_message_row_view, parent, false);
            TextView smTime = (TextView) customView.findViewById(R.id.smTime);
            smTime.setVisibility(View.GONE);
        }
        final Message message = getItem(position);

        if (quick && message.isSentToMany()) {
            group = message.getBroadcastGroupId() != null ? GroupUtils.fetchGroup(context, message.getBroadcastGroupId()) : null;
        }

        List<String> items = Arrays.asList(message.getTo().split("\\s*,\\s*"));
        List<String> userIds = null;
        if (!TextUtils.isEmpty(message.getContactIds())) {
            userIds = Arrays.asList(message.getContactIds().split("\\s*,\\s*"));
        }
        final Contact contactReceiver;
        if (group != null) {
            contactReceiver = null;
        } else if (individual) {
            contactReceiver = contact;
            contact.setContactNumber(items.get(0));
            if (userIds != null) {
                contact.setUserId(userIds.get(0));
            }
            contact.setFormattedContactNumber(ContactNumberUtils.getPhoneNumber(items.get(0), MobiComUserPreference.getInstance(context).getCountryCode()));
        } else {
            contactReceiver = contactService.getContactReceiver(items, userIds);
        }

        if (message != null) {
            View messageTextLayout = customView.findViewById(R.id.messageTextLayout);
            TextView smReceivers = (TextView) customView.findViewById(R.id.smReceivers);
            TextView createdAtTime = (TextView) customView.findViewById(R.id.createdAtTime);
            TextView messageTextView = (TextView) customView.findViewById(R.id.message);
            ImageView contactImage = (ImageView) customView.findViewById(R.id.contactImage);
            TextView alphabeticTextView = (TextView) customView.findViewById(R.id.alphabeticImage);
            ImageView sentOrReceived = (ImageView) customView.findViewById(R.id.sentOrReceivedIcon);
            TextView deliveryStatus = (TextView) customView.findViewById(R.id.status);
            TextView selfDestruct = (TextView) customView.findViewById(R.id.selfDestruct);
            final ImageView preview = (ImageView) customView.findViewById(R.id.preview);
            final AttachmentView attachmentView = (AttachmentView) customView.findViewById(R.id.main_attachment_view);
            TextView attachedFile = (TextView) customView.findViewById(R.id.attached_file);
            final ImageView attachmentIcon = (ImageView) customView.findViewById(R.id.attachmentIcon);
            downloadSizeTextView = (TextView) customView.findViewById(R.id.attachment_size_text);
            final LinearLayout attachmentDownloadLayout = (LinearLayout) customView.findViewById(R.id.attachment_download_layout);
            final LinearLayout attachmentRetry = (LinearLayout) customView.findViewById(R.id.attachment_retry_layout);
            final RelativeLayout attachmentDownloadProgressLayout = (RelativeLayout) customView.findViewById(R.id.attachment_download_progress_layout);
            final RelativeLayout mainAttachmentLayout = (RelativeLayout) customView.findViewById(R.id.attachment_preview_layout);
            final ProgressBar mediaDownloadProgressBar = (ProgressBar) customView.findViewById(R.id.media_download_progress_bar);
            final ProgressBar mediaUploadProgressBar = (ProgressBar) customView.findViewById(R.id.media_upload_progress_bar);
            if (attachedFile != null) {
                attachedFile.setText("");
                attachedFile.setVisibility(View.GONE);
            }

            if (attachmentIcon != null) {
                attachmentIcon.setVisibility(View.GONE);
            }

            if (!quick) {
                attachmentDownloadLayout.setVisibility(View.GONE);
                preview.setVisibility(message.hasAttachment() ? View.VISIBLE : View.GONE);
                attachmentView.setVisibility(View.GONE);
                if (message.isTypeOutbox() && !message.isCanceled()) {
                    mediaUploadProgressBar.setVisibility(message.isAttachmentUploadInProgress() ? View.VISIBLE : View.GONE);
                } else {
                    mediaUploadProgressBar.setVisibility(View.GONE);
                }
                attachedFile.setVisibility(message.hasAttachment() ? View.VISIBLE : View.GONE);
                //Todo: show progress for download image of type inbox
            }

            /*if (quick) {
                TextView unreadSmsTxtView = (TextView) customView.findViewById(R.id.unreadSmsCount);
                int count = messageDatabaseService.getUnreadSmsCount(contactReceiver);
                if (count == 0) {
                    unreadSmsTxtView.setVisibility(View.GONE);
                } else {
                    unreadSmsTxtView.setVisibility(View.VISIBLE);
                    unreadSmsTxtView.setText(((Integer) count).toString());
                }
            }*/

            if (individual && message.getTimeToLive() != null) {
                selfDestruct
                        .setText("Self destruct in " + message.getTimeToLive() + " mins");
                selfDestruct.setVisibility(View.VISIBLE);
            } else {
                selfDestruct.setText("");
                selfDestruct.setVisibility(View.GONE);
            }

            //messageTextView.setTextColor(quick || !smListItem.isTypeOutbox() ? Color.BLACK : Color.WHITE);
            //messageTextView.setTextColor(Color.BLACK);
            if (attachedFile != null) {
                attachedFile.setTextColor(Color.BLACK);
            }

            if (sentOrReceived != null) {
                if ((!quick && !message.isCall()) || message.isDummyEmptyMessage()) {
                    sentOrReceived.setVisibility(View.GONE);
                } else if (message.isCall()) {
                    sentOrReceived.setImageResource(R.drawable.ic_action_call_holo_light);
                } else if (getItemViewType(position) == 0) {
                    sentOrReceived.setImageResource(R.drawable.mobicom_social_forward);
                } else {
                    sentOrReceived.setImageResource(R.drawable.mobicom_social_reply);
                }

                if (quick && message.isCall()) {
                    messageTextView.setTextColor(context.getResources().getColor(message.isIncomingCall() ? R.color.incoming_call : R.color.outgoing_call));
                }
            }

            if (message.isCall() || message.isDummyEmptyMessage()) {
                createdAtTime.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            } else if (!quick && message.getKeyString() == null && message.isTypeOutbox()) {
                createdAtTime.setCompoundDrawablesWithIntrinsicBounds(null, null, message.getScheduledAt() != null ? scheduledIcon : pendingIcon, null);
            } else if (!quick && message.isTypeOutbox()) {
                createdAtTime.setCompoundDrawablesWithIntrinsicBounds(null, null, message.getDelivered() || (contact != null && new Support(context).isSupportNumber(contact.getFormattedContactNumber())) ? deliveredIcon : (message.getScheduledAt() != null ? scheduledIcon : sentIcon), null);
            }

            if (message.isCall()) {
                deliveryStatus.setText("");
            } else if (message.getType().equals(Message.MessageType.MT_OUTBOX.getValue()) || message.getType().equals(Message.MessageType.MT_INBOX.getValue())) {
                deliveryStatus.setText("via MT");
            } else {
                deliveryStatus.setText("via Carrier");
            }

            if (contactReceiver != null) {
                if (quick) {
                    String contactInfo = contactReceiver.getDisplayName();
                    if (items.size() > 1) {
                        Contact contact2 = contactService.getContactById(items.get(1));
                        contactInfo = TextUtils.isEmpty(contactReceiver.getFirstName()) ? contactReceiver.getContactNumber() : contactReceiver.getFirstName() + ", "
                                + (TextUtils.isEmpty(contact2.getFirstName()) ? contact2.getContactNumber() : contact2.getFirstName()) + (items.size() > 2 ? " & others" : "");
                    }
                    smReceivers.setText(contactInfo);
                }

                Uri contactUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(contactReceiver.getContactId()));

                if (!quick && message.isTypeOutbox()) {
                    Uri imageUri = Utils.getUserImageUri(context);
                    if (imageUri != null) {
                        contactImage.setImageURI(imageUri);
                    }
                } else if (contactReceiver != null && new Support(context).isSupportNumber(contactReceiver.getContactNumber()) && (quick || !message.isTypeOutbox())) {
                    contactImage.setImageResource(R.drawable.ic_launcher);
                } else {
                    contactImageLoader.loadImage(contactUri, contactImage, alphabeticTextView);
                }

                if (alphabeticTextView != null) {
                    String contactNumber = contactReceiver.getContactNumber().toUpperCase();
                    char firstLetter = contactReceiver.getDisplayName().toUpperCase().charAt(0);
                    if (firstLetter != '+') {
                        alphabeticTextView.setText(String.valueOf(firstLetter));
                    } else if (contactNumber.length() >= 2) {
                        alphabeticTextView.setText(String.valueOf(contactNumber.charAt(1)));
                    }

                    Character colorKey = AlphaNumberColorUtil.alphabetBackgroundColorMap.containsKey(firstLetter) ? firstLetter : null;
                    alphabeticTextView.setTextColor(context.getResources().getColor(AlphaNumberColorUtil.alphabetTextColorMap.get(colorKey)));
                    if (quick) {
                        GradientDrawable drawable = (GradientDrawable) alphabeticTextView.getBackground();
                        drawable.setColor(context.getResources().getColor(AlphaNumberColorUtil.alphabetBackgroundColorMap.get(colorKey)));
                    } else {
                        alphabeticTextView.setBackgroundResource(AlphaNumberColorUtil.alphabetBackgroundColorMap.get(colorKey));
                    }
                }
            } else if (quick && group != null) {
                smReceivers.setText(group.getName());
            }

            if (!quick) {

                if (message.hasAttachment()) {
                    mainAttachmentLayout.setLayoutParams(getImageLayoutParam(false));
                    if (message.getFileMetas() != null && message.getFileMetas().get(0).getContentType().contains("image")) {
                        attachedFile.setVisibility(View.GONE);
                    }
                    if (message.isAttachmentDownloaded()) {
                        preview.setVisibility(View.GONE);
                        String[] filePaths = new String[message.getFilePaths().size()];
                        int i = 0;
                        for (final String filePath : message.getFilePaths()) {
                            filePaths[i++] = filePath;
                            final String mimeType = FileUtils.getMimeType(filePath);
                            if (mimeType != null && mimeType.startsWith("image")) {
                                attachmentView.setMessage(message);
                                attachmentView.setProressBar(mediaDownloadProgressBar);
                                attachmentView.setDownloadProgressLayout(attachmentDownloadProgressLayout);
                                attachmentView.setVisibility(View.VISIBLE);
                                attachedFile.setVisibility(View.GONE);
                            } else {
                                showAttachmentIconAndText(attachedFile, filePath, mimeType);
                            }
                        }
                    } else if (message.isAttachmentUploadInProgress()) {
                        //showPreview(smListItem, preview, attachmentDownloadLayout);
                        attachmentDownloadProgressLayout.setVisibility(View.VISIBLE);
                        attachmentView.setProressBar(mediaDownloadProgressBar);
                        attachmentView.setDownloadProgressLayout(attachmentDownloadProgressLayout);
                        attachmentView.setMessage(message);
                        attachmentView.setVisibility(View.VISIBLE);
                    } else if (AttachmentManager.isAttachmentInProgress(message.getKeyString())) {
                        showPreview(message, preview, attachmentDownloadLayout);
                        FileMeta fileMeta = message.getFileMetas().get(0);
                        final String mimeType = FileUtils.getMimeType(fileMeta.getName());
                        if (!fileMeta.getContentType().contains("image")) {
                            showAttachmentIconAndText(attachedFile, fileMeta.getName(), mimeType);
                        }
                        attachmentView.setDownloadProgressLayout(attachmentDownloadProgressLayout);
                        attachmentDownloadProgressLayout.setVisibility(View.VISIBLE);
                    } else {
                        String[] fileKeys = new String[message.getFileMetaKeyStrings().size()];
                        int i = 0;
                        showPreview(message, preview, attachmentDownloadLayout);
                        //TODO: while doing multiple image support in single sms ...we might improve this
                        for (String fileKey : message.getFileMetaKeyStrings()) {
                            fileKeys[i++] = fileKey;
                            if (message.getFileMetas() != null) {
                                FileMeta fileMeta = message.getFileMetas().get(0);
                                attachmentDownloadLayout.setVisibility(View.VISIBLE);
                                attachmentDownloadProgressLayout.setVisibility(View.GONE);
                                downloadSizeTextView.setText(fileMeta.getSizeInReadableFormat());
                                final String mimeType = FileUtils.getMimeType(fileMeta.getName());
                                if (!fileMeta.getContentType().contains("image")) {
                                    showAttachmentIconAndText(attachedFile, fileMeta.getName(), mimeType);
                                }

                            }

                        }

                    }

                }
                if (message.isCanceled()) {
                    attachmentRetry.setVisibility(View.VISIBLE);
                }
                attachmentRetry.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(context, "Resending attachment....", Toast.LENGTH_LONG).show();
                        mediaUploadProgressBar.setVisibility(View.VISIBLE);
                        attachmentRetry.setVisibility(View.GONE);
                        //updating Cancel Flag to smListItem....
                        message.setCanceled(false);
                        messageDatabaseService.updateCanceledFlag(message.getMessageId(), 0);
                        conversationService.sendMessage(message, messageIntentClass);
                    }
                });
                attachmentDownloadProgressLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        attachmentView.setVisibility(View.GONE);
                        attachmentView.cancelDownload();
                        attachmentDownloadLayout.setVisibility(View.VISIBLE);
                        attachmentDownloadProgressLayout.setVisibility(View.GONE);
                        message.setAttDownloadInProgress(false);
                    }
                });

                //final ProgressBar mediaDownloadProgressBar = mediaDownloadProgressBar;
                preview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO: 1. get the image Size and decide if we can download directly
                        //2. if already downloaded to ds card show it directly ....
                        //3. if deleted from sd crad only ...ask user to download it again or skip ...
                        if (message.isAttachmentDownloaded()) {
                            showFullView(message);
                        } else {
                            attachmentDownloadLayout.setVisibility(View.GONE);
                            attachmentView.setProressBar(mediaDownloadProgressBar);
                            attachmentView.setDownloadProgressLayout(attachmentDownloadProgressLayout);
                            attachmentView.setMessage(message);
                            attachmentView.setVisibility(View.VISIBLE);
                            attachmentDownloadProgressLayout.setVisibility(View.VISIBLE);
                        }

                    }
                });
                attachmentView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFullView(message);
                    }
                });
            }

            if (message.getScheduledAt() != null) {
                createdAtTime.setText(DateUtils.getFormattedDate(message.getScheduledAt()));
            } else if (createdAtTime != null && message.isDummyEmptyMessage()) {
                createdAtTime.setText("");
            } else if (createdAtTime != null) {
                createdAtTime.setText(DateUtils.getFormattedDate(message.getCreatedAtTime()));
            }

            String mimeType = "";

            if (messageTextView != null) {
                messageTextView.setText(EmoticonUtils.getSmiledText(context, message.getMessage(), emojiconHandler));
                if (quick && message.hasAttachment() && message.getFilePaths() != null &&
                        !message.getFilePaths().isEmpty()) {
                    //Todo: handle it for fileKeyStrings when filePaths is empty
                    String filePath = message.getFilePaths().get(0);
                    mimeType = FileUtils.getMimeType(filePath);
                    attachmentIcon.setVisibility(View.VISIBLE);
                    messageTextView.setText(filePath.substring(filePath.lastIndexOf("/") + 1) + " " + messageTextView.getText());
                }

                if (mimeType != null) {
                    if (mimeType.startsWith("image")) {
                        attachmentIcon.setImageResource(R.drawable.ic_action_camera);
                    } else if (mimeType.startsWith("video")) {
                        attachmentIcon.setImageResource(R.drawable.ic_action_video);
                    }
                }
                if (!quick && messageTextLayout != null) {
                    messageTextLayout.setBackgroundResource(messageTypeColorMap.get(message.getType()));
                    if (message.hasAttachment()) {
                        messageTextLayout.setLayoutParams(getImageLayoutParam(message.isTypeOutbox()));
                        //messageTextLayout.setBackgroundResource(R.drawable.send_sms_background);
                        customView.findViewById(R.id.messageTextInsideLayout).setBackgroundResource(R.color.attachment_background_color);
                    }
                }
            }

        }

        if (quick) {
            customView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    InstructionUtil.hideInstruction(context, R.string.instruction_open_conversation_thread);
                    ((MobiComKitActivityInterface) context).onQuickConversationFragmentItemClick(view, contactReceiver);
                }
            });
        }

        return customView;
    }

    private void showAttachmentIconAndText(TextView attachedFile, final String filePath, final String mimeType) {
        attachedFile.setVisibility(View.VISIBLE);
        attachedFile.setText(filePath.substring(filePath.lastIndexOf("/") + 1));
        attachedFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                File file = new File(filePath);
                intent.setDataAndType(Uri.fromFile(file), mimeType);
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, R.string.info_app_not_found_to_open_file, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showPreview(Message smListItem, ImageView preview, LinearLayout attachmentDownloadLayout) {
        FileMeta fileMeta = smListItem.getFileMetas().get(0);
        imageThumbnailLoader.setImageFadeIn(false);
        imageThumbnailLoader.setLoadingImage(R.id.media_upload_progress_bar);
        imageThumbnailLoader.loadImage(fileMeta, preview);
        attachmentDownloadLayout.setVisibility(View.GONE);
    }

    private void showFullView(Message smListItem) {
        Intent intent = new Intent(context, FullScreenImageActivity.class);
        intent.putExtra(MobiComKitConstants.MESSAGE_JSON_INTENT, GsonUtils.getJsonFromObject(smListItem, Message.class));
        ((MobiComKitActivityInterface) context).startActivityForResult(intent, MobiComKitActivityInterface.REQUEST_CODE_FULL_SCREEN_ACTION);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).isTypeOutbox() ? 1 : 0;
    }


    public ViewGroup.LayoutParams getImageLayoutParam(boolean outBoxType) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        float wt_px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, context.getResources().getDisplayMetrics());
        ViewGroup.MarginLayoutParams params;
        if (outBoxType) {
            params = new RelativeLayout.LayoutParams(metrics.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins((int) wt_px, 0, (int) wt_px, 0);
        } else {
            params = new LinearLayout.LayoutParams(metrics.widthPixels - (int) wt_px * 2, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 0);

        }
        return params;
    }
}
