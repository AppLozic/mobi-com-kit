package com.applozic.mobicomkit.uiwidgets.people.contact;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AlphabetIndexer;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.commons.image.ImageLoader;
import com.applozic.mobicommons.people.OnContactsInteractionListener;
import com.applozic.mobicommons.people.SearchListFragment;
import com.applozic.mobicommons.people.contact.Contact;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by adarsh on 1/8/15.
 */

/**
 * Created by adarsh on 30/5/15.
 */
@SuppressLint("ValidFragment")
public class AppContactFragment extends ListFragment implements SearchListFragment,
        AdapterView.OnItemClickListener {

    // Defines a tag for identifying log entries
    private static final String TAG = "MobiComKitContactsListFragment";

    // Bundle key for saving previously selected search result item
    private static final String STATE_PREVIOUSLY_SELECTED_KEY =
            "net.mobitexter.mobiframework.contact.ui.SELECTED_ITEM";
    public static final String PACKAGE_TO_EXCLUDE_FOR_INVITE = "net.mobitexter";

    private ContactsAdapter mAdapter; // The main query adapter
    private ImageLoader mImageLoader; // Handles loading the contact image in a background thread
    private String mSearchTerm; // Stores the current search query term

    // Contact selected listener that allows the activity holding this fragment to be notified of
// a contact being selected
    private OnContactsInteractionListener mOnContactSelectedListener;

    // Stores the previously selected search item so that on a configuration change the same item
// can be reselected again
    private int mPreviouslySelectedSearchItem = 0;
    private BaseContactService contactService;


    private Button shareButton;
    private TextView resultTextView;

    private List<Contact> contactList;
    private boolean syncStatus = true;

    /**
     * Fragments require an empty constructor.
     */
    public AppContactFragment() {

    }

    public AppContactFragment(List<Contact> contacts) {
        this.contactList = contacts;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contactService = new AppContactService(getActivity().getApplicationContext());
        contactList = contactService.getAll();
        mAdapter = new ContactsAdapter(getActivity().getApplicationContext());

        if (savedInstanceState != null) {
            // If we're restoring state after this fragment was recreated then
            // retrieve previous search term and previously selected search
            // result.
            mSearchTerm = savedInstanceState.getString(SearchManager.QUERY);
            mPreviouslySelectedSearchItem =
                    savedInstanceState.getInt(STATE_PREVIOUSLY_SELECTED_KEY, 0);
        }

        mImageLoader = new ImageLoader(getActivity(), getListPreferredItemHeight()) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return contactService.downloadContactImage(getActivity(), (Contact) data);
            }
        };
        // Set a placeholder loading image for the image loader
        mImageLoader.setLoadingImage(R.drawable.applozic_ic_contact_picture_holo_light);
        // Add a cache to the image loader
        mImageLoader.addImageCache(getActivity().getSupportFragmentManager(), 0.1f);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the list fragment layout
        View view = inflater.inflate(R.layout.contact_list_fragment, container, false);
        shareButton = (Button) view.findViewById(R.id.actionButton);
        resultTextView = (TextView) view.findViewById(R.id.result);
        return view;
    }

    @SuppressLint("NewApi")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up ListView, assign adapter and set some listeners. The adapter was previously
        // created in onCreate().
//        if (syncStatus) {
//            resultTextView.setText(this.getResources().getString(R.string.contact_sync_in_progress));
//
//        } else {
//            resultTextView.setText(this.getResources().getString(R.string.zero_friends_on_mobitexter));
//
//        }
        shareButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String inviteMessage = getActivity().getResources().getString(R.string.invite_message);

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND)
                        .setType("text/plain").putExtra(Intent.EXTRA_TEXT, inviteMessage);

                List<Intent> targetedShareIntents = new ArrayList<Intent>();

                List<ResolveInfo> resInfo = getActivity().getPackageManager().queryIntentActivities(intent, 0);
                if (!resInfo.isEmpty()) {
                    for (ResolveInfo resolveInfo : resInfo) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        if (packageName.equals(PACKAGE_TO_EXCLUDE_FOR_INVITE)) {
                            continue;
                        }
                        Intent targetedShareIntent = new Intent(Intent.ACTION_SEND);
                        targetedShareIntent.setType("text/plain")
                                .setAction(Intent.ACTION_SEND)
                                .putExtra(Intent.EXTRA_TEXT, inviteMessage)
                                .setPackage(packageName);
                        targetedShareIntents.add(targetedShareIntent);
                    }
                    Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), "Share Via");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
                    startActivity(chooserIntent);
                }
            }
        });

        setListAdapter(mAdapter);
        getListView().setOnItemClickListener(this);
        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // Pause image loader to ensure smoother scrolling when flinging
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    mImageLoader.setPauseWork(true);
                    Utils.toggleSoftKeyBoard(getActivity(), true);
                } else {
                    mImageLoader.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
            }
        });

        // If there's a previously selected search item from a saved state then don't bother
        // initializing the loader as it will be restarted later when the query is populated into
        // the action bar search view (see onQueryTextChange() in onCreateOptionsMenu()).
        if (mPreviouslySelectedSearchItem == 0) {
            // Initialize the loader, and create a loader identified by ContactsQuery.QUERY_ID
            // getLoaderManager().initLoader(ContactsQuery.QUERY_ID, null, this);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            // Assign callback listener which the holding activity must implement. This is used
            // so that when a contact item is interacted with (selected by the user) the holding
            // activity will be notified and can take further action such as populating the contact
            // detail pane (if in multi-pane layout) or starting a new activity with the contact
            // details (single pane layout).
            mOnContactSelectedListener = (OnContactsInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnContactsInteractionListener");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // In the case onPause() is called during a fling the image loader is
        // un-paused to let any remaining background work complete.
        mImageLoader.setPauseWork(false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        mOnContactSelectedListener.onCustomContactSelected(contactList.get(position));
    }

    /**
     * Called when ListView selection is cleared, for example
     * when search mode is finished and the currently selected
     * contact should no longer be selected.
     */
    @SuppressLint("NewApi")
    private void onSelectionCleared() {
        // Uses callback to notify activity this contains this fragment
        mOnContactSelectedListener.onSelectionCleared();
        // Clears currently checked item
        getListView().clearChoices();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!TextUtils.isEmpty(mSearchTerm)) {
            // Saves the current search string
            outState.putString(SearchManager.QUERY, mSearchTerm);
        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Called when the action bar search text has changed.  Updates
        // the search filter, and restarts the loader to do a new query
        // using the new search string.
        String newFilter = !TextUtils.isEmpty(newText) ? newText : null;

        // Don't do anything if the filter is empty
        if (mSearchTerm == null && newFilter == null) {
            return true;
        }

        // Don't do anything if the new filter is the same as the current filter
        if (mSearchTerm != null && mSearchTerm.equals(newFilter)) {
            return true;
        }

        // Updates current filter to new filter
        mSearchTerm = newFilter;
        //mAdapter.indexOfSearchQuery();

//        getLoaderManager().restartLoader(
//                ContactsQuery.QUERY_ID, null, MobiTexterContactsListFragment.this);

        return true;
    }

    /**
     * Gets the preferred height for each item in the ListView, in pixels, after accounting for
     * screen density. ImageLoader uses this value to resize thumbnail images to match the ListView
     * item height.
     *
     * @return The preferred height in pixels, based on the current theme.
     */
    private int getListPreferredItemHeight() {
        final TypedValue typedValue = new TypedValue();

        // Resolve list item preferred height theme attribute into typedValue
        getActivity().getTheme().resolveAttribute(
                android.R.attr.listPreferredItemHeight, typedValue, true);

// Create a new DisplayMetrics object
        final DisplayMetrics metrics = new DisplayMetrics();

        // Populate the DisplayMetrics
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        // Return theme value based on DisplayMetrics
        return (int) typedValue.getDimension(metrics);
    }


    /**
     * This is a subclass of CursorAdapter that supports binding Cursor columns to a view layout.
     * If those items are part of search results, the search string is marked by highlighting the
     * query text. An {@link android.widget.AlphabetIndexer} is used to allow quicker navigation up and down the
     * ListView.
     */
    private class ContactsAdapter extends BaseAdapter {
        private LayoutInflater mInflater; // Stores the layout inflater
        private AlphabetIndexer mAlphabetIndexer; // Stores the AlphabetIndexer instance
        private TextAppearanceSpan highlightTextSpan; // Stores the highlight text appearance style
        Context context;
        /**
         * Instantiates a new Contacts Adapter.
         *
         * @param context A context that has access to the app's layout.
         */
        public ContactsAdapter(Context context) {
            this.context = context;
            // Stores inflater for use later
            mInflater = LayoutInflater.from(context);
            // Loads a string containing the English alphabet. To fully localize the app, provide a
            // strings.xml file in res/values-<x> directories, where <x> is a locale. In the file,
            // define a string with android:name="alphabet" and contents set to all of the
            // alphabetic characters in the language in their proper sort order, in upper case if
            // applicable.
            final String alphabet = context.getString(R.string.alphabet);

            // Instantiates a new AlphabetIndexer bound to the column used to sort contact names.
            // The cursor is left null, because it has not yet been retrieved.
            //mAlphabetIndexer = new AlphabetIndexer(null, ContactsQuery.SORT_KEY, alphabet);

            // Defines a span for highlighting the part of a display name that matches the search
            // string
            highlightTextSpan = new TextAppearanceSpan(getActivity(), R.style.searchTextHiglight);
        }

        /**
         * Identifies the start of the search string in the display name column of a Cursor row.
         * E.g. If displayName was "Adam" and search query (mSearchTerm) was "da" this would
         * return 1.
         *
         * @param displayName The contact display name.
         * @return The starting position of the search string in the display name, 0-based. The
         * method returns -1 if the string is not found in the display name, or if the search
         * string is empty or null.
         */
        private int indexOfSearchQuery(String displayName) {
            if (!TextUtils.isEmpty(mSearchTerm)) {
                return displayName.toLowerCase(Locale.getDefault()).indexOf(
                        mSearchTerm.toLowerCase(Locale.getDefault()));
            }
            return -1;
        }

        /**
         * Overrides newView() to inflate the list item views.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Inflates the list item layout.
            final View itemLayout =
                    mInflater.inflate(R.layout.contact_list_item, parent, false);

            Contact contact = contactList.get(position);
            TextView text1 = (TextView) itemLayout.findViewById(android.R.id.text1);
            TextView text2 = (TextView) itemLayout.findViewById(android.R.id.text2);
            QuickContactBadge icon = (QuickContactBadge) itemLayout.findViewById(android.R.id.icon);
            text1.setText(contact.getFullName());
            text2.setText(contact.getUserId());
            if (contact.isDrawableResources()) {
                int drawableResourceId = context.getResources().getIdentifier(contact.getrDrawableName(), "drawable", context.getPackageName());
                icon.setImageResource(drawableResourceId);
            } else {
                mImageLoader.loadImage(contact, icon);
            }

            // Returns the item layout view
            return itemLayout;
        }


        /**
         * An override of getCount that simplifies accessing the Cursor. If the Cursor is null,
         * getCount returns zero. As a result, no test for Cursor == null is needed.
         */
        @Override
        public int getCount() {
            return contactList.size();
        }

        @Override
        public Object getItem(int position) {
            return contactList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        /**
         * A class that defines fields for each resource ID in the list item layout. This allows
         * ContactsAdapter.newView() to store the IDs once, when it inflates the layout, instead of
         * calling findViewById in each iteration of bindView.
         */
        private class ViewHolder {
            TextView text1;
            TextView text2;
            QuickContactBadge icon;
        }
    }


}