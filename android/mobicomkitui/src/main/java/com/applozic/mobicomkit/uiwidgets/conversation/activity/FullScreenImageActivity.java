package com.applozic.mobicomkit.uiwidgets.conversation.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.attachment.AttachmentView;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.uiwidgets.R;

import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.GsonUtils;

import java.io.File;

/**
 * Created by devashish on 22/9/14.
 */
public class FullScreenImageActivity extends ActionBarActivity {

    private View decorView;
    private Message message;

    protected void onCreate(Bundle savedInstanceState) {
        startFullScreen();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.mobicom_image_full_screen);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AttachmentView mediaImageViewView = (AttachmentView) findViewById(R.id.full_screen_image);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.full_screen_progress_bar);
        mediaImageViewView.setProressBar(progressBar);
        String messageJson = getIntent().getStringExtra(MobiComKitConstants.MESSAGE_JSON_INTENT);

        if (!TextUtils.isEmpty(messageJson)) {
            message = (Message) GsonUtils.getObjectFromJson(messageJson, Message.class);
        }

        if (message != null && message.getFilePaths() != null && !message.getFilePaths().isEmpty()) {
            mediaImageViewView.setMessage(message);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {


        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            toggleActionBar();

        }
        return true;
    }

    private void toggleActionBar() {

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            if (actionBar.isShowing()) {
                actionBar.hide();
            } else {
                actionBar.show();
            }
        }
    }

    public void startFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        } else {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
            );
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.attachment_menu, menu);

        // Return true to display menu
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.shareOptions) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            Uri uri = Uri.fromFile(new File(message.getFilePaths().get(0)));
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.setType(FileUtils.getMimeType(new File(message.getFilePaths().get(0))));
            startActivity(Intent.createChooser(shareIntent, ""));
        } else if (i == R.id.forward) {
            Intent intent = new Intent();
            intent.putExtra(MobiComKitConstants.MESSAGE_JSON_INTENT, GsonUtils.getJsonFromObject(message, Message.class));
            setResult(RESULT_OK, intent);
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
