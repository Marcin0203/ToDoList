package main.com.todolist;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

public class CallAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private HttpClient client;
    private Request request;
    private JSONArray json;

    CallAsyncTask(HttpClient client, Request request) {
        this.client = client;
        this.request = request;
    }
    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            Response response = client.newCall(request).execute();
            if (response.code() == 200) {
                assert response.body() != null;
                json = new JSONArray(response.body().string());
            }
            else {
                return false;
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if (success) {
            client.saveJson(json);
        }
        else {
            client.setErrorServer();
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        client.setCanceled();
    }
}
