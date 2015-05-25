package com.mobicomkit.uiwidgets.conversation.fragment;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.mobicomkit.api.conversation.MessageIntentService;
import com.mobicomkit.api.conversation.MobiComConversationService;
import com.mobicomkit.uiwidgets.MobiComKitApplication;
import com.mobicomkit.uiwidgets.R;
import com.mobicomkit.uiwidgets.conversation.adapter.ConversationAdapter;

public class QuickConversationFragment extends MobiComQuickConversationFragment {

    public QuickConversationFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        conversationService = new MobiComConversationService(getActivity());
        conversationAdapter = new ConversationAdapter(getActivity(),
                R.layout.mobicom_message_row_view, messageList, null, true, MessageIntentService.class, null);
    }

    @Override
    public void onResume() {
        super.onResume();

       ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(MobiComKitApplication.TITLE);
    }
}

