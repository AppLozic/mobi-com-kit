package com.applozic.mobicomkit.uiwidgets.conversation.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.uiwidgets.R;

import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicommons.file.FileUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: shai
 * Date: 6/15/14
 * Time: 12:02 PM
 */
public class MultimediaOptionFragment extends DialogFragment {
    public static final int RESULT_OK = -1;
    public static final int REQUEST_CODE_TAKE_PHOTO = 11;
    public static final int REQUEST_CODE_ATTACH_PHOTO = 12;

    private Uri capturedImageUri;
    private int menuOptionsResourceId = R.array.multimediaOptions_sms;

    public MultimediaOptionFragment() {

    }

    public Uri getCapturedImageUri() {
        return capturedImageUri;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        capturedImageUri = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(menuOptionsResourceId, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 1:
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        // Ensure that there's a camera activity to handle the intent
                        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                            // Create the File where the photo should go
                            File photoFile;

                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                            String imageFileName = "JPEG_" + timeStamp + "_" + ".jpeg";

                            photoFile = FileClientService.getFilePath(imageFileName, getActivity(), "image/jpeg");

                            // Continue only if the File was successfully created
                            if (photoFile != null) {
                                capturedImageUri = Uri.fromFile(photoFile);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
                                getActivity().startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
                            }
                        }
                        break;
                    case 2:
                        Intent getContentIntent = FileUtils.createGetContentIntent();
                        getContentIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                        Intent intentPick = Intent.createChooser(getContentIntent, getString(R.string.select_file));
                        getActivity().startActivityForResult(intentPick, REQUEST_CODE_ATTACH_PHOTO);
                        break;
                    case 0:
                        ((ConversationActivity) getActivity()).processLocation();
                        break;
                    default:
                }
            }
        });
        return builder.create();
    }

    public void show(FragmentManager supportFragmentManager, int resourceId) {
        this.menuOptionsResourceId = resourceId;
        show(supportFragmentManager, "Attachment options");
    }
}
