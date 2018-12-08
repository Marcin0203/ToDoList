package main.com.todolist;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ScrollView;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private MySimpleCursorAdapter mySimpleCursorAdapter;
    private ListView listView;
    private ScrollView loadingScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.to_do_list_view);
        loadingScrollView = findViewById(R.id.loading_scroll_view);

        if (isExistingDatabase()) {
            fillList();
        }
        else {
            loadingScrollView.setVisibility(View.VISIBLE);
            HttpClient httpClient = new HttpClient(this);
            httpClient.getJsonData();
        }
        listView.setOnItemClickListener(new MyOnItemClickListener(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_menu:
                Intent intent = new Intent(this, AddElementActivity.class);
                startActivity(intent);
                return true;
        }
        return false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {DBHelper.COLUMN_ID, DBHelper.COLUMN_TITLE, DBHelper.COLUMN_USER_ID, DBHelper.COLUMN_COMPLETED};
        CursorLoader cursorLoader = new CursorLoader(this,
                MyContentProvider.URI_CONTENT, projection,
                null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mySimpleCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mySimpleCursorAdapter.swapCursor(null);
    }

    private boolean isExistingDatabase() {
        try {
            SQLiteDatabase checkDB = SQLiteDatabase.openDatabase("data/data/main.com.todolist/databases/"+DBHelper.DATABASE_NAME, null,
                    SQLiteDatabase.OPEN_READONLY);
            checkDB.close();
            return true;
        } catch (SQLiteException e) {
            return false;
        }
    }

    void fillList() {
        getLoaderManager().initLoader(0, null, this);

        String[] mapFrom = new String[] {DBHelper.COLUMN_COMPLETED, DBHelper.COLUMN_USER_ID, DBHelper.COLUMN_TITLE};
        int[] mapTo = new int[] {R.id.imageViewCompleted, R.id.id_user, R.id.title};

        mySimpleCursorAdapter = new MySimpleCursorAdapter(this,
                R.layout.row_list, null, mapFrom, mapTo, 0);

        listView.setAdapter(mySimpleCursorAdapter);
        loadingScrollView.setVisibility(View.GONE);
    }

    public void onClickInListView (final View view) {
        final Cursor cursor = getContentResolver().query(ContentUris.withAppendedId(
                MyContentProvider.URI_CONTENT, Integer.parseInt((String) view.getTag())),
                null, null, null, null);
        cursor.moveToFirst();
        PopupMenu popupMenu = new PopupMenu(this, view);

        if (cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_COMPLETED)).equals("true")) {
            popupMenu.inflate(R.menu.popup_menu_done);
        }
        else {
            popupMenu.inflate(R.menu.popup_menu_to_complete);
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.popupMenu_item_delete:
                        getContentResolver().delete(ContentUris.withAppendedId(
                                MyContentProvider.URI_CONTENT,
                                Integer.parseInt((String)view.getTag())),
                                null, null);
                        return true;
                    case R.id.popupMenu_item_status:
                        ContentValues values = new ContentValues();
                        values.put(DBHelper.COLUMN_ID, cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ID)));
                        values.put(DBHelper.COLUMN_USER_ID, cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_USER_ID)));
                        values.put(DBHelper.COLUMN_TITLE, cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TITLE)));
                        if (cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_COMPLETED)).equals("true"))
                            values.put(DBHelper.COLUMN_COMPLETED, "false");
                        else
                            values.put(DBHelper.COLUMN_COMPLETED, "true");

                        getContentResolver().update(ContentUris.withAppendedId(
                                MyContentProvider.URI_CONTENT, cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ID))),
                                values, null, null);
                        return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    public ScrollView getLoadingScrollView() {
        return loadingScrollView;
    }
}
