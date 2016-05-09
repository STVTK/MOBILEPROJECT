package com.stv.mynotes;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.preference.DialogPreference;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class tags_list extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    private CursorAdapter cursorAdapter;
    private String TagFilter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tags_list);

        cursorAdapter = new TagsCursorAdapter(this, null, 0);

        ListView list = (ListView) findViewById(R.id.tags_listview);
        list.setAdapter(cursorAdapter);
        //TODO
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri uri = Uri.parse(TagsProvider.CONTENT_URI + "/" + id);
            }
        });
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                Uri uri = Uri.parse(TagsProvider.CONTENT_URI + "/" + id);
                TagFilter = DBOpenHelper.TAG_ID + "=" + uri.getLastPathSegment();

                Toast.makeText(tags_list.this, "bello", Toast.LENGTH_SHORT).show();
                openAlertForUpdateTag(uri);
                return true;
            }
        });
        getLoaderManager().initLoader(0, null, this);
    }
    private void insertTag(String Tag_title) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.TAG_IDENTIFIER, Tag_title);
        Uri TagUri = getContentResolver().insert(TagsProvider.CONTENT_URI, values);
        setResult(RESULT_OK);
    }
    private void updateTag(String Tag_title) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.TAG_IDENTIFIER, Tag_title);
        getContentResolver().update(TagsProvider.CONTENT_URI, values, TagFilter, null);
        Toast.makeText(this, "Tag Updated", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tags_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_create_sample:
                insertSampleData();
                break;
            case R.id.action_delete_all:
                deleteAllTags();
                break;
            case R.id.action_add_tag:
                Intent intent = new Intent(this, tags_list.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void deleteAllTags() {

        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE) {
                            //Insert Data management code here
                            getContentResolver().delete(TagsProvider.CONTENT_URI, null, null);
                            restartLoader();
                            Toast.makeText(tags_list.this, "All tags deleted", Toast.LENGTH_SHORT).show();
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
        insertTag("Home");
        insertTag("Tech");
        insertTag("Bello");
        restartLoader();
    }
    private void restartLoader() { getLoaderManager().restartLoader(0, null, this); }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, TagsProvider.CONTENT_URI,
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

    public void openAlertForNewTag(View view) {
        android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Tag");

        final EditText input = new EditText(tags_list.this);
        input.setHint("Example: Tech, TODO ...");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialogBuilder.setView(input);
        alertDialogBuilder
                .setMessage("Enter a Tag")
                .setCancelable(true)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    boolean is_Empty = false;

                    public void onClick(DialogInterface dialog, int id) {
                        String url = input.getText().toString().trim();
                        if (url.equals("")) {
                            Toast.makeText(getApplicationContext(), "Nothing Huh ?!", Toast.LENGTH_LONG).show();
                            is_Empty = true;
                        }
                        if (!is_Empty) {
                            insertTag(url);
                            restartLoader();
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
    public void openAlertForUpdateTag(Uri uri) {
        android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Update Tag");

        Cursor cursor = getContentResolver().query(uri, DBOpenHelper.TAG_ALL_COLUMNS, TagFilter, null, null);
        cursor.moveToFirst();
        String oldTitle= cursor.getString(cursor.getColumnIndex(DBOpenHelper.TAG_IDENTIFIER));


        final EditText input = new EditText(tags_list.this);
        input.setHint("Example: Tech, TODO ...");
        input.setText(oldTitle);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialogBuilder.setView(input);
        alertDialogBuilder
                .setMessage("Enter a new tag name")
                .setCancelable(true)
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    boolean is_Empty = false;

                    public void onClick(DialogInterface dialog, int id) {
                        String url = input.getText().toString().trim();
                        if (url.equals("")) {
                            deleteTag();
                            is_Empty = true;
                        }
                        if (!is_Empty) {
                            updateTag(url);
                            restartLoader();
                        }
                    }
                })
                .setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteTag();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        alertDialogBuilder.show();
    }
    private void deleteTag() {
        getContentResolver().delete(TagsProvider.CONTENT_URI, TagFilter, null);
        Toast.makeText(this, "Tag Deleted", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            restartLoader();
        }
    }
}
