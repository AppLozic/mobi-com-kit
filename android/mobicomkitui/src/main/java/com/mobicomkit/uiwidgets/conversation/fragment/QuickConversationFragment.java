package com.mobicomkit.uiwidgets.conversation.fragment;

import android.os.Bundle;
import com.mobicomkit.api.conversation.MobiComConversationService;
import com.mobicomkit.uiwidgets.conversation.adapter.QuickConversationAdapter;

public class QuickConversationFragment extends MobiComQuickConversationFragment {

    public QuickConversationFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        conversationService = new MobiComConversationService(getActivity());
        conversationAdapter = new QuickConversationAdapter(getActivity(),
                messageList, null);
    }

    @Override
    public void onResume() {
        super.onResume();
     }
}

