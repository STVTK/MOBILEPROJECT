package com.stv.mynotes;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;


public class EditorActivity extends ActionBarActivity {

    private String action;
    private EditText editor;
    private EditText Title;
    private String noteFilter;
    private String oldText;
    private String oldTitle;

    private String EditedDate;
    private TextView EditedDateView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        editor = (EditText) findViewById(R.id.editText);
        Title = (EditText)findViewById(R.id.Title);
        EditedDateView = (TextView)findViewById(R.id.EditedDate);

        Intent intent = getIntent();

        Uri uri = intent.getParcelableExtra(NotesProvider.CONTENT_ITEM_TYPE);

        if (uri == null) {
            action = Intent.ACTION_INSERT;
            setTitle(getString(R.string.new_note));
        } else {
            action = Intent.ACTION_EDIT;
            noteFilter = DBOpenHelper.NOTE_ID + "=" + uri.getLastPathSegment();

            Cursor cursor = getContentResolver().query(uri,
                    DBOpenHelper.ALL_COLUMNS, noteFilter, null, null);
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
        }

        return true;
    }
    private void DownloadWebContent(){
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
                            if (result!= null) {
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
        getContentResolver().delete(NotesProvider.CONTENT_URI,
                noteFilter, null);
        Toast.makeText(this, getString(R.string.note_deleted), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void finishEditing() {
        String newText = editor.getText().toString().trim();
        String newTitle = Title.getText().toString().trim();

        switch (action) {
            case Intent.ACTION_INSERT:
                if (newText.length() == 0 && newTitle.length()==0) {
                    setResult(RESULT_CANCELED);
                } else {
                    insertNote(newText, newTitle);
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
    @Override
    public void onBackPressed() {
        finishEditing();
    }
}
