package com.applozic.mobicomkit.uiwidgets.conversation;

import android.os.AsyncTask;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MobiComConversationService;

import com.applozic.mobicommons.people.contact.Contact;

/**
 * Created by devashish on 9/2/15.
 */
public class DeleteConversationAsyncTask extends AsyncTask<Void, Integer, Long> {

    private Message message;
    private Contact contact;
    private MobiComConversationService conversationService;

    public DeleteConversationAsyncTask(MobiComConversationService conversationService, Message message, Contact contact) {
        this.message = message;
        this.contact = contact;
        this.conversationService = conversationService;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Long doInBackground(Void... params) {
        conversationService.deleteMessage(message, contact);
        return null;
    }
}
