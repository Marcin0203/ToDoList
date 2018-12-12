package main.com.todolist;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;

public class MyMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener {
    private ListView listView;
    private Context context;
    private ActionMode actionMode;

    MyMultiChoiceModeListener(ListView listView, Context context) {
        this.listView = listView;
        this.context = context;
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        this.actionMode = mode;
        MenuInflater menuInflater = mode.getMenuInflater();
        menuInflater.inflate(R.menu.contextual_bar_list, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                deleteSelected();
                return true;
            case R.id.done:
                changeSelected(true);
                return true;
            case R.id.toComplete:
                changeSelected(false);
                return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        MainActivity mainActivity = (MainActivity) context;
        mainActivity.loadList();
    }

    private void deleteSelected() {
        long selected[] = listView.getCheckedItemIds();
        for (long aSelected : selected) {
            context.getContentResolver().delete(ContentUris.withAppendedId(
                    MyContentProvider.URI_CONTENT, aSelected), null, null);
        }
        clearSelectedAndLoadList();
    }

    private void changeSelected(Boolean done) {
        long selected[] = listView.getCheckedItemIds();
        for (long aSelected : selected) {
            Cursor cursor = context.getContentResolver().query(ContentUris.withAppendedId(
                    MyContentProvider.URI_CONTENT, aSelected),
                    null, null, null, null);
            cursor.moveToFirst();
            ContentValues values = new ContentValues();
            values.put(DBHelper.COLUMN_ID, cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_ID)));
            values.put(DBHelper.COLUMN_USER_ID, cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_USER_ID)));
            values.put(DBHelper.COLUMN_TITLE, cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TITLE)));
            if (done)
                values.put(DBHelper.COLUMN_COMPLETED, "true");
            else
                values.put(DBHelper.COLUMN_COMPLETED, "false");
            context.getContentResolver().update(ContentUris.withAppendedId(
                    MyContentProvider.URI_CONTENT, cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ID))),
                    values, null, null);
        }

        clearSelectedAndLoadList();
    }

    private void clearSelectedAndLoadList() {
        listView.clearChoices();
        actionMode.finish();
        MainActivity mainActivity = (MainActivity) context;
        mainActivity.loadList();
    }
}
