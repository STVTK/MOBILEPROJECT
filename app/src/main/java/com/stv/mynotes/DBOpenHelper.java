package com.stv.mynotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.stv.mynotes.Entity.Note;
import com.stv.mynotes.Entity.Tag;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DBOpenHelper extends SQLiteOpenHelper{

    private static final String LOG = "DBOpenHelper";

    //Constants for db name and version
    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 1;

    //Constants for identifying table and columns
    //COMMON
    public static final String KEY_ID = "_id";
    public static final String DATE_TIME_CREATED = "DATE TIME CREATED";

    //NOTES
    public static final String TABLE_NOTES = "notes";
    public static final String NOTE_TITLE  = "noteTitle";
    public static final String NOTE_TEXT = "noteText";
    public static final String NOTE_EDITED = "noteEdited";
    //TAGS
    public static final String TABLE_TAGS = "tags";
    public static final String TAG_IDENTIFIER = "tagIdentifier";
    //TAG_NOTE
    private static final String TABLE_NOTE_TAG = "note_tag";
    private static final String KEY_NOTE_ID = "note_id";
    private static final String KEY_TAG_ID = "tag_id";


    public static final String[] NOTES_ALL_COLUMNS =
            {KEY_ID, NOTE_TEXT, DATE_TIME_CREATED, NOTE_TITLE, NOTE_EDITED};

    //SQL to create table
    private static final String TABLE_CREATE_NOTES =
            "CREATE TABLE " + TABLE_NOTES + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    NOTE_TITLE + " TEXT, " +
                    NOTE_TEXT + " TEXT, " +
                    NOTE_EDITED + " TEXT default CURRENT_TIMESTAMP, " +
                    DATE_TIME_CREATED + " TEXT default CURRENT_TIMESTAMP" +
                    ")";
    //SQL to create table
    private static final String TABLE_CREATE_TAGS =
            "CREATE TABLE " + TABLE_TAGS + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TAG_IDENTIFIER + " TEXT unique, " +
                    DATE_TIME_CREATED + " TEXT default CURRENT_TIMESTAMP" +
                    ")";
    private static final String TABLE_CREATE_TAG_NOTE =
            "CREATE TABLE " + TABLE_NOTE_TAG + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_TAG_ID + " TEXT," +
                    KEY_NOTE_ID + " TEXT," +
                    DATE_TIME_CREATED + " TEXT default CURRENT_TIMESTAMP" +
                    ")";

    public DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_NOTES);
        db.execSQL(TABLE_CREATE_TAGS);
        db.execSQL(TABLE_CREATE_TAG_NOTE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTE_TAG);
        onCreate(db);
    }
    // ------------------------ "NOTE" table methods ----------------//
    //CREATING A NOTE
    public long createNOTE(Note note, long [] tag_ids){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NOTE_TITLE, note.getTitle());
        values.put(NOTE_TEXT, note.getText());
        values.put(DATE_TIME_CREATED, note.getCreatedDate());
        values.put(NOTE_EDITED, note.getEditedDate());

        long note_id = db.insert(TABLE_NOTES, null, values);

        for(long tag_id : tag_ids){
            createNoteTag(note_id, tag_id);
        }
        return note_id;
    }
    //GET A NOTE
    public Note getNote(long note_id){
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_NOTES + " WHERE "
                + KEY_ID + " = " + note_id;
        Log.e(LOG, selectQuery);
        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null) {c.moveToFirst();}
        Note note = new Note();
        note.setId(c.getInt(c.getColumnIndex(KEY_ID)));
        note.setTitle(c.getString(c.getColumnIndex(NOTE_TITLE)));
        note.setText(c.getString(c.getColumnIndex(NOTE_TEXT)));
        note.setCreatedDate(c.getString(c.getColumnIndex(DATE_TIME_CREATED)));
        note.setEditedDate(c.getString(c.getColumnIndex(NOTE_EDITED)));

        return note;
    }
    //GET ALL NOTES
    public List<Note> getAllNotes(){
        List<Note> notes = new ArrayList<Note>();
        String selectQuery = "SELECT  * FROM " + TABLE_NOTES;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                Note note = new Note();
                note.setId(c.getInt((c.getColumnIndex(KEY_ID))));
                note.setTitle((c.getString(c.getColumnIndex(NOTE_TITLE))));
                note.setText(c.getString(c.getColumnIndex(NOTE_TEXT)));
                note.setCreatedDate(c.getString(c.getColumnIndex(DATE_TIME_CREATED)));
                note.setEditedDate(c.getString(c.getColumnIndex(NOTE_EDITED)));
                // adding to todo list
                notes.add(note);
            } while (c.moveToNext());
        }
        return notes;
    }
    //GET ALL NOTES BY TAG
    public List<Note> getAllNotesByTag(String tag_Identifier){
        List<Note> notes = new ArrayList<Note>();

        String selectQuery = "SELECT  * FROM " + TABLE_NOTES + " td, "
                + TABLE_TAGS + " tg, " + TABLE_NOTE_TAG + " tt WHERE tg."
                + TAG_IDENTIFIER + " = '" + tag_Identifier + "'" + " AND tg." + KEY_ID
                + " = " + "tt." + KEY_TAG_ID + " AND td." + KEY_ID + " = "
                + "tt." + KEY_NOTE_ID;
        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                Note note = new Note();
                note.setId(c.getInt((c.getColumnIndex(KEY_ID))));
                note.setTitle((c.getString(c.getColumnIndex(NOTE_TITLE))));
                note.setText(c.getString(c.getColumnIndex(NOTE_TEXT)));
                note.setCreatedDate(c.getString(c.getColumnIndex(DATE_TIME_CREATED)));
                note.setEditedDate(c.getString(c.getColumnIndex(NOTE_EDITED)));
                // adding to notes list
                notes.add(note);
            } while (c.moveToNext());
        }
        return notes;
    }
    //GET NOTES Count
    public int getNotesCount(){
        String countQuery = "SELECT * FROM " + TABLE_NOTES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    //Update NOTE
    public int updateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NOTE_TITLE, note.getTitle());
        values.put(NOTE_TEXT, note.getText());
        values.put(NOTE_EDITED, note.getEditedDate());
        // updating row
        return db.update(TABLE_NOTES, values, KEY_ID + " = ?",
                new String[] { String.valueOf(note.getId()) });
    }

    //DELETE Note
    public void deleteNote(long Note_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTES, KEY_ID + " = ?",
                new String[]{String.valueOf(Note_id)});
    }

    // ------------------------ "tags" table methods ----------------//

    // CREATE TAG
    public long createTag(Tag tag) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TAG_IDENTIFIER, tag.getIdentifier());
        values.put(DATE_TIME_CREATED, getDateTime());

        // insert row
        long tag_id = db.insert(TABLE_NOTES, null, values);

        return tag_id;
    }

    /**
     * getting all tags
     * */
    public List<Tag> getAllTags() {
        List<Tag> tags = new ArrayList<Tag>();
        String selectQuery = "SELECT  * FROM " + TABLE_NOTES;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Tag t = new Tag();
                t.setId(c.getInt((c.getColumnIndex(KEY_ID))));
                t.setIdentifier(c.getString(c.getColumnIndex(TAG_IDENTIFIER)));

                // adding to tags list
                tags.add(t);
            } while (c.moveToNext());
        }
        return tags;
    }

    /*
     * Updating a tag
     */
    public int updateTag(Tag tag) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TAG_IDENTIFIER, tag.getIdentifier());

        // updating row
        return db.update(TABLE_TAGS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(tag.getId()) });
    }

    /*
     * Deleting a tag
     */
    public void deleteTag(Tag tag, boolean should_delete_all_tag_notes) {
        SQLiteDatabase db = this.getWritableDatabase();

        // before deleting tag
        // check if todos under this tag should also be deleted
        if (should_delete_all_tag_notes) {
            // get all todos under this tag
            List<Note> allTagNotes = getAllNotesByTag(tag.getIdentifier());

            // delete all todos
            for (Note note : allTagNotes) {
                // delete todo
                deleteNote(note.getId());
            }
        }

        // now delete the tag
        db.delete(TABLE_TAGS, KEY_ID + " = ?",
                new String[] { String.valueOf(tag.getId()) });
    }
    // ------------------------ "note_tags" table methods ----------------//

    /*
     * Creating note_tag
     */
    public long createNoteTag(long note_id, long tag_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NOTE_ID, note_id);
        values.put(KEY_TAG_ID, tag_id);
        values.put(DATE_TIME_CREATED, getDateTime());

        long id = db.insert(TABLE_NOTE_TAG, null, values);

        return id;
    }

    /*
     * Updating a note tag
     */
    public int updateNoteTag(long id, long tag_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TAG_ID, tag_id);

        // updating row
        return db.update(TABLE_NOTES, values, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
    }

    /*
     * Deleting a Note tag
     */
    public void deleteNoteTag(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTES, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
    }

    // closing database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }

    /**
     * get datetime
     * */
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
