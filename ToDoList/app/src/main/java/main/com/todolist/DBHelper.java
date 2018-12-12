package main.com.todolist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public final static int DATABASE_VERSION = 1;
    public final static String DATABASE_NAME = "to_do_list.db";
    public final static String LIST_TABLE_NAME = "list";
    public final static String COLUMN_USER_ID = "user_id";
    public final static String COLUMN_ID = "_id";
    public final static String COLUMN_TITLE = "title";
    public final static String COLUMN_COMPLETED = "completed";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "create table "+LIST_TABLE_NAME+" ("+COLUMN_USER_ID+" integer, "+
                COLUMN_ID+" integer primary key autoincrement, "+COLUMN_TITLE+" text, "+COLUMN_COMPLETED+
                " text)";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS to_do_list");
        onCreate(db);
    }
}
