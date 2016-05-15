package com.stv.mynotes;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class NotesByTagList extends AppCompatActivity implements View.OnTouchListener, LoaderManager.LoaderCallbacks<Cursor> {

    private Uri uri;
    private String tagFilter;
    private CursorAdapter cursorAdapter;
    private static final int EDITOR_REQUEST_CODE = 1001;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_by_tag_list);
        loadActivity();
    }
    private void loadActivity(){
        Intent intent = getIntent();
        uri = intent.getParcelableExtra(NotesProvider.CONTENT_ITEM_TYPE);
        setTitle(getTagIdentifier() + " Notes");
        final String selectQuery = "SELECT * FROM " + DBOpenHelper.TABLE_NOTES + " tn, "
                + DBOpenHelper.TABLE_TAGS + " tg, " + DBOpenHelper.TABLE_NOTE_TAG + " tnt WHERE tg."
                + DBOpenHelper.TAG_IDENTIFIER + " = '" + getTagIdentifier() + "'" + "AND tg." + DBOpenHelper.TAG_ID
                + " = " + "tnt." + DBOpenHelper.KEY_TAG_ID + " AND tn." + DBOpenHelper.NOTE_ID + " = " + "tnt."
                + DBOpenHelper.KEY_NOTE_ID;

        SQLiteDatabase db = new DBOpenHelper(NotesByTagList.this).getReadableDatabase();


        tagFilter = DBOpenHelper.TAG_ID + "=" + uri.getLastPathSegment();

        ListView list = (ListView) findViewById(R.id.All_NOTES_By_TAGS);

        Toast.makeText(getApplicationContext(),String.valueOf(getTagId()) , Toast.LENGTH_LONG).show();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursorAdapter = new NotesCursorAdapter(this,cursor,0);

        list.setAdapter(cursorAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor selected = (Cursor) cursorAdapter.getItem(position);
                Intent intent = new Intent(NotesByTagList.this, EditorActivity.class);
                Uri uri = Uri.parse(NotesProvider.CONTENT_URI + "/" + selected.getLong(selected.getColumnIndex(DBOpenHelper.KEY_NOTE_ID)));
                intent.putExtra(NotesProvider.CONTENT_ITEM_TYPE, uri);
                startActivityForResult(intent, EDITOR_REQUEST_CODE);
            }
        });
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                Cursor selected = (Cursor) cursorAdapter.getItem(position);
                final Uri CustomUri = Uri.parse(NoteTagProvider.CONTENT_URI + "/" + selected.getLong(selected.getColumnIndex(DBOpenHelper.NOTE_TAG_ID)));
                DialogInterface.OnClickListener dialogClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int button) {
                                if (button == DialogInterface.BUTTON_POSITIVE) {
                                    //Insert Data management code here
                                    String noteTagFilter = DBOpenHelper.NOTE_TAG_ID + "=" + CustomUri.getLastPathSegment();
                                    getContentResolver().delete(NoteTagProvider.CONTENT_URI, noteTagFilter, null);
//                                    restartLoader();
                                    loadActivity();
                                    Toast.makeText(NotesByTagList.this, "Deleted", Toast.LENGTH_SHORT).show();
                                }
                            }
                        };
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(NotesByTagList.this);
                builder.setMessage(getString(R.string.are_you_sure))
                        .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                        .show();

                return true;
            }
        });
    }

    private long getTagId(){
        Cursor cursor = getContentResolver().query(uri, DBOpenHelper.TAG_ALL_COLUMNS, tagFilter, null, null);
        cursor.moveToFirst();
        long id = cursor.getLong(cursor.getColumnIndex(DBOpenHelper.TAG_ID));
        return id;
    }
    private String getTagIdentifier(){
        Cursor cursor = getContentResolver().query(uri, DBOpenHelper.TAG_ALL_COLUMNS, tagFilter, null, null);
        cursor.moveToFirst();
        String identifier = cursor.getString(cursor.getColumnIndex(DBOpenHelper.TAG_IDENTIFIER));
        return identifier;
    }
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, NotesProvider.CONTENT_URI,
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }

    public void openEditorForNewNoteFromTag(View view) {
        Intent intent = new Intent(this, EditorActivity.class);
        intent.putExtra("FromTag",true);
//        intent.putExtra("TagId",getTagId());
        startActivityForResult(intent, EDITOR_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDITOR_REQUEST_CODE && resultCode == RESULT_OK) {
            loadActivity();
            restartLoader();
        }
    }
}
