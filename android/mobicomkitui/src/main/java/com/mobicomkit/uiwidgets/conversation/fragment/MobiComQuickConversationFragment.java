package com.mobicomkit.uiwidgets.conversation.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mobicomkit.api.conversation.Message;
import com.mobicomkit.api.conversation.MessageIntentService;
import com.mobicomkit.api.conversation.MobiComConversationService;
import com.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.mobicomkit.broadcast.BroadcastService;
import com.mobicomkit.uiwidgets.R;
import com.mobicomkit.uiwidgets.conversation.ConversationListView;
import com.mobicomkit.uiwidgets.conversation.activity.MobiComActivity;
import com.mobicomkit.uiwidgets.conversation.adapter.ConversationAdapter;
import com.mobicomkit.uiwidgets.instruction.InstructionUtil;
import com.shamanland.fab.FloatingActionButton;

import net.mobitexter.mobiframework.commons.core.utils.Utils;
import net.mobitexter.mobiframework.people.contact.Contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by devashish on 10/2/15.
 */
public class MobiComQuickConversationFragment extends Fragment {

    public static final String QUICK_CONVERSATION_EVENT = "quick_conversation";
    protected MobiComConversationService conversationService;

    protected ConversationListView listView = null;
    protected FloatingActionButton fabButton;
    protected TextView emptyTextView;
    protected Button startNewButton;
    protected ProgressBar spinner;
    protected SwipeRefreshLayout swipeLayout;
    protected int listIndex;

    protected Map<String, Message> latestSmsForEachContact = new HashMap<String, Message>();
    protected List<Message> messageList = new ArrayList<Message>();
    protected ConversationAdapter conversationAdapter = null;

    protected boolean loadMore = true;
    private Long minCreatedAtTime;

    public ConversationListView getListView() {
        return listView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        conversationService = new MobiComConversationService(getActivity());
        conversationAdapter = new ConversationAdapter(getActivity(),
                R.layout.mobicom_message_row_view, messageList, null, true, MessageIntentService.class, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View list = inflater.inflate(R.layout.mobicom_message_list, container, false);
        listView = (ConversationListView) list.findViewById(R.id.messageList);
        listView.setScrollToBottomOnSizeChange(Boolean.FALSE);

        fabButton = (FloatingActionButton) list.findViewById(R.id.fab_start_new);
        fabButton.setVisibility(View.VISIBLE);

        LinearLayout individualMessageSendLayout = (LinearLayout) list.findViewById(R.id.individual_message_send_layout);
        LinearLayout extendedSendingOptionLayout = (LinearLayout) list.findViewById(R.id.extended_sending_option_layout);

        individualMessageSendLayout.setVisibility(View.GONE);
        extendedSendingOptionLayout.setVisibility(View.GONE);

        View spinnerLayout = inflater.inflate(R.layout.mobicom_message_list_header_footer, null);
        listView.addFooterView(spinnerLayout);

        spinner = (ProgressBar) spinnerLayout.findViewById(R.id.spinner);
        emptyTextView = (TextView) spinnerLayout.findViewById(R.id.noConversations);
        startNewButton = (Button) spinnerLayout.findViewById(R.id.start_new_conversation);

        swipeLayout = (SwipeRefreshLayout) list.findViewById(R.id.swipe_container);
        swipeLayout.setEnabled(false);

        return list;
    }

    protected View.OnClickListener startNewConversation() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MobiComActivity) getActivity()).startContactActivityForResult();
            }
        };
    }

    public void addMessage(final Message message) {
        final Context context = getActivity();
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                message.processContactIds(context);
                Message recentMessage = latestSmsForEachContact.get(message.getContactIds());
                if (recentMessage != null && message.getCreatedAtTime() >= recentMessage.getCreatedAtTime()) {
                    messageList.remove(recentMessage);
                } else if (recentMessage != null) {
                    return;
                }

                latestSmsForEachContact.put(message.getContactIds(), message);
                messageList.add(0, message);
                conversationAdapter.notifyDataSetChanged();
                //listView.smoothScrollToPosition(messageList.size());
                listView.setSelection(0);
                emptyTextView.setVisibility(View.GONE);
                startNewButton.setVisibility(View.GONE);
            }
        });
    }

    public void updateLastMessage(String keyString, String formattedContactNumber) {
        for (Message message : messageList) {
            if (message.getKeyString() != null && message.getKeyString().equals(keyString)) {
                MessageDatabaseService messageDatabaseService = new MessageDatabaseService(getActivity());
                List<Message> lastMessage = messageDatabaseService.getLatestMessage(formattedContactNumber);
                if (lastMessage.isEmpty()) {
                    removeConversation(message, formattedContactNumber);
                } else {
                    deleteMessage(lastMessage.get(0), formattedContactNumber);
                }
                break;
            }
        }
    }

    public String getLatestContact() {
        if (messageList != null && !messageList.isEmpty()) {
            Message message = messageList.get(0);
            return message.getTo();
        }
        return null;
    }

    public void deleteMessage(final Message message, final String formattedContactNumber) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Message recentMessage = latestSmsForEachContact.get(formattedContactNumber);
                if (recentMessage != null && message.getCreatedAtTime() <= recentMessage.getCreatedAtTime()) {
                    latestSmsForEachContact.put(formattedContactNumber, message);
                    messageList.set(messageList.indexOf(recentMessage), message);

                    conversationAdapter.notifyDataSetChanged();
                    if (messageList.isEmpty()) {
                        emptyTextView.setVisibility(View.VISIBLE);
                        startNewButton.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    public void updateLatestMessage(Message message, String formattedContactNumber) {
        deleteMessage(message, formattedContactNumber);
    }

    public void removeConversation(final Message message, final String formattedContactNumber) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                latestSmsForEachContact.remove(formattedContactNumber);
                messageList.remove(message);
                conversationAdapter.notifyDataSetChanged();
                checkForEmptyConversations();
            }
        });
    }

    public void removeConversation(final Contact contact) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Message message = latestSmsForEachContact.get(contact.getFormattedContactNumber());
                messageList.remove(message);
                latestSmsForEachContact.remove(contact.getFormattedContactNumber());
                conversationAdapter.notifyDataSetChanged();
                checkForEmptyConversations();
            }
        });
    }

    public void checkForEmptyConversations() {
        if (latestSmsForEachContact.isEmpty() && spinner.getVisibility() == View.GONE) {
            emptyTextView.setVisibility(View.VISIBLE);
            startNewButton.setVisibility(View.VISIBLE);
        } else {
            emptyTextView.setVisibility(View.GONE);
            startNewButton.setVisibility(View.GONE);
        }
    }

    public void setLoadMore(boolean loadMore) {
        this.loadMore = loadMore;
    }

    @Override
    public void onPause() {
        super.onPause();
        listIndex = listView.getFirstVisiblePosition();
        BroadcastService.currentUserId =null;
    }

    @Override
    public void onResume() {
        //Assigning to avoid notification in case if quick conversation fragment is opened....
        BroadcastService.currentUserId ="quick-conversation";
        super.onResume();
        if (listView != null) {
            if (listView.getCount() > listIndex) {
                listView.setSelection(listIndex);
            } else {
                listView.setSelection(0);
            }
        }
        downloadConversations();
        ((ActionBarActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //FlurryAgent.logEvent(QUICK_CONVERSATION_EVENT);
        listView.setAdapter(conversationAdapter);
        startNewButton.setOnClickListener(startNewConversation());
        fabButton.setOnClickListener(startNewConversation());

        //listView.setOnTouchListener(new ShowHideOnScroll(fabButton));

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0 && loadMore) {
                    loadMore = false;
                    new DownloadConversation(view, false, firstVisibleItem, visibleItemCount, totalItemCount).execute();
                }
            }
        });
    }

    public void downloadConversations() {
        downloadConversations(false);
    }

    public void downloadConversations(boolean showInstruction) {
        minCreatedAtTime = null;
        new DownloadConversation(listView, true, 1, 0, 0, showInstruction).execute();
    }

    public class DownloadConversation extends AsyncTask<Void, Integer, Long> {

        private AbsListView view;
        private int firstVisibleItem;
        private int amountVisible;
        private int totalItems;
        private boolean initial;
        private boolean showInstruction;
        private List<Message> nextMessageList = new ArrayList<Message>();
        private Context context;

        public DownloadConversation(AbsListView view, boolean initial, int firstVisibleItem, int amountVisible, int totalItems, boolean showInstruction) {
            this.context = getActivity();
            this.view = view;
            this.initial = initial;
            this.firstVisibleItem = firstVisibleItem;
            this.amountVisible = amountVisible;
            this.totalItems = totalItems;
            this.showInstruction = showInstruction;
        }

        public DownloadConversation(AbsListView view, boolean initial, int firstVisibleItem, int amountVisible, int totalItems) {
            this(view, initial, firstVisibleItem, amountVisible, totalItems, false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadMore = false;
            spinner.setVisibility(View.VISIBLE);
        }

        protected Long doInBackground(Void... voids) {
            if (initial) {
                nextMessageList = conversationService.getLatestMessagesGroupByPeople();
                if (!nextMessageList.isEmpty()) {
                    minCreatedAtTime = nextMessageList.get(nextMessageList.size() - 1).getCreatedAtTime();
                }
            } else if (!messageList.isEmpty()) {
                listIndex = firstVisibleItem;
                Long createdAt = messageList.isEmpty() ? null : messageList.get(messageList.size() - 1).getCreatedAtTime();
                minCreatedAtTime = (minCreatedAtTime == null ? createdAt : Math.min(minCreatedAtTime, createdAt));
                nextMessageList = conversationService.getLatestMessagesGroupByPeople(minCreatedAtTime);
            }

            return 0L;
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(Long result) {
            for (Message currentMessage : nextMessageList) {
                if (currentMessage.isSentToMany()) {
                    continue;
                }
                Message recentSms = latestSmsForEachContact.get(currentMessage.getContactIds());
                if (recentSms != null) {
                    if (currentMessage.getCreatedAtTime() >= recentSms.getCreatedAtTime()) {
                        latestSmsForEachContact.put(currentMessage.getContactIds(), currentMessage);
                        messageList.remove(recentSms);
                        messageList.add(currentMessage);
                    }
                } else {
                    latestSmsForEachContact.put(currentMessage.getContactIds(), currentMessage);
                    messageList.add(currentMessage);
                }
            }

            conversationAdapter.notifyDataSetChanged();
            if (initial) {
                emptyTextView.setVisibility(messageList.isEmpty() ? View.VISIBLE : View.GONE);
                startNewButton.setVisibility(messageList.isEmpty() ? View.VISIBLE : View.GONE);
                if (!messageList.isEmpty()) {
                    listView.setSelection(0);
                }
            } else {
                listView.setSelection(firstVisibleItem);
            }
            spinner.setVisibility(View.GONE);
            String errorMessage = getResources().getString(R.string.internet_connection_not_available);
            Utils.isNetworkAvailable(getActivity(), errorMessage);
            loadMore = !nextMessageList.isEmpty();

            if (context != null && showInstruction) {
                InstructionUtil.showInstruction(context, R.string.instruction_open_conversation_thread, MobiComActivity.INSTRUCTION_DELAY, BroadcastService.INTENT_ACTIONS.INSTRUCTION.toString());
            }
        }
    }

}
