package com.applozic.mobicomkit.api.account.user;

/**
 * Created by Aman on 7/12/2015.
 */

import android.content.Context;
import android.os.AsyncTask;

import com.applozic.mobicomkit.api.account.register.RegisterUserClientService;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

    public interface TaskListener {
        void onSuccess(RegistrationResponse registrationResponse,Context context);

        void onFailure(RegistrationResponse registrationResponse, Exception exception);

    }

    private final TaskListener taskListener;
    private final String mUserId;
    private final String mEmail;
    private final String mPassword;
    private final String mPhoneNumber;
    private final Context context;
    private Exception mException;
    private RegistrationResponse registrationResponse;


    public UserLoginTask(User user, TaskListener listener, Context context) {
        mUserId = user.getUserId();
        mEmail = user.getEmailId();
        mPassword = user.getPassword();
        mPhoneNumber = user.getContactNumber();
        this.taskListener = listener;
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            registrationResponse = new RegisterUserClientService(context).createAccount(mEmail, mUserId, mPhoneNumber, "");
        } catch (Exception e) {
            e.printStackTrace();
            mException = e;
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean result) {
        // And if it is we call the callback function on it.
        if (result && this.taskListener != null) {
            this.taskListener.onSuccess(registrationResponse,context);


        } else if (mException != null && this.taskListener != null) {
            this.taskListener.onFailure(registrationResponse, mException);
        }
    }


}
