package sg.edu.nus.taptask;


import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.net.Uri;
import android.widget.Button;
import android.widget.TextView;

import sg.edu.nus.taptask.model.TapActionCall;
import sg.edu.nus.taptask.model.TapActionManager;


public class AddCallTaskActivity extends ActionBarActivity {

    static final int PICK_CONTACT_REQUEST = 0;
    private TextView targetNameField;
    private TextView targetNumField;
    private TapActionManager tapActionManager;
    private Button continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_call_task);
        targetNameField = (TextView) findViewById(R.id.targetName);
        targetNumField = (TextView) findViewById(R.id.targetNum);
        continueButton = (Button) findViewById(R.id.button);
        continueButton.setEnabled(false);
        tapActionManager = TapActionManager.getInstance(getBaseContext());
        targetNumField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectContact(v);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_call_task, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // go to add new task screen
    public void onClickContinueButton(View view) {
        Log.e("Meow", "Recording call activity screen activated");

        TapActionCall action = new TapActionCall(null, targetNumField.getText().toString(), targetNameField.getText().toString());
        tapActionManager.setCurrentTapAction(action);

        Intent intent;
        intent = new Intent(this, RecordActivity.class);
        startActivity(intent);
    }

    public void selectContact(View view){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(intent, PICK_CONTACT_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {

        if (data != null) {
            Uri uri = data.getData();

            if (uri != null) {
                Cursor c = null;
                try {
                    c = getContentResolver().query(uri, new String[]{
                                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME },
                            null, null, null);

                    if (c != null && c.moveToFirst()) {
                        String number = c.getString(0);
                        String name = c.getString(1);
                        this.targetNumField.setText(number);
                        this.targetNameField.setText(name);
                        if(targetNumField.getText().toString() != null && targetNameField.getText().toString() !=null){
                            continueButton.setEnabled(true);
                        } else {
                            continueButton.setEnabled(false);
                        }
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        }

    }

}
