package ng.com.nhub.paygis;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.HttpURLConnection;

import ng.com.nhub.paygis.etc.BuildVars;
import ng.com.nhub.paygis.lib.FileLog;
import ng.com.nhub.paygis.lib.LocaleController;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//import com.loopj.android.http.AsyncHttpClient;
//import com.loopj.android.http.AsyncHttpResponseHandler;
//import com.loopj.android.http.RequestParams;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean startPressed = false;
    private ProgressDialog progressDialog;
    private AlertDialog visibleDialog = null;
    private EditText phoneEdit;
    private EditText passwordEdit;
    private EditText animalEdit;
    private EditText motherEdit;
    private TextView registerBtn;

    private final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        phoneEdit = (EditText) findViewById(R.id.mobileNumber);
        passwordEdit = (EditText) findViewById(R.id.password);
        motherEdit = (EditText) findViewById(R.id.mother);
        animalEdit = (EditText) findViewById(R.id.animal);


        TextView loginBtn = (TextView) findViewById(R.id.textLogin);
        loginBtn.setOnClickListener(this);

        registerBtn = (TextView) findViewById(R.id.btnRegister);
        // registerBtn.setText(LocaleController.getString("StartLocating", R.string.StartLocating));

        registerBtn.setOnClickListener(this);
    }

    private Boolean validated(){
        return true;
    }

    private void registerNewUser(){
        showProgress();

        if(!validated()){
            needShowAlert(LocaleController.getString("review_entries", R.string.review_entries));
            return ;
        }

        try{

            String username = phoneEdit.getText().toString();
            String password = passwordEdit.getText().toString();
            String animalQuestion = animalEdit.getText().toString();
            String motherQuestion = motherEdit.getText().toString();

            RequestBody formBody = new FormBody.Builder()
                    .add("username", username)
                    .add("password", password)
                    .add("q_animal", animalQuestion)
                    .add("q_mother", motherQuestion)
                    .add("q_space", "Mars")
                    .add("q_book", "Digital Fortress")
                    .build();

            Request request = new Request.Builder()
                    .url(BuildVars.ServerRootPath + "/user/register")
                    .post(formBody)
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

                    if (response.code() == HttpURLConnection.HTTP_CREATED) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent principalIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(principalIntent);
                            }
                        });
                    } else if (response.code() == HttpURLConnection.HTTP_FORBIDDEN) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                needShowAlert(LocaleController.getString("user_exist", R.string.user_exist));
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
                    needShowAlert(LocaleController.getString("general_error", R.string.general_error));
                }
            });
            FileLog.e("tmessages", "okHttp failed to deliver request");
            e.printStackTrace();
            startPressed = false;
        }

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.textLogin: {
                Intent principalIntent = new Intent(this, LoginActivity.class);
                startActivity(principalIntent);
                return;
            }case R.id.btnRegister:{

                if (startPressed) {
                    return;
                }
                startPressed = true;
                registerNewUser();
            }default :{
                FileLog.e("tmessages", "Unexpected onClick() event from: " + view);
                return;
            }
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
                registerBtn.performClick();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
