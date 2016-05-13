package com.stv.mynotes;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NotesByTagList extends AppCompatActivity {

    private Uri uri;
    private String tagFilter;
    private CursorAdapter cursorAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_by_tag_list);

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

//        Toast.makeText(getApplicationContext(),String.valueOf(getTagId()) , Toast.LENGTH_LONG).show();
//        Toast.makeText(getApplicationContext(),getTagIdentifier() , Toast.LENGTH_LONG).show();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursorAdapter = new NotesCursorAdapter(this,cursor,0);

        list.setAdapter(cursorAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri uri = Uri.parse(NotesProvider.CONTENT_URI + "/" + id);
                Cursor selected = (Cursor) cursorAdapter.getItem(position);
                Toast.makeText(getApplicationContext(), selected.getString(selected.getColumnIndex(DBOpenHelper.NOTE_TITLE)), Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), selected.getString(selected.getColumnIndex(DBOpenHelper.NOTE_TEXT)), Toast.LENGTH_LONG).show();
            }
        });
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                Uri uri = Uri.parse(TagsProvider.CONTENT_URI + "/" + id);
                Toast.makeText(getApplicationContext(), "Ouch !", Toast.LENGTH_LONG).show();
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
    private long getNoteID(int position){


        return 0;
    }
}
