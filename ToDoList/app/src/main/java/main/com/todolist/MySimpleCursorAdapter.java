package main.com.todolist;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

public class MySimpleCursorAdapter extends SimpleCursorAdapter {
    private Context context;

    public MySimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position,convertView,parent);

        Cursor cursor = getCursor();

        cursor.moveToPosition(position);
        ImageView imageView = view.findViewById(R.id.imageViewCompleted);

        if (cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_COMPLETED)).equals("true")) {
            imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.correct));
        }
        else {
            imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.incorrect));
        }

        ImageButton buttonPopupmenu = view.findViewById(R.id.buttonPopupmenu);
        buttonPopupmenu.setTag(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID)));
        buttonPopupmenu.setFocusable(false);
        return view;
    }
}
