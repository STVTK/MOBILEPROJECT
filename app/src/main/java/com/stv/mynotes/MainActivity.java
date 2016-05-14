package com.stv.mynotes;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity
implements LoaderManager.LoaderCallbacks<Cursor>, SearchView.OnQueryTextListener
{
    private static final int EDITOR_REQUEST_CODE = 1001;
    private CursorAdapter cursorAdapter;
    public static Context baseContext;
    ListView list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LoadDefaultView();

        Intent intent = getIntent();
        if(Intent.ACTION_SEARCH.equals(intent.getAction())){
            String text = intent.getStringExtra(SearchManager.QUERY);
            Toast.makeText(MainActivity.this, text,
                    Toast.LENGTH_SHORT).show();
        }

    }
    private void LoadDefaultView(){
        baseContext = getBaseContext();

        cursorAdapter = new NotesCursorAdapter(this, null, 0);

        list = (ListView) findViewById(android.R.id.list);
        list.setAdapter(cursorAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                Uri uri = Uri.parse(NotesProvider.CONTENT_URI + "/" + id);
                intent.putExtra(NotesProvider.CONTENT_ITEM_TYPE, uri);
                startActivityForResult(intent, EDITOR_REQUEST_CODE);
            }
        });
        getLoaderManager().initLoader(0, null, this);
    }

    private void insertNote(String noteText, String noteTitle) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, noteText);
        values.put(DBOpenHelper.NOTE_TITLE, noteTitle);
        Uri noteUri = getContentResolver().insert(NotesProvider.CONTENT_URI, values);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//         Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        if (searchView != null) {
            searchView.setOnQueryTextListener(this);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_create_sample:
                insertSampleData();
                break;
            case R.id.action_delete_all:
                deleteAllNotes();
                break;
            case R.id.action_add_tag:
                Intent intent = new Intent(this, tags_list.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteAllNotes() {

        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE) {
                            //Insert Data management code here
                            getContentResolver().delete(
                                    NotesProvider.CONTENT_URI, null, null
                            );
                            restartLoader();

                            Toast.makeText(MainActivity.this,
                                    getString(R.string.all_deleted),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.are_you_sure))
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .show();
    }

    private void insertSampleData() {
        insertNote("Simple note", "Title1");
        insertNote("Multi-line\nnote", "Title2");
        insertNote("Very long note with a lot of text that exceeds the width of the screen", "Title3");
        restartLoader();
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

    public void openEditorForNewNote(View view) {
        Intent intent = new Intent(this, EditorActivity.class);
        startActivityForResult(intent, EDITOR_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDITOR_REQUEST_CODE && resultCode == RESULT_OK) {
            restartLoader();
        }
    }

    private void displayResults(String newText){
        String selectQuery = "SELECT * FROM " + DBOpenHelper.TABLE_NOTES + " tn WHERE tn." +
                DBOpenHelper.NOTE_TITLE + " LIKE '" + newText + "%' OR tn." + DBOpenHelper.NOTE_TEXT + " LIKE '" + newText + "%' OR tn." +
                DBOpenHelper.NOTE_TITLE + " LIKE '%" + newText + "%' OR tn." + DBOpenHelper.NOTE_TEXT + " LIKE '%" + newText + "%' ";
        SQLiteDatabase db = new DBOpenHelper(MainActivity.this).getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursorAdapter = new NotesCursorAdapter(this, cursor, 0);
        list.setAdapter(cursorAdapter);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        if(s.equals("")){
            LoadDefaultView();
        }else {
            displayResults(s);
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        displayResults(s);
        return false;
    }
}
