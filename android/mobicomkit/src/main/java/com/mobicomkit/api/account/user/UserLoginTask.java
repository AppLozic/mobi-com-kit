package com.mobicomkit.api.account.user;

/**
 * Created by Aman on 7/12/2015.
 */

import android.content.Context;
import android.os.AsyncTask;

import com.mobicomkit.api.account.register.RegisterUserClientService;

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

    public interface TaskListener {
        void onFinished(Boolean result, Exception exception);
    }

    private final TaskListener taskListener;
    private final String mUserId;
    private final String mEmail;
    private final String mPassword;
    private final String mPhoneNumber;
    private final Context context;
    private Exception mException;

    public UserLoginTask(String userId, String email, String password, String phoneNumber, TaskListener listener, Context context) {
        mUserId = userId;
        mEmail = email;
        mPassword = password;
        mPhoneNumber = phoneNumber;
        this.taskListener = listener;
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            new RegisterUserClientService(context).createAccount(mEmail, mUserId, mPhoneNumber, "");
        } catch (Exception e) {
            e.printStackTrace();
            mException = e;
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean result) {
        if (this.taskListener != null) {
            // And if it is we call the callback function on it.
            this.taskListener.onFinished(result, mException);
        }
    }
}
