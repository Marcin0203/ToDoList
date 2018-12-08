package main.com.todolist;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private MySimpleCursorAdapter mySimpleCursorAdapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.to_do_list_view);

        if (checkDataBase()) {
            fillList();
        }
        else {
            HttpClient httpClient = new HttpClient(this);
            httpClient.getJsonData();
        }
    }

    void fillList() {
        getLoaderManager().initLoader(0, null, this);

        String[] mapFrom = new String[] {DBHelper.COLUMN_COMPLETED, DBHelper.COLUMN_USER_ID, DBHelper.COLUMN_TITLE};
        int[] mapTo = new int[] {R.id.imageViewCompleted, R.id.id_user, R.id.title};

        mySimpleCursorAdapter = new MySimpleCursorAdapter(this,
                R.layout.row_list, null, mapFrom, mapTo, 0);

        listView.setAdapter(mySimpleCursorAdapter);
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

    private boolean checkDataBase() {
        try {
            SQLiteDatabase checkDB = SQLiteDatabase.openDatabase("data/data/main.com.todolist/databases/"+DBHelper.DATABASE_NAME, null,
                    SQLiteDatabase.OPEN_READONLY);
            checkDB.close();
            return true;
        } catch (SQLiteException e) {
            return false;
        }
    }
}
