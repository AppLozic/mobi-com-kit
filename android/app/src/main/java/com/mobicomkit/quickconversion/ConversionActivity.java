package com.mobicomkit.quickconversion;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mobicomkit.api.conversation.Message;
import com.mobicomkit.api.conversation.MessageIntentService;
import com.mobicomkit.api.conversation.MobiComMessageService;
import com.mobicomkit.broadcast.BroadcastService;
import com.mobicomkit.sample.R;
import com.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.mobicomkit.uiwidgets.conversation.MessageCommunicator;
import com.mobicomkit.uiwidgets.conversation.MobiComKitBroadcastReceiverForFragments;
import com.mobicomkit.uiwidgets.conversation.UIService;
import com.mobicomkit.uiwidgets.conversation.activity.MobiComKitActivityInterface;
import com.mobicomkit.uiwidgets.conversation.fragment.ConversationFragment;
import com.mobicomkit.uiwidgets.conversation.fragment.QuickConversationFragment;
import com.mobicomkit.uiwidgets.instruction.InstructionUtil;

import net.mobitexter.mobiframework.people.contact.Contact;


/**
 * Created by devashish on 6/25/2015.
 */
public class ConversionActivity extends ActionBarActivity implements MessageCommunicator, MobiComKitActivityInterface {

    protected ConversationFragment conversation;
    protected QuickConversationFragment quickConversationFragment;
    protected MobiComKitBroadcastReceiverForFragments mobiComKitBroadcastReceiver;
    protected ActionBar mActionBar;
    FragmentActivity fragmentActivity;
    public static final String TAKE_ORDER = "takeOrder";

    public ConversionActivity() {

    }

    public ConversionActivity(FragmentActivity fragmentActivity) {
        this.fragmentActivity = fragmentActivity;
    }

    public static void addFragment(FragmentActivity fragmentActivity, Fragment fragmentToAdd, String fragmentTag) {
        FragmentManager supportFragmentManager = fragmentActivity.getSupportFragmentManager();

        Fragment activeFragment = UIService.getActiveFragment(fragmentActivity);
        FragmentTransaction fragmentTransaction = supportFragmentManager
                .beginTransaction();
        if (null != activeFragment) {
            fragmentTransaction.hide(activeFragment);
        }

        fragmentTransaction.replace(R.id.layout_child_activity, fragmentToAdd,
                fragmentTag);

        if (supportFragmentManager.getBackStackEntryCount() > 1) {
            supportFragmentManager.popBackStack();
        }
        fragmentTransaction.addToBackStack(fragmentTag);
        fragmentTransaction.commit();
        supportFragmentManager.executePendingTransactions();
        //Log.i(TAG, "BackStackEntryCount: " + supportFragmentManager.getBackStackEntryCount());
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerMobiTexterBroadcastReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mobiComKitBroadcastReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActionBar = getSupportActionBar();

        setContentView(R.layout.quickconversion_activity);
        quickConversationFragment = new QuickConversationFragment();
        conversation = new ConversationFragment();

        addFragment(this, quickConversationFragment, "QuickConversationFragment");

        mobiComKitBroadcastReceiver = new MobiComKitBroadcastReceiverForFragments(this);
        InstructionUtil.showInfo(this, R.string.info_message_sync, BroadcastService.INTENT_ACTIONS.INSTRUCTION.toString());

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) finish();
            }
        });
        mActionBar.setTitle(R.string.conversations);

        new ConversationUIService(this).checkForStartNewConversation(getIntent());
    }

    protected void registerMobiTexterBroadcastReceiver() {
        registerReceiver(mobiComKitBroadcastReceiver, BroadcastService.getIntentFilter());
    }

    private void showActionBar() {
        mActionBar.setDisplayShowTitleEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mobicom_basic_menu_for_normal_message, menu);
        showActionBar();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        new ConversationUIService(this).onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.start_new) {
            new ConversationUIService(this).startContactActivityForResult();
        } else if (id == R.id.refresh) {
            String message = this.getString(R.string.info_message_sync);
            new MobiComMessageService(this, MessageIntentService.class).syncMessagesWithServer(message);
        } else if (id == R.id.shareOptions) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            String textToShare = this.getResources().getString(R.string.invite_message);
            intent.setAction(Intent.ACTION_SEND)
                    .setType("text/plain").putExtra(Intent.EXTRA_TEXT, textToShare);
            startActivity(Intent.createChooser(intent, "Share Via"));
            return super.onOptionsItemSelected(item);
        } else if (id == R.id.deleteConversation) {
            conversation.deleteConversationThread();
        }
        return false;
    }

    @Override
    public void onQuickConversationFragmentItemClick(View view, Contact contact) {

        addFragment(this, conversation, "Conversation");
        conversation.loadConversation(contact);
    }

    @Override
    public void startContactActivityForResult() {
        new ConversationUIService(this).startContactActivityForResult();
    }

    @Override
    public void addFragment(ConversationFragment conversationFragment) {
        addFragment(this, conversationFragment, "ConversationFragment");
    }

    @Override
    public void onBackPressed() {
        Boolean takeOrder = getIntent().getBooleanExtra(TAKE_ORDER, false);
        if (takeOrder)
            this.finish();
        else
            super.onBackPressed();
    }

    @Override
    public void updateLatestMessage(Message message, String formattedContactNumber) {
        new ConversationUIService(this).updateLatestMessage(message, formattedContactNumber);

    }

    @Override
    public void removeConversation(Message message, String formattedContactNumber) {
        new ConversationUIService(this).removeConversation(message, formattedContactNumber);
    }
}
