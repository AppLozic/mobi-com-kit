package com.applozic.mobicomkit.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.MobiComKitClientService;

import com.applozic.mobicommons.commons.core.utils.DBUtils;


public class MobiComDatabaseHelper extends SQLiteOpenHelper {

    public static final int DB_VERSION = 2;

    public static final String _ID = "_id";
    public static final String SMS_KEY_STRING = "smsKeyString";
    public static final String STORE_ON_DEVICE_COLUMN = "storeOnDevice";
    public static final String TO_FIELD = "toField";
    public static final String SMS = "sms";
    public static final String TIMESTAMP = "timeStamp";
    public static final String SMS_TYPE = "SMSType";
    public static final String TIME_TO_LIVE = "timeToLive";
    public static final String CONTACTID = "contactId";
    public static final String SCHEDULE_SMS_TABLE_NAME = "ScheduleSMS";
    public static final String SMS_TABLE_NAME = "sms";
    public static final String CONTACT_TABLE_NAME = "contact";

    public static final String FULL_NAME = "fullName";

    public static final String CONTACT_NO = "contactNO";
    public static final String DISPLAY_NAME = "displayName";
    public static final String CONTACT_IMAGE_LOCAL_URI = "contactImageLocalURI";
    public static final String CONTACT_IMAGE_URL = "contactImageURL";
    public static final String USERID = "userId";
    public static final String EMAIL = "email";


    public static final String CREATE_SCHEDULE_SMS_TABLE = "create table " + SCHEDULE_SMS_TABLE_NAME + "( "
            + _ID + " integer primary key autoincrement  ," + SMS
            + " text not null, " + TIMESTAMP + " INTEGER ,"
            + TO_FIELD + " varchar(20) not null, " + SMS_TYPE + " varchar(20) not null ," + CONTACTID + " varchar(20) , " + SMS_KEY_STRING + " varChar(50), " + STORE_ON_DEVICE_COLUMN + " INTEGER DEFAULT 1, source INTEGER, timeToLive integer) ;";


    public static final String CREATE_SMS_TABLE = "create table sms ( "
            + "id integer primary key autoincrement, "
            + "keyString var(100), "
            + "toNumbers varchar(1000) not null, "
            + "contactNumbers varchar(2000) not null, "
            + "broadcastGroupId integer, "
            + "message text not null, "
            + "type integer, "
            + "read integer default 0, "
            + "delivered integer default 0, "
            + "storeOnDevice integer default 1, "
            + "sentToServer integer default 1, "
            + "createdAt integer, "
            + "scheduledAt integer, "
            + "source integer, "
            + "timeToLive integer, "
            + "fileMetaKeyStrings varchar(2000), "
            + "filePaths varchar(2000), "
            + "thumbnailUrl varchar(2000), "
            + "size integer, "
            + "name varchar(2000), "
            + "contentType varchar(200), "
            + "metaFileKeyString varchar(2000), "
            + "blobKeyString varchar(2000), "
            + "canceled integer default 0, "
            + "UNIQUE (keyString, contactNumbers))";

    private static final String CREATE_CONTACT_TABLE = " CREATE TABLE contact ( " +
            USERID + " VARCHAR(50) primary key, "
            + FULL_NAME + " VARCHAR(200), "
            + CONTACT_NO + " VARCHAR(15), "
            + DISPLAY_NAME + " VARCHAR(25), "
            + CONTACT_IMAGE_URL + " VARCHAR(200), "
            + CONTACT_IMAGE_LOCAL_URI + " VARCHAR(200), "
            + EMAIL + " VARCHAR(100) "
            + " ) ";

    private static final String CREATE_INDEX_SMS_TYPE = "CREATE INDEX IF NOT EXISTS INDEX_SMS_TYPE ON sms (type)";
    private static final String TAG = "MobiComDatabaseHelper";
    private static MobiComDatabaseHelper sInstance;
    private Context context;

    private MobiComDatabaseHelper(Context context) {
        this(context, "MCK_" + MobiComKitClientService.getApplicationKey(context), null, DB_VERSION);
        this.context = context;
    }

    public MobiComDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static MobiComDatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new MobiComDatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        //Store Database name in shared preference ...
        if (!DBUtils.isTableExists(database, "sms")) {
            database.execSQL(CREATE_SMS_TABLE);
        }
        if (!DBUtils.isTableExists(database, SCHEDULE_SMS_TABLE_NAME)) {
            database.execSQL(CREATE_SCHEDULE_SMS_TABLE);
        }
        if (!DBUtils.isTableExists(database, "contact")) {
            database.execSQL(CREATE_CONTACT_TABLE);
        }
        //ALL indexes should go here after creating tables.
        database.execSQL(CREATE_INDEX_SMS_TYPE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,
                          int newVersion) {
        //Note: some user might directly upgrade from an old version to the new version, in that case it may happen that
        //schedule sms table is not present.
        if (newVersion > oldVersion) {
            Log.i(TAG, "Upgrading database from version "
                    + oldVersion + " to " + newVersion
                    + ", which will destroy all old data");

            if (!DBUtils.isTableExists(database, "sms")) {
                database.execSQL(CREATE_SMS_TABLE);
            }

            if (!DBUtils.isTableExists(database, SCHEDULE_SMS_TABLE_NAME)) {
                database.execSQL(CREATE_SCHEDULE_SMS_TABLE);
            }
            database.execSQL(CREATE_INDEX_SMS_TYPE);
        } else {
            onCreate(database);
        }
    }

    @Override
    public synchronized void close() {
        //super.close();
    }

    public int delDatabase() {

        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("delete from " + SCHEDULE_SMS_TABLE_NAME);

        db.execSQL("delete from " + SMS_TABLE_NAME);

        db.execSQL("delete from " + CONTACT_TABLE_NAME);

        db.close();

        return 0;
    }
}