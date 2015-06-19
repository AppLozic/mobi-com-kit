package com.mobicomkit.api;

import android.content.Context;

import net.mobitexter.mobiframework.commons.core.utils.Utils;

/**
 * Created by devashish on 22/12/14.
 */
public class MobiComKitServer {


    public static final boolean PROD = true;
    public static final String PROD_DISPLAY_URL = "http://www.mobicomkit.com";
    public static final String BASE_URL_METADATA = "com.mobicomkit.server.url";
    public static String baseUrl;
    public static final String CREATE_ACCOUNT_URL = baseUrl + "/rest/ws/registration/v1/register";
    public static final String CHECK_FOR_MT_USER = baseUrl + "/rest/ws/contact/v2/ismtexter";
    public static final String PHONE_NUMBER_UPDATE_URL = baseUrl + "/rest/ws/registration/phone/number/update";
    public static final String FILE_UPLOAD_URL = baseUrl + "/rest/ws/file/url";
    public static final String NOTIFY_CONTACTS_ABOUT_JOINING_MT = baseUrl + "/rest/ws/registration/notify/contacts";
    public static final String VERIFICATION_CONTACT_NUMBER_URL = baseUrl + "/rest/ws/verification/number";
    public static final String VERIFICATION_CODE_CONTACT_NUMBER_URL = baseUrl + "/rest/ws/verification/code";
    public static final String MTEXT_DELIVERY_URL = baseUrl + "/rest/ws/sms/mtext/delivered?";
    public static final String APP_VERSION_UPDATE_URL = baseUrl + "/rest/ws/registration/version/update";
    public static final String SETTING_UPDATE_URL = baseUrl + "/rest/ws/setting/single/update";
    public static final String TIMEZONE_UPDATAE_URL = baseUrl + "/rest/ws/setting/updateTZ";
    public static final String SERVER_SYNC_URL = baseUrl + "/rest/ws/mobicomkit/sync/messages";
    public static final String SEND_MESSAGE_URL = baseUrl + "/rest/ws/mobicomkit/v1/message/add";
    public static final String UPDATE_DELIVERY_FLAG_URL = baseUrl + "/rest/ws/sms/update/delivered";
    public static final String MESSAGE_STAT_URL = baseUrl + "/rest/ws/sms/stat/update";
    public static final String FILE_URL = baseUrl + "/rest/ws/file/";
    public static final String SYNC_SMS_URL = baseUrl + "/rest/ws/sms/add/batch";
    public static final String MESSAGE_LIST_URL = baseUrl + "/rest/ws/mobicomkit/v1/message/list";
    public static final String GOOGLE_CONTACT_URL = baseUrl + "/rest/ws/user/session/contact/google/list";
    public static final String PLATFORM_CONTACT_URL = baseUrl + "/rest/ws/user/session/contact/google/list";
    public static final String MESSAGE_THREAD_DELETE_URL = baseUrl + "/rest/ws/mobicomkit/v1/message/delete/conversation.task";
    public static final String MESSAGE_DELETE_URL = baseUrl + "/rest/ws/mobicomkit/v1/message/delete";
    public static final String SERVER_DEVICE_CONTACT_SYNC_URL = baseUrl + "/rest/ws/contact/v1/device/add";
    public static final String SERVER_CONTACT_SYNC_URL = baseUrl + "/rest/ws/contact/v1/add";
    public static final String FREE_MESSAGE_FAILED_URL = baseUrl + "/rest/ws/sms/mtext/failed?";
    public static final String CONTACT_SYNC_COMPLETE_URL = baseUrl + "/rest/ws/contact/syncCompleted?suUserKeyString";
    public static final Short MOBICOMKIT_VERSION_CODE = 71;
    public static final String ARGUMRNT_SAPERATOR = "&";
    //Todo: Fix this url.
    public static final String APP_SERVER_URL = "xxx";
    //public static final String ERROR_BASE_URL = "http://onlinesmsutility.appspot.com";
    public static final String ERROR_BASE_URL = "https://osu-alpha.appspot.com";
    public static final String SUBMIT_ERROR_URL = ERROR_BASE_URL + "/rest/ws/error/submit";
    public static String APPLICATION_KEY_HEADER = "Application-Key";
    public static String APPLICATION_KEY_HEADER_VALUE_METADATA = "com.mobicomkit.application.id";
    public static String SUPPORT_PHONE_NUMBER_METADATA = "com.mobicomkit.support.phone.number";
//    public static String APPLICATION_KEY_HEADER_VALUE = "c";

    public MobiComKitServer(Context context) {
        this.baseUrl = Utils.getMetaDataValue(context, BASE_URL_METADATA);

    }
}
