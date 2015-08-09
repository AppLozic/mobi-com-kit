package com.applozic.mobicomkit.uiwidgets.conversation.fragment;

import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.applozic.mobicomkit.api.conversation.MessageIntentService;
import com.applozic.mobicomkit.api.conversation.MobiComConversationService;
import com.applozic.mobicomkit.uiwidgets.MobiComKitApplication;
import com.applozic.mobicomkit.uiwidgets.R;

import com.applozic.mobicommons.commons.core.utils.LocationUtils;

public class ConversationFragment extends MobiComConversationFragment {

    private static final String TAG = "ConversationFragment";

    public ConversationFragment() {
        this.messageIntentClass = MessageIntentService.class;
    }

    public void attachLocation(Location mCurrentLocation) {
        String address = LocationUtils.getAddress(getActivity(), mCurrentLocation);
        if (!TextUtils.isEmpty(address)) {
            address = "Address: " + address + "\n";
        } else {
            address = "";
        }
        messageEditText.setText(address + "http://maps.google.com/?q=" + mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.title = MobiComKitApplication.TITLE;
        this.conversationService = new MobiComConversationService(getActivity());
        hideExtendedSendingOptionLayout = true;

        View view = super.onCreateView(inflater, container, savedInstanceState);

        sendType.setSelection(1);

        messageEditText.setHint(R.string.enter_mt_message_hint);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.secret_message_timer_array, R.layout.mobiframework_custom_spinner);

        adapter.setDropDownViewResource(R.layout.mobiframework_custom_spinner);

        attachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (attachmentLayout.getVisibility() == View.VISIBLE) {
                    Toast.makeText(getActivity(), R.string.select_file_count_limit, Toast.LENGTH_LONG).show();
                    return;
                }

                multimediaOptionFragment.show(getActivity().getSupportFragmentManager(), R.array.multimediaOptions_mt);
            }
        });

        return view;
    }

    @Override
    protected void processMobiTexterUserCheck() {

    }

    public void updateTitle() {
        //((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(MobiComKitApplication.TITLE);
        super.updateTitle();
    }

}