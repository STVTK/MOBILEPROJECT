package com.stv.mynotes;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class EditorActivity extends ActionBarActivity {

    private String action;
    private EditText editor;
    private EditText Title;
    private String noteFilter;
    private String oldText;
    private String oldTitle;

    private String EditedDate;
    private TextView EditedDateView;

    private Uri uri;

    private static final String LOG = "DBOpenHelper";

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        editor = (EditText) findViewById(R.id.editText);
        Title = (EditText) findViewById(R.id.Title);
        EditedDateView = (TextView) findViewById(R.id.EditedDate);

        intent = getIntent();

        uri = intent.getParcelableExtra(NotesProvider.CONTENT_ITEM_TYPE);

        if (uri == null) {
            action = Intent.ACTION_INSERT;
            setTitle(getString(R.string.new_note));
        } else {
            action = Intent.ACTION_EDIT;
            noteFilter = DBOpenHelper.NOTE_ID + "=" + uri.getLastPathSegment();

            Cursor cursor = getContentResolver().query(uri, DBOpenHelper.NOTE_ALL_COLUMNS, noteFilter, null, null);
            cursor.moveToFirst();
            oldText = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TEXT));
            oldTitle = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TITLE));
            EditedDate = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_EDITED));

            editor.setText(oldText);
            Title.setText(oldTitle);
            EditedDateView.setText(EditedDate);
            editor.requestFocus();
            Title.requestFocus();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (action.equals(Intent.ACTION_EDIT)) {
            getMenuInflater().inflate(R.menu.menu_editor, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finishEditing();
                break;
            case R.id.action_delete:
                deleteNote();
                break;
            case R.id.action_download:
                DownloadWebContent();
                break;
            case R.id.action_add_tag:
                AddTags();
                break;

        }

        return true;
    }

    private void AddTags() {
        final String [] allTags = getAllTags().toArray(new String[getAllTags().size()]);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Choose a tag");
        dialogBuilder.setItems(allTags, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String selectedTad = allTags[item];
                long id = getTagId(selectedTad);
                insertNoteTag(id);
                Toast.makeText(getApplicationContext(), "TAG_ID" + String.valueOf(id), Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "NOTE_ID" + String.valueOf(getNoteId()), Toast.LENGTH_LONG).show();
            }
        });
        AlertDialog alertDialogObject = dialogBuilder.create();
        alertDialogObject.show();
    }


    private void DownloadWebContent() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("URL");

        final EditText input = new EditText(EditorActivity.this);
        input.setHint("Example https://www.google.com ");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialogBuilder.setView(input);
        alertDialogBuilder
                .setMessage("Enter a url")
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    boolean is_Empty = false;

                    public void onClick(DialogInterface dialog, int id) {
                        String url = input.getText().toString().trim();
                        if (!url.equals("")) {
                            Toast.makeText(getApplicationContext(), url, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Nothing Huh ?!", Toast.LENGTH_LONG).show();
                            is_Empty = true;
                        }
                        if (!is_Empty) {
                            DownloadTask task = new DownloadTask();
                            String result = null;

                            try {
                                result = task.execute(url).get();
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), "Failed to get " + url + " content", Toast.LENGTH_LONG).show();
                            }
                            if (result != null) {
                                editor.getText();
                                editor.setText(editor.getText() + result);
                            }
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        alertDialogBuilder.show();
    }

    private void deleteNote() {
        getContentResolver().delete(NotesProvider.CONTENT_URI, noteFilter, null);
        Toast.makeText(this, getString(R.string.note_deleted), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void finishEditing() {
        String newText = editor.getText().toString().trim();
        String newTitle = Title.getText().toString().trim();

        switch (action) {
            case Intent.ACTION_INSERT:
                if (newText.length() == 0 && newTitle.length() == 0) {
                    setResult(RESULT_CANCELED);
                } else {
                    insertNote(newText, newTitle);
                    Boolean fromTag = intent.getBooleanExtra("FromTag",false);
                    if(fromTag){
                        long Tag_id = intent.getLongExtra("TagId",-1);
                        insertNoteTag(Tag_id);
                    }
                }
                break;
            case Intent.ACTION_EDIT:
                if (newText.length() == 0 && newTitle.length() == 0) {
                    deleteNote();
                } else if (oldText.equals(newText) && oldTitle.equals(newTitle)) {
                    setResult(RESULT_CANCELED);
                } else {
                    updateNote(newText, newTitle);
                }

        }
        finish();
    }

    private void updateNote(String noteText, String noteTitle) {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, noteText);
        values.put(DBOpenHelper.NOTE_TITLE, noteTitle);
        values.put(DBOpenHelper.NOTE_EDITED, dateFormat.format(date));
        getContentResolver().update(NotesProvider.CONTENT_URI, values, noteFilter, null);
        Toast.makeText(this, getString(R.string.note_updated), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
    }

    private void insertNote(String noteText, String noteTitle) {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, noteText);
        values.put(DBOpenHelper.NOTE_TITLE, noteTitle);
        values.put(DBOpenHelper.NOTE_EDITED, dateFormat.format(date));
        getContentResolver().insert(NotesProvider.CONTENT_URI, values);
        setResult(RESULT_OK);
    }
    private void insertNoteTag(long tag_id){
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.KEY_TAG_ID,tag_id);
        Boolean fromTag = intent.getBooleanExtra("FromTag",false);
        if(fromTag){
            String selectQuery = "SELECT " + DBOpenHelper.NOTE_ID + " FROM " + DBOpenHelper.TABLE_NOTES + " ORDER BY " + DBOpenHelper.NOTE_ID + " DESC LIMIT 1";
            SQLiteDatabase db = new DBOpenHelper(EditorActivity.this).getReadableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);
            Long lastId = null;
            if (cursor != null && cursor.moveToFirst()) {
                lastId = cursor.getLong(0); //The 0 is the column index, we only have 1 column, so the index is 0
            }
            Toast.makeText(this, String.valueOf(lastId), Toast.LENGTH_SHORT).show();
            Toast.makeText(this, String.valueOf(tag_id), Toast.LENGTH_SHORT).show();
            values.put(DBOpenHelper.KEY_NOTE_ID, lastId);

            values.put(DBOpenHelper.NOTE_TAG_IDENTIFIER,String.valueOf(tag_id) + "//" + String.valueOf(lastId));
        } else {
            values.put(DBOpenHelper.KEY_NOTE_ID, getNoteId());
            values.put(DBOpenHelper.NOTE_TAG_IDENTIFIER,String.valueOf(tag_id) + "//" + String.valueOf(getNoteId()));
        }
        getContentResolver().insert(NoteTagProvider.CONTENT_URI, values);
        setResult(RESULT_OK);
    }

    @Override
    public void onBackPressed() {
        finishEditing();
    }
    private void insertTag(String Tag_title) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.TAG_IDENTIFIER, Tag_title);
        Uri TagUri = getContentResolver().insert(TagsProvider.CONTENT_URI, values);
        setResult(RESULT_OK);
    }
    private List<String> getAllTags(){
        List<String> tags = new ArrayList<String>();
        String selectQuery = "SELECT * FROM " + DBOpenHelper.TABLE_TAGS;
        SQLiteDatabase db = new DBOpenHelper(EditorActivity.this).getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                tags.add(cursor.getString(cursor.getColumnIndex(DBOpenHelper.TAG_IDENTIFIER)));
            } while (cursor.moveToNext());
        }
        return tags;
    }
    private long getTagId(String tag_title){
        long id = -1;
        String selectQuery = "SELECT * FROM " + DBOpenHelper.TABLE_TAGS + " WHERE " + DBOpenHelper.TAG_IDENTIFIER + " = '" + tag_title+"'";
        SQLiteDatabase db = new DBOpenHelper(EditorActivity.this).getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor != null && cursor.moveToFirst()) {
            id = cursor.getLong(cursor.getColumnIndex(DBOpenHelper.TAG_ID));
        }
        return id;
    }
    private long getNoteId(){
        Cursor cursor = getContentResolver().query(uri, DBOpenHelper.NOTE_ALL_COLUMNS, noteFilter, null, null);
        cursor.moveToFirst();
        long id = cursor.getLong(cursor.getColumnIndex(DBOpenHelper.NOTE_ID));
        return id;
    }
}
