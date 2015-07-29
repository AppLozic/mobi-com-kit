package com.applozic.mobicomkit.uiwidgets.conversation.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.UserClientService;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MessageIntentService;
import com.applozic.mobicomkit.api.conversation.MobiComMessageService;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.uiwidgets.MobiComKitApplication;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.MobiComKitBroadcastReceiver;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.ConversationFragment;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.MobiComQuickConversationFragment;
import com.applozic.mobicomkit.uiwidgets.instruction.InstructionUtil;

import com.applozic.mobicommons.commons.core.utils.Support;

import com.applozic.mobicomkit.uiwidgets.people.activity.MobiComKitPeopleActivity;


/**
 * Created by devashish on 8/3/14.
 */

//http://gmariotti.blogspot.in/2013/05/working-with-slidingpanelayout.html
public class SlidingPaneActivity extends MobiComActivity {

    public static final int RESULT_OK = -1;
    private static final String TAG = "SlidingPaneActivity";
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static int RESULT_LOAD_IMAGE = 1;
    protected GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private ProgressDialog resProgressBar;

    @Override
    protected void onStop() {
        /*if (mLocationClient != null) {
            mLocationClient.disconnect();
        }*/
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        title = MobiComKitApplication.TITLE;
        HOME_BUTTON_ENABLED = true;
        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(this);

        setContentView(R.layout.sliding_pane);

        mActionBar = getSupportActionBar();
        slidingPaneLayout = (SlidingPaneLayout) findViewById(R.id.sliding_pane_layout);

        slidingPaneLayout.setPanelSlideListener(new SliderListener());
        slidingPaneLayout.openPane();

        slidingPaneLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                new FirstLayoutListener());

        quickConversationFragment = (MobiComQuickConversationFragment) getSupportFragmentManager().findFragmentById(R.id.quick_conversation_fragment_pane);
        conversationFragment = (ConversationFragment) getSupportFragmentManager().findFragmentById(R.id.conversation_fragment_pane);

        mobiComKitBroadcastReceiver = new MobiComKitBroadcastReceiver(quickConversationFragment, conversationFragment);
        InstructionUtil.showInfo(this, R.string.info_message_sync, BroadcastService.INTENT_ACTIONS.INSTRUCTION.toString());

        SharedPreferences prefs = getSharedPreferences(MobiComKitClientService.getApplicationKey(this), Context.MODE_PRIVATE);
        if (prefs.getBoolean(UserClientService.SHARED_PREFERENCE_VERSION_UPDATE_KEY, false)) {
            new UserClientService(this).updateCodeVersion(userPreferences.getDeviceKeyString());
            prefs.edit().remove(UserClientService.SHARED_PREFERENCE_VERSION_UPDATE_KEY).commit();
        }

        /*googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();*/

        checkForStartNewConversation(getIntent());

        /*Contact contact = new Contact();
        contact.setUserId("devashish.mamgain");
        openConversationFragment(contact);*/

       /* AppRater appRater = new AppRater();
        appRater.appLaunched(this);*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      /*
       * The action bar up action should open the slider if it is currently
       * closed, as the left pane contains content one level up in the
       * navigation hierarchy.
       */
        if (item.getItemId() == android.R.id.home && !slidingPaneLayout.isOpen()) {
            slidingPaneLayout.openPane();
            return true;
        }

        //Note:  using if-else and not switch as resource constants in library projects are not final
        Intent intent;
        Support support = new Support(this);
        int i = item.getItemId();
        if (i == R.id.start_new) {
            startContactActivityForResult();
        } else if (i == R.id.dial) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + (support.isSupportNumber(currentOpenedContactNumber) ? MobiComKitClientService.getApplicationKey(this) : currentOpenedContactNumber)));
            startActivity(callIntent);
        } else if (i == R.id.shareOptions) {
            intent = new Intent(Intent.ACTION_SEND);
            String textToShare = this.getResources().getString(R.string.invite_message);
            intent.setAction(Intent.ACTION_SEND)
                    .setType("text/plain").putExtra(Intent.EXTRA_TEXT, textToShare);
            startActivity(Intent.createChooser(intent, "Share Via"));
        } else if (i == R.id.refresh) {
            String message = this.getString(R.string.info_message_sync);
            new MobiComMessageService(this, MessageIntentService.class).syncMessagesWithServer(message);
        } else if (i == R.id.support) {
            openConversationFragment(support.getSupportContact());
        } else if (i == R.id.deleteConversation) {
            conversationFragment.deleteConversationThread();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mobicom_basic_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void startContactActivityForResult() {
        startContactActivityForResult(null, null);
    }

    @Override
    public void addFragment(ConversationFragment conversationFragment) {

    }

    @Override
    public void startContactActivityForResult(Message message, String messageContent) {
        Intent intent = new Intent(this, MobiComKitPeopleActivity.class);
        super.startContactActivityForResult(intent, message, messageContent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == LOCATION_SERVICE_ENABLE) {
            if (((LocationManager) getSystemService(Context.LOCATION_SERVICE))
                    .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                googleApiClient.connect();
            } else {
                Toast.makeText(SlidingPaneActivity.this, R.string.unable_to_fetch_location, Toast.LENGTH_LONG).show();
            }
            return;
        } /*else if (resultCode == MultimediaOptionFragment.REQUEST_CODE_ATTACH_PHOTO ||
                resultCode == MultimediaOptionFragment.REQUEST_CODE_TAKE_PHOTO && intent != null) {*/
    }

    public void processLocation() {

    }

    void showErrorDialog(int code) {
        GooglePlayServicesUtil.getErrorDialog(code, this,
                CONNECTION_FAILURE_RESOLUTION_REQUEST).show();
    }

}

