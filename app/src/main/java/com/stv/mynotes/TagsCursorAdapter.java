package com.stv.mynotes;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class TagsCursorAdapter extends CursorAdapter {
    public TagsCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(
                R.layout.tag_list_item, parent, false
        );
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

//        String notesNumber = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TEXT));
        String TagTitle = cursor.getString(cursor.getColumnIndex(DBOpenHelper.TAG_IDENTIFIER));
//        String TagTitle = "TITIKE TAG";
        String notesNumber = "5";
        int pos = TagTitle.indexOf(10);
        if (pos != -1) {
            TagTitle= TagTitle.substring(0, pos) + " ...";
        }

        TextView tv = (TextView) view.findViewById(R.id.TitleTag);
        tv.setText(TagTitle);

        TextView NotesNumber= (TextView) view.findViewById(R.id.NotesNumber);
        NotesNumber.setText(notesNumber);
    }
}
