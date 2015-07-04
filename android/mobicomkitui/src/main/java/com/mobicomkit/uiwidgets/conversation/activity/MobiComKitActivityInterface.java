package com.mobicomkit.uiwidgets.conversation.activity;

import android.view.View;

import com.mobicomkit.uiwidgets.conversation.fragment.ConversationFragment;

import net.mobitexter.mobiframework.people.contact.Contact;

/**
 * Created by User on 23-05-2015.
 */
public interface MobiComKitActivityInterface {

    void onQuickConversationFragmentItemClick(View view, Contact contact);

    void startContactActivityForResult();

    void addFragment(ConversationFragment conversationFragment);

}
