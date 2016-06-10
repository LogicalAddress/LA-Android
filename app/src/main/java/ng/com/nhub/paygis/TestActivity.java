package ng.com.nhub.paygis;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import ng.com.nhub.paygis.etc.AppData;
import ng.com.nhub.paygis.lib.FileLog;
import ng.com.nhub.paygis.lib.LocaleController;

public class TestActivity extends AppCompatActivity {

    private static final int CONTACT_PICKER_RESULT = 1001;
    public static final String MIMETYPE_LOGICAL_ADDRESS = "vnd.android.cursor.item/logicaladdress";

    private AlertDialog visibleDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptAddToContact();
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CONTACT_PICKER_RESULT && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();

            // get the contact id from the Uri

//            Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
//            cursor.moveToFirst();
//            int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
//            String getDisplayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//            Log.d("phone number", cursor.getString(column));
            laToContactAddress(contactUri);
//            cursor.close();
        }else{
            // gracefully handle failure
        }
    }

    private void promptAddToContact(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle(LocaleController.getString("app_name", R.string.app_name));
        builder.setMessage(LocaleController.getString("add_to_contact", R.string.add_to_contact));
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(i, CONTACT_PICKER_RESULT);
            }
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        showAlertDialog(builder);
    }

    private void showAlertDialog(AlertDialog.Builder builder) {

        try {
            visibleDialog = builder.show();
            visibleDialog.setCanceledOnTouchOutside(true);
            visibleDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    visibleDialog = null;
//                    onDialogDismiss();
                }
            });
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
    }

    public void laToContactAddress(Uri contactUri) {

        try {

            ContentValues values = new ContentValues();
            values.put(ContactsContract.RawContacts.Data.DATA1, "VVVVVVVVVWWWWWAA");

            int mRowsUpdated = getContentResolver().update(
                    ContactsContract.Data.CONTENT_URI,
                    values,
                    ContactsContract.Data.RAW_CONTACT_ID + "='" + contactUri.getLastPathSegment() + "' AND "
                            + ContactsContract.Data.MIMETYPE + "='"
                            + MIMETYPE_LOGICAL_ADDRESS + "'", null);

            if (mRowsUpdated == 0) {
                values.put(ContactsContract.Data.RAW_CONTACT_ID, contactUri.getLastPathSegment());
                values.put(ContactsContract.Data.MIMETYPE, MIMETYPE_LOGICAL_ADDRESS);
                getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
                Toast.makeText(this, "Adding", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Found", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return;
    }

}
