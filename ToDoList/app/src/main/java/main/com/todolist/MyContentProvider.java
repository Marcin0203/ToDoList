package main.com.todolist;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class MyContentProvider extends ContentProvider {
    private DBHelper dbHelper;
    private static final String ID = "main.com.todolist.MyContentProvider";
    public static final Uri URI_CONTENT = Uri.parse("content://"
    + ID + "/" + DBHelper.LIST_TABLE_NAME);
    public static final Uri URI_LIMIT = Uri.parse(URI_CONTENT+"/limit/");
    public static final String QUERY_PARAMETER_OFFSET = "offset";
    private static final int ALL_TABLE = 1;
    private static final int SELECTED_ROW = 2;
    static final int ELEMENTS_LIMITS = 5;
    private static final UriMatcher uriMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(ID, DBHelper.LIST_TABLE_NAME, ALL_TABLE);
        uriMatcher.addURI(ID, DBHelper.LIST_TABLE_NAME + "/#", SELECTED_ROW);
        uriMatcher.addURI(ID, DBHelper.LIST_TABLE_NAME +"/limit/#", ELEMENTS_LIMITS);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DBHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int typeUri = uriMatcher.match(uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = null;

        switch (typeUri) {
            case ALL_TABLE:
                cursor = db.query(true,
                        DBHelper.LIST_TABLE_NAME,
                        new String[]{DBHelper.COLUMN_USER_ID,
                                DBHelper.COLUMN_ID,
                                DBHelper.COLUMN_TITLE,
                                DBHelper.COLUMN_COMPLETED},
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder,
                        null);
                break;
            case SELECTED_ROW:
                cursor = db.query(true,
                        DBHelper.LIST_TABLE_NAME,
                        new String[]{DBHelper.COLUMN_USER_ID,
                                DBHelper.COLUMN_ID,
                                DBHelper.COLUMN_TITLE,
                                DBHelper.COLUMN_COMPLETED},
                        addIdToSelection(selection, uri),
                        selectionArgs,
                        null,
                        null,
                        sortOrder,
                        null);
                break;
            case ELEMENTS_LIMITS:
                String offset = uri.getQueryParameter(QUERY_PARAMETER_OFFSET);
                String limit = offset + "," + uri.getPathSegments().get(2);
                cursor = db.query(true,
                        DBHelper.LIST_TABLE_NAME,
                        new String[]{DBHelper.COLUMN_USER_ID,
                                DBHelper.COLUMN_ID,
                                DBHelper.COLUMN_TITLE,
                                DBHelper.COLUMN_COMPLETED},
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder,
                        limit);
                break;
            default:
                throw new  IllegalArgumentException("Nieznane URI: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int typeUri = uriMatcher.match(uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long idAddRow = 0;
        switch (typeUri) {
            case ALL_TABLE:
                idAddRow = db.insert(DBHelper.LIST_TABLE_NAME,
                        null,
                        values);
                break;
            default:
                throw new  IllegalArgumentException("Nieznane URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(DBHelper.LIST_TABLE_NAME + "/" + idAddRow);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int typeUri = uriMatcher.match(uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int numberDeleted = 0;
        switch (typeUri) {
            case ALL_TABLE:
                numberDeleted = db.delete(DBHelper.LIST_TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case SELECTED_ROW:
                numberDeleted = db.delete(DBHelper.LIST_TABLE_NAME,
                        addIdToSelection(selection, uri),
                        selectionArgs);
                break;
            default:
                throw new  IllegalArgumentException("Nieznane URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return numberDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int typeUri = uriMatcher.match(uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int numberUpdated = 0;
        switch (typeUri) {
            case ALL_TABLE:
                numberUpdated = db.update(DBHelper.LIST_TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case SELECTED_ROW:
                numberUpdated = db.update(DBHelper.LIST_TABLE_NAME,
                        values,
                        addIdToSelection(selection, uri),
                        selectionArgs);
                break;
            default:
                throw new  IllegalArgumentException("Nieznane URI: " + uri);
        }
        return numberUpdated;
    }

    private String addIdToSelection (String selection, Uri uri) {
        if (selection != null && !selection.equals("")) {
            selection = selection + " and " + DBHelper.COLUMN_ID + "="
                    + uri.getLastPathSegment();
        }
        else {
            selection = DBHelper.COLUMN_ID + "=" +uri.getLastPathSegment();
        }
        return selection;
    }
}
