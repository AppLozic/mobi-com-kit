package com.mobicomkit.sample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mobicomkit.api.account.user.MobiComUserPreference;
import com.mobicomkit.api.conversation.Message;
import com.mobicomkit.contact.AppContactService;
import com.mobicomkit.database.MobiComDatabaseHelper;
import com.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.mobicomkit.uiwidgets.conversation.UIService;
import com.mobicomkit.uiwidgets.conversation.activity.MobiComActivityForFragment;
import com.mobicomkit.uiwidgets.conversation.activity.SlidingPaneActivity;
import com.mobicomkit.uiwidgets.conversation.fragment.ConversationFragment;

import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.people.contact.Contact;


public class MainActivity extends MobiComActivityForFragment
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, EcommerceFragment.OnFragmentInteractionListener {

    public static final String DATABASE_NAME = "yourappdb";
    public static final String MOBICOMKIT = "applozic.connect";
    public static final String USER_ID = "userId";
    public static final String TAKE_ORDER = "takeOrder";
    public static final String TAKE_ORDER_USERID_METADATA = "com.mobicomkit.take.order.userId";
    public static final int DATABASE_VERSION = 1;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */

    private CharSequence mTitle;

    public MainActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        buildSupportContactData();
        MobiComUserPreference userPreference = MobiComUserPreference.getInstance(this);
        if (!userPreference.isRegistered()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return;
        }

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments

        /*if (position == 1) {
            Intent intent = new Intent(this, SlidingPaneActivity.class);
            startActivity(intent);
            return;
        }*/
        if (position == 1) {
            Intent intent = new Intent(this, ConversationActivity.class);
            startActivity(intent);
            return;
        }/*
        if (position == 1) {
            ConversationFragment conversationFragment = new ConversationFragment();
            Contact contact = new Contact(this, "mobicomkit");
            mTitle = getString(R.string.user_id);
            addFragment(this, conversationFragment, "conversationFragment");
            conversationFragment.loadConversation(contact);
            return;

        }*/
        if (position == 0) {
            mTitle = getString(R.string.ecommerce);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, EcommerceFragment.newInstance("", ""))
                    .commit();
            return;
        }

        if (position == 2) {

            Toast.makeText(getBaseContext(), "Logging out", Toast.LENGTH_SHORT).show();

            MobiComUserPreference userPreference = MobiComUserPreference.getInstance(this);

            MobiComDatabaseHelper databaseHelper = MobiComDatabaseHelper.getInstance(this);

            databaseHelper.delDatabase();

            boolean flag = userPreference.clearAll();

            if (flag) {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                return;
            }
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void startChat(View v) {
        Intent chatIntent = new Intent(this, SlidingPaneActivity.class);
        chatIntent.putExtra(USER_ID, MOBICOMKIT);
        startActivity(chatIntent);
    }

    public void takeOrder(View v) {
        Intent takeOrderIntent = new Intent(this, ConversationActivity.class);
        takeOrderIntent.putExtra(TAKE_ORDER, true);
        takeOrderIntent.putExtra(USER_ID, Utils.getMetaDataValue(this, TAKE_ORDER_USERID_METADATA));
        startActivity(takeOrderIntent);
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
           /* case 2:
                mTitle = getString(R.string.title_section2);
                break;*/
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void processLocation() {

    }

    @Override
    public void startContactActivityForResult() {

    }

    @Override
    public void addFragment(ConversationFragment conversationFragment) {
        addFragment(this, conversationFragment, "conversationFragment");
    }

    @Override
    public void startContactActivityForResult(Message message, String messageContent) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void updateLatestMessage(Message message, String formattedContactNumber) {
        new ConversationUIService(this).updateLatestMessage(message, formattedContactNumber);


    }

    @Override
    public void removeConversation(Message message, String formattedContactNumber) {

        new ConversationUIService(this).removeConversation(message, formattedContactNumber);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }

    }

    public static void addFragment(FragmentActivity fragmentActivity, Fragment fragmentToAdd, String fragmentTag) {
        FragmentManager supportFragmentManager = fragmentActivity.getSupportFragmentManager();

        Fragment activeFragment = UIService.getActiveFragment(fragmentActivity);
        FragmentTransaction fragmentTransaction = supportFragmentManager
                .beginTransaction();
        if (null != activeFragment) {
            fragmentTransaction.hide(activeFragment);
        }

        fragmentTransaction.replace(R.id.container, fragmentToAdd,
                fragmentTag);

        if (supportFragmentManager.getBackStackEntryCount() > 1) {
            supportFragmentManager.popBackStack();
        }
        fragmentTransaction.addToBackStack(fragmentTag);
        fragmentTransaction.commit();
        supportFragmentManager.executePendingTransactions();
        //Log.i(TAG, "BackStackEntryCount: " + supportFragmentManager.getBackStackEntryCount());
    }

    private void buildSupportContactData() {
        Context context = getApplicationContext();
        AppContactService appContactService = new AppContactService(context);
        // avoid each time update ....
        if (appContactService.getContactById(getString(R.string.support_contact_userId)) == null) {
            Contact contact = new Contact();
            contact.setUserId(getString(R.string.support_contact_userId));
            contact.setFullName(getString(R.string.support_contact_display_name));
            contact.setContactNumber(getString(R.string.support_contact_number));
            contact.setImageURL(getString(R.string.support_contact_image_url));
            contact.setEmailId(getString(R.string.support_contact_emailId));
            appContactService.add(contact);
        }
    }

}
