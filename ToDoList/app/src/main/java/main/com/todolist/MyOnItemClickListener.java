package main.com.todolist;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

class MyOnItemClickListener implements AdapterView.OnItemClickListener {
    private Context context;

    MyOnItemClickListener(Context context) {
        this.context = context;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Cursor cursor = context.getContentResolver().query(ContentUris.withAppendedId(
                MyContentProvider.URI_CONTENT, id), null, null,
                null, null);
        cursor.moveToFirst();

        final EditText editText = new EditText(context);
        editText.setText(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TITLE)));

        AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.AlertDialogTheme)
                .setPositiveButton(context.getResources().getString(R.string.buttonPositive),
                        new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity mainActivity = (MainActivity) context;
                            ContentValues values = new ContentValues();
                            values.put(DBHelper.COLUMN_ID, cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID)));
                            values.put(DBHelper.COLUMN_USER_ID, cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_USER_ID)));
                            values.put(DBHelper.COLUMN_COMPLETED, cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_COMPLETED)));
                            values.put(DBHelper.COLUMN_TITLE, editText.getText().toString());
                            context.getContentResolver().update(ContentUris.withAppendedId(
                                    MyContentProvider.URI_CONTENT, cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ID))),
                                    values, null, null);

                            mainActivity.loadList();
                            }
                    })
                .setNegativeButton(context.getResources().getString(R.string.buttonNegative), null)
                .setView(editText)
                .show();

        Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
        layoutParams.weight = 10;
        btnPositive.setLayoutParams(layoutParams);
        btnNegative.setLayoutParams(layoutParams);
    }
}
