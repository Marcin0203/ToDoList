package main.com.todolist;

import android.content.ContentValues;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class AddElementActivity extends AppCompatActivity {
    private EditText title;
    private Spinner status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_element);

        title = findViewById(R.id.titleEditText);
        status = findViewById(R.id.spinnerStatus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_element_activity_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.back_menu:
                this.finish();
                return true;
        }
        return false;
    }

    public void addElement(View view) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_USER_ID, 1);
        values.put(DBHelper.COLUMN_TITLE, title.getText().toString());
        if ((status.getSelectedItem()).equals("Do Wykonania"))
            values.put(DBHelper.COLUMN_COMPLETED, "false");
        else
            values.put(DBHelper.COLUMN_COMPLETED, "true");

        Uri newUri = getContentResolver().insert(MyContentProvider.URI_CONTENT, values);
        Long id = Long.valueOf(newUri.getLastPathSegment());

        if (id > -1) {
            title.setText("");
            Toast.makeText(this, "Poprawnie dodano nowy element.", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Błąd w dodawaniu elementu.", Toast.LENGTH_SHORT).show();
        }

        
    }
}
