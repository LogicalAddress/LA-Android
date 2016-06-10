package ng.com.nhub.paygis;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

import ng.com.nhub.paygis.etc.AppData;
import ng.com.nhub.paygis.etc.ApplicationLoader;
import ng.com.nhub.paygis.etc.BuildVars;
import ng.com.nhub.paygis.etc.User;
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
import okhttp3.ResponseBody;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private ProgressDialog progressDialog;
    private AlertDialog visibleDialog = null;
    private boolean startPressed = false;

    private EditText phoneEdit;
    private EditText passwordEdit;
    private TextView loginBtn;

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView registerText = (TextView) findViewById(R.id.tRegister);
        registerText.setOnClickListener(this);

        loginBtn = (TextView) findViewById(R.id.btnLogin);
        loginBtn.setOnClickListener(this);
    }

    private Boolean validated(){
        return true;
    }

    private void doLoginUser(){
        showProgress();

        if(!validated()) {
            needShowAlert(LocaleController.getString("review_entries", R.string.review_entries));
            return;
        }

        try{

            String username = phoneEdit.getText().toString();
            String password = passwordEdit.getText().toString();

            RequestBody formBody = new FormBody.Builder()
                    .add("username", username)
                    .add("password", password)
                    .build();

            Request request = new Request.Builder()
                    .url(BuildVars.ServerRootPath + "/user/login")
                    .post(formBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideProgress();
                            needShowAlert(LocaleController.getString("unknown_error", R.string.unknown_error));
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
                            Log.i("info", "strResponse response: " + strResponse);
                            JSONObject JSONresponse = new JSONObject(strResponse);
                            //Store accessToken here and UserInfo
                            User user = new User();
                            user.accessToken = JSONresponse.getString("access_token");
                            JSONObject userDetails = JSONresponse.getJSONObject("user");
                            user.username = userDetails.getString("username");
                            user.userId = userDetails.getString("_id");
                            user.platform = "Android";
                            user.updatedAt = userDetails.getString("updated_at");
                            user.createdAt = userDetails.getString("created_at");
                            user.pushString = "#";
                            user.registeredForPush = false;
                            AppData.setCurrentUser(user);
                            AppData.saveUserData(true);
//                            AppData.loadUserData();
                            //Don't know why - crashes without these 2 lines in HomeActivity immediately after login
                            AppData.loadHomeData();
                            AppData.loadWorkData();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    postLoginAction(); //TODO: Pub Sub
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else if ((response.code() == HttpURLConnection.HTTP_BAD_REQUEST ||
                                    response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) ) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                needShowAlert(LocaleController.getString("login_error", R.string.login_error));
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
            FileLog.e("tmessages", "okHttp failed to deliver request");
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideProgress();
                    needShowAlert(LocaleController.getString("unknown_error", R.string.unknown_error));
                }
            });
            startPressed = false;
        }

    }

    private void postLoginAction(){


        Intent principalIntent = new Intent(getApplicationContext(), PrincipalActivity.class);
        // principalIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        principalIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(principalIntent);
        finish(); // I die too.
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

    private void clearCurrentState() {
        SharedPreferences preferences = ApplicationLoader.appContext.getSharedPreferences("logininfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.tRegister: {
                this.onBackPressed();
                return;
            }case R.id.btnLogin: {
                if (startPressed) {
                    return;
                }
                startPressed = true;
                doLoginUser();
            }default: {
                FileLog.e("tmessages", "Unexpected onClick() event from: " + view);
                return;
            }
        }

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
                loginBtn.performClick();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
