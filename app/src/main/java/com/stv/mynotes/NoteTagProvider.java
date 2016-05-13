package com.stv.mynotes;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class NoteTagProvider extends ContentProvider{

    private static final String AUTHORITY = "com.stv.mynotes.notetagprovider";
    private static final String BASE_PATH = "notetag";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH );
    private static final int NOTE_TAG = 1;
    private static final int NOTE_TAG_ID = 2;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final String CONTENT_ITEM_TYPE = "NoteTag";

    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH, NOTE_TAG);
        uriMatcher.addURI(AUTHORITY, BASE_PATH +  "/#", NOTE_TAG_ID);
    }

    private SQLiteDatabase database;

    @Override
    public boolean onCreate() {
        DBOpenHelper helper = new DBOpenHelper(getContext());
        database = helper.getWritableDatabase();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (uriMatcher.match(uri) == NOTE_TAG_ID) {
            selection = DBOpenHelper.NOTE_TAG_ID + "=" + uri.getLastPathSegment();
        }

        return database.query(DBOpenHelper.TABLE_NOTE_TAG, DBOpenHelper.NOTE_TAG_ALL_COLUMNS,
                selection, null, null, null,
                DBOpenHelper.NOTE_TAG_CREATED + " DESC");
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id = database.insert(DBOpenHelper.TABLE_NOTE_TAG, null, values);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return database.delete(DBOpenHelper.TABLE_NOTE_TAG, selection, selectionArgs);

    }
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return database.update(DBOpenHelper.TABLE_NOTE_TAG, values, selection, selectionArgs);
    }
}
