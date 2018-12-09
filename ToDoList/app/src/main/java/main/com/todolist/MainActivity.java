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
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private MySimpleCursorAdapter mySimpleCursorAdapter;
    private ListView listView;
    private ScrollView loadingScrollView;
    private Boolean statusToDO = true;
    private Boolean statusDone = true;
    private int listItems;
    private Button[] buttons;
    private int howButtons;
    private int itemsToView = 20;
    private int currentButton = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.to_do_list_view);
        loadingScrollView = findViewById(R.id.loading_scroll_view);
        Spinner spinnerFiltering = findViewById(R.id.spinnerStatusFiltering);

        if (isExistingDatabase()) {
            fillList();
        }
        else {
            loadingScrollView.setVisibility(View.VISIBLE);
            HttpClient httpClient = new HttpClient(this);
            httpClient.getJsonData();
        }
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new MyMultiChoiceModeListener(listView, this));
        listView.setOnItemClickListener(new MyOnItemClickListener(this));

        spinnerFiltering.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    statusToDO = true;
                    statusDone = false;
                }
                else if (position == 2) {
                    statusToDO = false;
                    statusDone = true;
                }
                else {
                    statusDone = true;
                    statusToDO = true;
                }
                try {
                    currentButton = 0;
                    checkHowElementsAndFillButtonsFooter();
                    loadList(false);
                }
                catch (ArrayIndexOutOfBoundsException ignored){}
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        try {
            checkHowElementsAndFillButtonsFooter();
        }
        catch (ArrayIndexOutOfBoundsException ignored){}
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
                ContentUris.withAppendedId(MyContentProvider.URI_LIMIT, itemsToView)
                        .buildUpon().appendQueryParameter(MyContentProvider.QUERY_PARAMETER_OFFSET, "0").build(),
                projection, getSelection(), null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        loadList(true);
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

    public void onClickButtonInListView (final View view) {
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

                        loadList(true);
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

                        loadList(true);
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

    public String getSelection() {
        if (statusDone && statusToDO)
            return "("+DBHelper.COLUMN_COMPLETED+"='true' or "+DBHelper.COLUMN_COMPLETED+"='false')";
        else if (statusToDO)
            return "("+DBHelper.COLUMN_COMPLETED+"='false'"+")";
        else
            return "("+DBHelper.COLUMN_COMPLETED+"='true'"+")";

    }

    void checkHowElementsAndFillButtonsFooter() {
        Cursor cursor = getContentResolver().query(
                MyContentProvider.URI_CONTENT,
                null, getSelection(), null, null);
        listItems = cursor.getCount();
        buttonsFooter();
        buttons[currentButton].setBackgroundDrawable(getResources().getDrawable(R.drawable.selected_button_page));
    }

    private void buttonsFooter() {
        int val = listItems%itemsToView;
        val = val==0?0:1;
        howButtons = listItems/itemsToView+val;

        LinearLayout ll = findViewById(R.id.btnLay);
        ll.removeAllViews();

        buttons = new Button[howButtons];

        for(int i = 0; i< howButtons; i++) {
            buttons[i] =   new Button(this);
            buttons[i].setBackgroundColor(getResources().getColor(android.R.color.transparent));
            buttons[i].setText(""+(i+1));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ll.addView(buttons[i], lp);

            final int j = i;
            buttons[j].setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    loadListOnClick(j);
                    CheckButtonsBackground(j);
                }
            });
        }
    }

    private void CheckButtonsBackground(int index) {
        for(int i = 0; i< howButtons; i++) {
            if(i==index) {
                currentButton = i;
                buttons[i].setBackgroundDrawable(getResources().getDrawable(R.drawable.selected_button_page));
            }
            else {
                buttons[i].setBackgroundColor(getResources().getColor(android.R.color.transparent));
            }
        }

    }

    private void loadListOnClick(int button) {
        mySimpleCursorAdapter.swapCursor(
                getContentResolver().query(
                        ContentUris.withAppendedId(MyContentProvider.URI_LIMIT, itemsToView)
                                .buildUpon().appendQueryParameter(MyContentProvider.QUERY_PARAMETER_OFFSET, String.valueOf(button*20)).build(),
                        null, getSelection(), null, null)
        );
    }

    public void loadList(Boolean refreshButtonFooter) {
        mySimpleCursorAdapter.swapCursor(
                getContentResolver().query(
                        ContentUris.withAppendedId(MyContentProvider.URI_LIMIT, itemsToView)
                                .buildUpon().appendQueryParameter(MyContentProvider.QUERY_PARAMETER_OFFSET, String.valueOf(currentButton*20)).build(),
                        null, getSelection(), null, null)
        );
        if (refreshButtonFooter)
            checkHowElementsAndFillButtonsFooter();
    }
}
