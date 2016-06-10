package ng.com.nhub.paygis;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

import ng.com.nhub.paygis.address.Home;
import ng.com.nhub.paygis.address.Work;
import ng.com.nhub.paygis.etc.AppData;
import ng.com.nhub.paygis.etc.BuildVars;
import ng.com.nhub.paygis.lib.FileLog;
import ng.com.nhub.paygis.lib.LocaleController;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class WorkRegisterActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private AlertDialog visibleDialog = null;
    private boolean startPressed = false;

    TextView registerAddress;
    Double longitude;
    Double latitude;
    Double altitude;
    Double accuracy;

    private EditText userAddressEdit;
    private EditText userCityEdit;
    private EditText userWorkNameEdit;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(BuildVars.DEBUG_VERSION){
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setTheme(R.style.AppDebugTheme);
        }
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_work_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, LocaleController.getString("LoadingMap", R.string.LoadingMap), Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

                if(registerAddress.getVisibility() == View.VISIBLE){
                    registerAddress.setVisibility(View.GONE);
                }

                Intent mapIntent = new Intent(WorkRegisterActivity.this, MapLocationPickerActivity.class);
                mapIntent.putExtra("longitude", longitude);
                mapIntent.putExtra("latitude", latitude);
                startActivityForResult(mapIntent, 1);
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userAddressEdit = (EditText) findViewById(R.id.workAddress);
        userCityEdit = (EditText) findViewById(R.id.city);
        userWorkNameEdit = (EditText) findViewById(R.id.work_name);
        registerAddress = (TextView) findViewById(R.id.btnCreateAddress);
        registerAddress.setVisibility(View.GONE);

        /*Should we are editing*/
        if(AppData.getWork().city != null){
            userCityEdit.setText(AppData.getWork().city);
        }

        if(AppData.getWork().address != null){
            userAddressEdit.setText(AppData.getWork().address);
        }

        if(AppData.getWork().title != null){
            userWorkNameEdit.setText(AppData.getWork().title);
        }

        if(AppData.getWork().longitude != null){
            this.longitude = AppData.getWork().longitude;
        }

        if(AppData.getWork().latitude != null){
            this.latitude = AppData.getWork().latitude;
            registerAddress.setText(LocaleController.getString("editLocation", R.string.editLocation));
        }

        registerAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (startPressed) {
                    return;
                }
                startPressed = true;

                doCreateLogicalAddress();
            }
        });

        //        Prioritize the Map location Picker for first time user
        if (registerAddress.getVisibility() == View.GONE &&
                AppData.getWork().title == null) {
            Intent mapIntent = new Intent(WorkRegisterActivity.this, MapLocationPickerActivity.class);
            mapIntent.putExtra("longitude", longitude);
            mapIntent.putExtra("latitude", latitude);
            startActivityForResult(mapIntent, 1);
        }
    }


    private Boolean validated(){
        return true;
    }

    private void postCreateAction(){
        Intent data = new Intent();
        data.putExtra("updated", true);
        setResult(RESULT_OK, data);
        finish(); // ends current activity
    }

    private void doCreateLogicalAddress(){

        showProgress();

        if(!validated()) {
            needShowAlert(LocaleController.getString("review_entries", R.string.review_entries));
            return;
        }

        try{

            String address = userAddressEdit.getText().toString();
            String city = userCityEdit.getText().toString();
            String workName = userWorkNameEdit.getText().toString();

            JSONObject messageJson = new JSONObject();
            messageJson.put("location", new JSONObject());
            messageJson.getJSONObject("location").put("altitude", altitude);
            messageJson.getJSONObject("location").put("speed", "0.0");
            messageJson.getJSONObject("location").put("altitude_accuracy", accuracy);
            messageJson.getJSONObject("location").put("gps", new JSONObject());
            messageJson.getJSONObject("location").getJSONObject("gps").put("longitude", longitude);
            messageJson.getJSONObject("location").getJSONObject("gps").put("latitude", latitude);

            messageJson.put("work", new JSONObject());
            messageJson.getJSONObject("work").put("address", address);
            messageJson.getJSONObject("work").put("city", city);
            messageJson.getJSONObject("work").put("work_name", workName);

            RequestBody body = RequestBody.create(JSON, messageJson.toString());
            Request request = new Request.Builder()
                    .url(BuildVars.ServerRootPath + "/api/v1/work")
                    .header("User-Agent", "Android")
                    .addHeader("x-auth-token", AppData.getCurrentUser().accessToken)
                    .put(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideProgress();
                            needShowAlert(LocaleController.getString("general_error", R.string.general_error));
                        }
                    });

                    e.printStackTrace();
                    FileLog.e("tmessages", "okHttp Fatal" + e);
                    startPressed = false;
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideProgress();
                        }
                    });

                    if (response.code() == HttpURLConnection.HTTP_OK) {

                        try {

                            String strResponse = response.body().string();

                            JSONObject JSONresponse = new JSONObject(strResponse);

                            Work logicalAddress = new Work();
                            JSONObject workDetails = JSONresponse.getJSONObject("work");
                            logicalAddress.address = workDetails.getString("address");
                            logicalAddress.city = workDetails.getString("city");
                            logicalAddress.traceCode = workDetails.getString("trace_id");
                            logicalAddress.title = workDetails.getString("work_name");
                            logicalAddress.longitude = longitude;
                            logicalAddress.latitude = latitude;

                            AppData.setWorkAddress(logicalAddress);
                            AppData.saveWorkAddress(true);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    postCreateAction(); //TODO: Pub Sub
                                }
                            });

                        } catch (JSONException e) {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    needShowAlert(LocaleController.getString("try_again", R.string.try_again));
                                }
                            });

                            e.printStackTrace();
                        }

                    } else if ((response.code() == HttpURLConnection.HTTP_BAD_REQUEST ||
                            response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) ) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                needShowAlert(LocaleController.getString("session_expired", R.string.session_expired));
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                needShowAlert(LocaleController.getString("try_again", R.string.try_again));
                            }
                        });
                    }
                    startPressed = false;
                }
            });

        }catch (Exception e){

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideProgress();
                    needShowAlert(LocaleController.getString("unknown_error", R.string.unknown_error));
                }
            });

            FileLog.e("tmessages", "okHttp failed to deliver request");
            e.printStackTrace();
            startPressed = false;
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1 && data != null) {

            Log.i("info", "Retnan Intent Result: " +
                    data.getDoubleExtra("latitude", 0.0) +
                    " dragLong :" + data.getDoubleExtra("longitude", 0.0));

            if(data.getDoubleExtra("latitude", 0.0) == 0.0 &&
                    data.getDoubleExtra("longitude", 0.0) == 0){
                return;
            }else{
                longitude = data.getDoubleExtra("longitude", 0.0);
                latitude = data.getDoubleExtra("latitude", 0.0);
                accuracy = data.getDoubleExtra("accuracy", 0.0);
                altitude = data.getDoubleExtra("altitude", 0.0);

                registerAddress.setVisibility(View.VISIBLE);
            }
        }else{

        }
    }

    public void needShowAlert(final String text) {
        if (text == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(LocaleController.getString("app_name", R.string.app_name));
        builder.setMessage(text);
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
        showAlertDialog(builder);
    }

    public void showProgress() {
        if (this.isFinishing() || progressDialog != null) {
            return;
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(LocaleController.getString("Loading", R.string.Loading));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void hideProgress() {
        if (progressDialog == null) {
            return;
        }
        try {
            progressDialog.dismiss();
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        progressDialog = null;
    }

    private void showAlertDialog(AlertDialog.Builder builder) {

        try {
            visibleDialog = builder.show();
            visibleDialog.setCanceledOnTouchOutside(true);
            visibleDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    visibleDialog = null;
                    onDialogDismiss();
                }
            });
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
    }

    private void onDialogDismiss() {

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.next, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        switch (id) {
            case android.R.id.home: {
                onBackPressed(); //Redundacy - Not a orphan
                return true;
            }
            case R.id.action_next:{
                if(registerAddress.getVisibility() == View.VISIBLE){
                    registerAddress.performClick();
                }else{
                    Toast.makeText(getApplicationContext(),
                            LocaleController.getString("select_location_first",
                                    R.string.select_location_first),
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}