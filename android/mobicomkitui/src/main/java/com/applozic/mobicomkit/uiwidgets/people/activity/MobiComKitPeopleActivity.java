package com.applozic.mobicomkit.uiwidgets.people.activity;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.applozic.mobicomkit.uiwidgets.R;

import com.applozic.mobicomkit.uiwidgets.people.contact.AppContactFragment;
import com.applozic.mobicommons.people.OnContactsInteractionListener;
import com.applozic.mobicommons.people.contact.Contact;
import com.applozic.mobicommons.people.contact.ContactUtils;

import com.applozic.mobicommons.people.group.Group;

import java.util.Map;

public class MobiComKitPeopleActivity extends ActionBarActivity implements OnContactsInteractionListener,
        SearchView.OnQueryTextListener {

    public static final String SHARED_TEXT = "SHARED_TEXT";
    public static final String FORWARD_MESSAGE = "forwardMessage";
    protected SearchView searchView;
    protected String searchTerm;
    AppContactFragment mContactsListFragment;
    private boolean isSearchResultView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.people_activity);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.activity_contacts_title);

        mContactsListFragment = (AppContactFragment)
                getSupportFragmentManager().findFragmentById(R.id.contact_list);

        // This flag notes that the Activity is doing a search, and so the result will be
        // search results rather than all contacts. This prevents the Activity and Fragment
        // from trying to a search on search results.
        isSearchResultView = true;

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();

        String searchQuery = intent.getStringExtra(SearchManager.QUERY);
        // Set special title for search results
        String title = getString(R.string.contacts_list_search_results_title, searchQuery);
        setTitle(title);

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mContactsListFragment.onQueryTextChange(searchQuery);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(true);
        searchView.setIconified(false);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * This interface callback lets the main contacts list fragment notify
     * this activity that a contact has been selected.
     *
     * @param contactUri The contact Uri to the selected contact.
     */
    @Override
    public void onContactSelected(Uri contactUri) {
        Long contactId = ContactUtils.getContactId(getContentResolver(), contactUri);
        Map<String, String> phoneNumbers = ContactUtils.getPhoneNumbers(getApplicationContext(), contactId);

        if (phoneNumbers.isEmpty()) {
            Toast toast = Toast.makeText(this.getApplicationContext(), R.string.phone_number_not_present, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        Intent intent = new Intent();
        intent.putExtra("contactId", contactId);
        intent.setData(contactUri);
        finishActivity(intent);
    }

    public void startNewConversation(String contactNumber) {
        Intent intent = new Intent();
        intent.putExtra("userId", contactNumber);
        finishActivity(intent);
    }

    @Override
    public void onGroupSelected(Group group) {
        Intent intent = new Intent();
        intent.putExtra("groupId", group.getGroupId());
        intent.putExtra("groupName", group.getName());
        finishActivity(intent);
    }

    @Override
    public void onCustomContactSelected(Contact contact) {
        Intent intent = new Intent();
        intent.putExtra("userId", contact.getUserId());
        finishActivity(intent);
    }

    public void finishActivity(Intent intent) {
        String forwardMessage = getIntent().getStringExtra(FORWARD_MESSAGE);
        if (!TextUtils.isEmpty(forwardMessage)) {
            intent.putExtra(FORWARD_MESSAGE, forwardMessage);
        }

        String sharedText = getIntent().getStringExtra(SHARED_TEXT);
        if (!TextUtils.isEmpty(sharedText)) {
            intent.putExtra(SHARED_TEXT, sharedText);
        }

        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onSelectionCleared() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
            // For platforms earlier than Android 3.0, triggers the search activity
        } else if (i == R.id.menu_search) {// if (!Utils.hasHoneycomb()) {
            onSearchRequested();
            //}

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSearchRequested() {
        // Don't allow another search if this activity instance is already showing
        // search results. Only used pre-HC.
        return !isSearchResultView && super.onSearchRequested();
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        this.searchTerm = query;
        startNewConversation(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        this.searchTerm = query;
        mContactsListFragment.onQueryTextChange(query);
        return false;
    }

}
