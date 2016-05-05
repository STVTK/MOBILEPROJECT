package com.stv.mynotes;

import android.content.ClipData;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.stv.mynotes.Entity.Note;
import java.util.List;

public class NoteAdapter extends ArrayAdapter<Note> {
    private List<Note> objects;
    public NoteAdapter(Context context, List<Note> objects) {
        super(context,0,objects);
        this.objects=objects;
    }

    public View getView(int position, View convertView, ViewGroup parent){

        View view = convertView;

        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.note_list_item, parent, false);
        }
        Note note = objects.get(position);
        TextView title = (TextView) view.findViewById(R.id.TitleNote);
        TextView text = (TextView) view.findViewById(R.id.tvNote);


        title.setText(note.getTitle());
        text.setText(note.getText());
        return view;
    }
}
