package com.stv.mynotes;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper{

    //Constants for db name and version
    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 1;

    //Constants for identifying table and columns
    public static final String TABLE_NOTES = "notes";
    public static final String NOTE_ID = "_id";
    public static final String NOTE_TEXT = "noteText";
    public static final String NOTE_CREATED = "noteCreated";
    public static final String NOTE_EDITED = "noteEdited";
    public static final String NOTE_TITLE  = "noteTitle";
    //TAG
    public static final String TABLE_TAGS = "tags";
    public static final String TAG_ID = "_id";
    public static final String TAG_IDENTIFIER = "tagIdentifier";
    public static final String TAG_CREATED = "tagCreated";
    //NOTE_TAG
    private static final String TABLE_NOTE_TAG = "note_tag";
    private static final String NOTE_TAG_ID = "noteTagId";
    private static final String KEY_NOTE_ID = "note_id";
    private static final String KEY_TAG_ID = "tag_id";
    private static final String NOTE_TAG_CREATED = "noteTagCreated";



    public static final String[] ALL_COLUMNS =
            {NOTE_ID, NOTE_TEXT, NOTE_CREATED, NOTE_TITLE, NOTE_EDITED};

    //SQL to create table
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NOTES + " (" +
                    NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    NOTE_TITLE + " TEXT, " +
                    NOTE_TEXT + " TEXT, " +
                    NOTE_EDITED + " TEXT default CURRENT_TIMESTAMP, " +
                    NOTE_CREATED + " TEXT default CURRENT_TIMESTAMP" +
                    ")";
    private static final String TABLE_CREATE_TAGS =
            "CREATE TABLE " + TABLE_TAGS + " (" +
                    TAG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TAG_IDENTIFIER + " TEXT unique, " +
                    TAG_CREATED + " TEXT default CURRENT_TIMESTAMP" +
                    ")";
    private static final String TABLE_CREATE_NOTE_TAG=
            "CREATE TABLE " + TABLE_NOTE_TAG + " (" +
                    NOTE_TAG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_TAG_ID + " TEXT," +
                    KEY_NOTE_ID + " TEXT," +
                    NOTE_TAG_CREATED + " TEXT default CURRENT_TIMESTAMP" +
                    ")";



    public DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
        db.execSQL(TABLE_CREATE_TAGS);
        db.execSQL(TABLE_CREATE_NOTE_TAG);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTE_TAG);
        onCreate(db);
    }
}
