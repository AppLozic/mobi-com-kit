package com.mobicomkit.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import net.mobitexter.mobiframework.commons.core.utils.DBUtils;


public class MobiComDatabaseHelper extends SQLiteOpenHelper {

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
    public static final String CREATE_SCHEDULE_SMS_TABLE = "create table " + SCHEDULE_SMS_TABLE_NAME + "( "
            + _ID + " integer primary key autoincrement  ," + SMS
            + " text not null, " + TIMESTAMP + " INTEGER ,"
            + TO_FIELD + " varchar(20) not null, " + SMS_TYPE + " varchar(20) not null ," + CONTACTID + " varchar(20) , " + SMS_KEY_STRING + " varChar(50), " + STORE_ON_DEVICE_COLUMN + " INTEGER DEFAULT 1, source INTEGER, timeToLive integer) ;";
    public static final String DB_NAME_KEY = "DB_NAME";
    public static final String DB_VERSION = "DB_VERSION";
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
    private static final String TAG = "MobiComDatabaseHelper";
    private static MobiComDatabaseHelper sInstance;
    private Context context;

    private MobiComDatabaseHelper(Context context) {
        this(context, PreferenceManager.getDefaultSharedPreferences(context).getString(DB_NAME_KEY, null), null,
                PreferenceManager.getDefaultSharedPreferences(context).getInt(DB_VERSION, 1));
        this.context = context;
    }

    public MobiComDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        init(context, name, version);
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

    public static void init(Context context, String databaseName, int databaseVersion) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(MobiComDatabaseHelper.DB_NAME_KEY, databaseName).commit();
        sharedPreferences.edit().putInt(MobiComDatabaseHelper.DB_VERSION, databaseVersion).commit();
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
        } else {
            onCreate(database);
        }
    }

    @Override
    public synchronized void close() {
        //super.close();
    }
}
