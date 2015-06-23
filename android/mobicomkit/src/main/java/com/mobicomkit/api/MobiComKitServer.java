package com.mobicomkit.api;

import android.content.Context;

import net.mobitexter.mobiframework.commons.core.utils.Utils;

/**
 * Created by devashish on 22/12/14.
 */
public class MobiComKitServer {

    private Context context;
    public MobiComKitServer(Context context) {
        this.context = context;
    }

    public static final boolean PROD = true;
    public static final String PROD_DISPLAY_URL = "http://www.mobicomkit.com";
    public static final String BASE_URL_METADATA = "com.mobicomkit.server.url";
    public static final String CREATE_ACCOUNT_URL = "/rest/ws/registration/v1/register";
    public static final String CHECK_FOR_MT_USER =  "/rest/ws/contact/v2/ismtexter";
    public static final String PHONE_NUMBER_UPDATE_URL = "/rest/ws/registration/phone/number/update";
    public static final String FILE_UPLOAD_URL =  "/rest/ws/file/url";
    public static final String NOTIFY_CONTACTS_ABOUT_JOINING_MT =  "/rest/ws/registration/notify/contacts";
    public static final String VERIFICATION_CONTACT_NUMBER_URL =  "/rest/ws/verification/number";
    public static final String VERIFICATION_CODE_CONTACT_NUMBER_URL =  "/rest/ws/verification/code";
    public static final String MTEXT_DELIVERY_URL =  "/rest/ws/sms/mtext/delivered?";
    public static final String APP_VERSION_UPDATE_URL =  "/rest/ws/registration/version/update";
    public static final String SETTING_UPDATE_URL = "/rest/ws/setting/single/update";
    public static final String TIMEZONE_UPDATAE_URL = "/rest/ws/setting/updateTZ";
    public static final String SERVER_SYNC_URL =  "/rest/ws/mobicomkit/sync/messages";
    public static final String SEND_MESSAGE_URL = "/rest/ws/mobicomkit/v1/message/add";
    public static final String UPDATE_DELIVERY_FLAG_URL = "/rest/ws/sms/update/delivered";
    public static final String MESSAGE_STAT_URL =  "/rest/ws/sms/stat/update";
    public static final String FILE_URL =  "/rest/ws/file/";
    public static final String SYNC_SMS_URL = "/rest/ws/sms/add/batch";
    public static final String MESSAGE_LIST_URL = "/rest/ws/mobicomkit/v1/message/list";
    public static final String GOOGLE_CONTACT_URL = "/rest/ws/user/session/contact/google/list";
    public static final String PLATFORM_CONTACT_URL = "/rest/ws/user/session/contact/google/list";
    public static final String MESSAGE_THREAD_DELETE_URL =  "/rest/ws/mobicomkit/v1/message/delete/conversation.task";
    public static final String MESSAGE_DELETE_URL = "/rest/ws/mobicomkit/v1/message/delete";
    public static final String SERVER_DEVICE_CONTACT_SYNC_URL =  "/rest/ws/contact/v1/device/add";
    public static final String SERVER_CONTACT_SYNC_URL = "/rest/ws/contact/v1/add";
    public static final String FREE_MESSAGE_FAILED_URL = "/rest/ws/sms/mtext/failed?";
    public static final String CONTACT_SYNC_COMPLETE_URL =  "/rest/ws/contact/syncCompleted?suUserKeyString";
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

    public String getBaseUrl() {
        return Utils.getMetaDataValue(context, BASE_URL_METADATA);
    }

    public String getCreateAccountUrl() {
        return getBaseUrl() + CREATE_ACCOUNT_URL;
    }

    public String getCheckForMtUser(){
        return getBaseUrl() + CHECK_FOR_MT_USER;
    }

    public String getPhoneNumberUpdateUrl(){
        return getBaseUrl() + PHONE_NUMBER_UPDATE_URL;
    }

    public String getFileUploadUrl(){
        return getBaseUrl() + FILE_UPLOAD_URL;
    }

    public  String getNotifyContactsAboutJoiningMt() {
        return getBaseUrl() + NOTIFY_CONTACTS_ABOUT_JOINING_MT;
    }

    public  String getVerificationContactNumberUrl() {
        return getBaseUrl() + VERIFICATION_CONTACT_NUMBER_URL;
    }


    public  String getVerificationCodeContactNumberUrl() {
        return getBaseUrl() + VERIFICATION_CODE_CONTACT_NUMBER_URL;
    }


    public  String getMtextDeliveryUrl() {
        return getBaseUrl() + MTEXT_DELIVERY_URL;
    }


    public  String getAppVersionUpdateUrl() {
        return getBaseUrl() + APP_VERSION_UPDATE_URL;
    }

    public  String getSettingUpdateUrl() {
        return getBaseUrl() + SETTING_UPDATE_URL;
    }

    public  String getTimezoneUpdataeUrl() {
        return getBaseUrl() + TIMEZONE_UPDATAE_URL;
    }

    public  String getServerSyncUrl() {
        return  getBaseUrl() + SERVER_SYNC_URL;
    }

    public  String getSendMessageUrl() {
        return getBaseUrl() + SEND_MESSAGE_URL;
    }

    public  String getUpdateDeliveryFlagUrl() {
        return getBaseUrl() +  UPDATE_DELIVERY_FLAG_URL;
    }

    public  String getMessageStatUrl() {
        return getBaseUrl() + MESSAGE_STAT_URL;
    }

    public  String getFileUrl() {
        return getBaseUrl() + FILE_URL;
    }

    public  String getSyncSmsUrl() {
        return getBaseUrl() + SYNC_SMS_URL;
    }

    public  String getMessageListUrl() {
        return getBaseUrl() + MESSAGE_LIST_URL;
    }


    public  String getGoogleContactUrl() {
        return getBaseUrl() + GOOGLE_CONTACT_URL;
    }

    public  String getPlatformContactUrl() {
        return getBaseUrl() + PLATFORM_CONTACT_URL;
    }


    public  String getMessageThreadDeleteUrl() {
        return  getBaseUrl() + MESSAGE_THREAD_DELETE_URL;
    }

    public  String getMessageDeleteUrl() {
        return getBaseUrl() + MESSAGE_DELETE_URL;
    }

}
