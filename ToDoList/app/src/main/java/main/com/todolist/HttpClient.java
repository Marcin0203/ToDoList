package main.com.todolist;

import android.content.ContentValues;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.font.TextAttribute;

import okhttp3.OkHttpClient;
import okhttp3.Request;

class HttpClient extends OkHttpClient {
    private Context context;
    private MainActivity mainActivity;

    HttpClient(Context context) {
        this.mainActivity = (MainActivity) context;
        this.context = context;
    }

    void getJsonData () throws IllegalArgumentException {
        Request request = new Request.Builder()
                .url("https://jsonplaceholder.typicode.com/todos/")
                .get()
                .addHeader("Content-Type", "application/json")
                .build();

        CallAsyncTask callAsyncTask = new CallAsyncTask(this, request);
        callAsyncTask.execute();
    }

    void saveJson(JSONArray jsonArray) {
        for (int i=0; i<jsonArray.length(); i++) {
            try {
                ContentValues values = getContentValues(jsonArray.getJSONObject(i));
                context.getContentResolver().insert(MyContentProvider.URI_CONTENT, values);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        mainActivity.setReadJson(true);
        mainActivity.fillList();
        mainActivity.checkHowElementsAndFillButtonsFooter();
    }

    private ContentValues getContentValues(JSONObject json) throws JSONException {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_USER_ID, json.getString("userId"));
        values.put(DBHelper.COLUMN_ID, json.getString("id"));
        values.put(DBHelper.COLUMN_TITLE, json.getString("title"));
        values.put(DBHelper.COLUMN_COMPLETED, json.getString("completed"));
        return values;
    }

    void setErrorServer() {
        ScrollView loadingScrollView = mainActivity.findViewById(R.id.loading_scroll_view);
        TextView textViewNullJson = mainActivity.findViewById(R.id.textViewNullJson);
        Button buttonNullJson = mainActivity.findViewById(R.id.buttonNullJson);
        textViewNullJson.setVisibility(View.VISIBLE);
        buttonNullJson.setVisibility(View.VISIBLE);
        loadingScrollView.setVisibility(View.GONE);
        Toast.makeText(context, "Błąd pobierania JSONa, sprawdź połączenie z Internetem.", Toast.LENGTH_LONG).show();
    }

    void setCanceled() {
        Toast.makeText(context, "Operacja przerwana.", Toast.LENGTH_SHORT).show();
    }
}
