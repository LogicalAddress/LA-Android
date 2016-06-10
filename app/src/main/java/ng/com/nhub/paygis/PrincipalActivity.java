package ng.com.nhub.paygis;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

//import com.facebook.appevents.AppEventsLogger;
import com.facebook.appevents.AppEventsLogger;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import ng.com.nhub.paygis.address.Remote;
import ng.com.nhub.paygis.etc.AppData;
import ng.com.nhub.paygis.etc.ApplicationLoader;
import ng.com.nhub.paygis.etc.BuildVars;
import ng.com.nhub.paygis.lib.FileLog;
import ng.com.nhub.paygis.lib.LocaleController;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PrincipalActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        HomeFragment.OnFragmentInteractionListener, TraceTargetFragment.OnFragmentInteractionListener{


    public static final int NOTIFICATION_ID = 0;
    private ProgressDialog progressDialog;
    private AlertDialog visibleDialog = null;
//    private ActionBarLayout actionBarLayout;
    ActionBarDrawerToggle drawerToggle;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_principal);
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        TraceTargetFragment defaultDisplayFragment = new TraceTargetFragment();
        defaultDisplayFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, defaultDisplayFragment).commit();


        Intent intent = getIntent();
        String lAViaIntent = intent.getStringExtra("logicalAddress");
        Uri lQRAddressViaIntent = intent.getParcelableExtra("logicalQRAddress");

        if(lAViaIntent != null){
            processTextWithLogicalAddress(lAViaIntent);
        }

        if(lQRAddressViaIntent != null){
            processImageWithQRCode(lQRAddressViaIntent);
        }
    }

    private void processTextWithLogicalAddress(String LARaw){
        //trace://492492742947sdsds42847237232353f - 32char Logical Address
        String mainCode = LARaw.substring(8, 40);
        callAddress(mainCode);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        callAddress(uri.toString());
    }

    class ProcessQRCode extends AsyncTask<Uri, Void, Result> {
        @Override
        protected Result doInBackground(Uri... params) {
            return returnCodeInImage(params[0]);
        }
        protected void onPostExecute(Result scanResult) {
             hideProgress();
            if (scanResult == null){
                needShowAlert(LocaleController.getString("QRScanError", R.string.QRScanError));
            }else{
                callAddress(scanResult.getText());
            }
        }
    }

    private void callShortAddress(String code){

    }


    private void postCreateAction(){
        showNotification();
        Intent intent = new Intent(this, TraceMapFullscreenActivity.class);
        startActivity(intent);
//        finish(); //Not sure
    }

    /* Long or short code - It should work */
    private void callAddress(String code){
        showDialProgress();
        // Toast.makeText(getApplicationContext(), code, Toast.LENGTH_SHORT).show();

        try{

            //Log.i("info", "strResponse get about to make request: " + AppData.getHome().traceCode);

            Request request = new Request.Builder()
                    .url(BuildVars.ServerRootPath + "/api/v1/universe/" + code)
                    .header("User-Agent", "Android")
                    .addHeader("x-auth-token", AppData.getCurrentUser().accessToken)
                    .get()
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideProgress();
                            needShowAlert(LocaleController.getString("try_again", R.string.try_again));
                        }
                    });
                    e.printStackTrace();
                    FileLog.e("tmessages", "okHttp Fatal" + e);
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

                        String strResponse = response.body().string();

                        try {
                            JSONObject JSONresponse = new JSONObject(strResponse);

                            Remote logicalAddress = new Remote();
                            JSONObject details = JSONresponse.getJSONObject("data");
                            JSONObject locationObj = details.getJSONObject("location_ref");
                            logicalAddress.longitude = locationObj.getJSONObject("gps").getDouble("longitude");
                            logicalAddress.latitude = locationObj.getJSONObject("gps").getDouble("latitude");
                            logicalAddress.traceCode = details.getString("trace_id");


                            AppData.setRemoteAddress(logicalAddress);
                            AppData.saveRemoteAddress(true);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    postCreateAction(); //TODO: Pub Sub
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    needShowAlert(LocaleController.getString("unknown_error", R.string.unknown_error));
                                }
                            });
                        }

                    } else if (response.code() == HttpURLConnection.HTTP_NOT_FOUND) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                needShowAlert(LocaleController.getString("incorrect_logical_address", R.string.incorrect_logical_address));
                            }
                        });
                    }else if (response.code() == HttpURLConnection.HTTP_BAD_REQUEST) {
                        //TODO: To Fire Log Out Event
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                needShowAlert(LocaleController.getString("session_expired", R.string.session_expired));
                            }
                        });
                    }  else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                needShowAlert(LocaleController.getString("try_again", R.string.try_again));
                            }
                        });
                    }
                    /*
                    http://stackoverflow.com/questions/31040310/okhttp-get-failed-response-body
                    Actually, on okhttp 2.4.0, when you call response.body().string() twice, the second
                    call breaks execution with java.lang.IllegalStateException: closed
                     */
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
        }
    }

    private void processImageWithQRCode(Uri UriQRcode){
        showProgress();
        new ProcessQRCode().execute(UriQRcode);
    }

    public Result returnCodeInImage(Uri uri)
    {
        try
        {
            InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap == null)
            {
                FileLog.e("tmessages", "uri is not a bitmap," + uri.toString());
                return null;
            }
            int width = bitmap.getWidth(), height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            bitmap.recycle();
            bitmap = null;
            RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
            BinaryBitmap bBitmap = new BinaryBitmap(new HybridBinarizer(source));
            MultiFormatReader reader = new MultiFormatReader();
            try
            {
                Result result = reader.decode(bBitmap);
                return result;
            }
            catch (NotFoundException e)
            {
                FileLog.e("tmessages", "decode exception", e);
                return null;
            }
        }
        catch (FileNotFoundException e)
        {
            FileLog.e("tmessages", "can not open file" + uri.toString(), e);
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

        drawerToggle.setDrawerIndicatorEnabled(true);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2 && data != null) {
            if(data.getStringExtra("decodedQRCode") != null){
                callAddress(data.getStringExtra("decodedQRCode"));
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.principal, menu);
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
                onBackPressed();
                return true;
            }
            case R.id.action_scan:{
                Intent intent = new Intent(this, QRCodeScannerActivity.class);
                // startActivity(intent);
                startActivityForResult(intent, 2);
                return true;
            }case R.id.nav_home:{
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                return true;
            }case R.id.nav_work:{
                Intent intent = new Intent(this, WorkActivity.class);
                startActivity(intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        int id = item.getItemId();

        if (id == R.id.nav_home) {

            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);

            //For TABLET: Use Fragments

//            HomeFragment firstFragment = new HomeFragment();
//            firstFragment.setArguments(getIntent().getExtras());
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.fragment_container, firstFragment).addToBackStack(null).commit();
//
//            drawerToggle.setDrawerIndicatorEnabled(false);
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


//            FragmentManager fragmentManager = getFragmentManager();
//            Fragment homeFragment = (android.app.Fragment) HomeFragment.newInstance(null, null);
//            fragmentManager.beginTransaction()
//                    .replace(R.id.fragment_container, (android.app.Fragment) homeFragment).addToBackStack(null).commit();

            drawer.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.nav_work) {

            Intent intent = new Intent(this, WorkActivity.class);
            startActivity(intent);

            //FOR TABLET: Use Fragments

//            WorkFragment workFragment = new WorkFragment();
//            workFragment.setArguments(getIntent().getExtras());
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.fragment_container, workFragment).addToBackStack(null).commit();
//
//            drawerToggle.setDrawerIndicatorEnabled(false);
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            drawer.closeDrawer(GravityCompat.START);
            return true;

//        } else if (id == R.id.nav_create_home) {
//
//            Intent intent = new Intent(this, HomeRegisterActivity.class);
//            startActivity(intent);
////            We may never create fragmented version display of this activity
//
//            drawer.closeDrawer(GravityCompat.START);
//            return true;
//        } else if (id == R.id.nav_create_work) {
//
//            Intent intent = new Intent(this, WorkRegisterActivity.class);
//            startActivity(intent);
//
////            We may never create fragmented version display of this activity
//
//            drawer.closeDrawer(GravityCompat.START);
//            return true;
        } else if (id == R.id.nav_share) {
            PackageManager pm=getPackageManager();
            try {

                Intent waIntent = new Intent(Intent.ACTION_SEND);
                waIntent.setType("text/plain");
                String text = LocaleController.getString("InviteText", R.string.InviteText);

                PackageInfo info=pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
                waIntent.setPackage("com.whatsapp");

                waIntent.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(Intent.createChooser(waIntent, "Share with"));

            } catch (PackageManager.NameNotFoundException e) {
                Toast.makeText(ApplicationLoader.appContext, LocaleController.getString("WhatsAppNotInstalled", R.string.WhatsAppNotInstalled), Toast.LENGTH_SHORT).show();
                Uri uri = Uri.parse("market://details?id=com.whatsapp");
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(goToMarket);
            }
            drawer.closeDrawer(GravityCompat.START);
            return true;
//        } else if (id == R.id.nav_setting) {
//            Intent intent = new Intent(this, SettingsActivity.class);
//            startActivity(intent);
//            drawer.closeDrawer(GravityCompat.START);
//            return true;
       /* } else if (id == R.id.nav_trace_map) {
//            Intent intent = new Intent(this, TraceMapFullscreenActivity.class);
//            startActivity(intent);
            showNotification();
            drawer.closeDrawer(GravityCompat.START);
            return true;
        */}else if (id == R.id.nav_about) {

            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void showNotification(){

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(LocaleController.getString("app_name", R.string.app_name));
        builder.setContentInfo(LocaleController.getString("notification_message", R.string.notification_message));

        Intent intent = new Intent(this, TraceMapFullscreenActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(TraceMapFullscreenActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManager NM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NM.notify(PrincipalActivity.NOTIFICATION_ID, builder.build());
    }

    public void needShowAlert(final String text) {
        if (text == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle(LocaleController.getString("app_name", R.string.app_name));
        builder.setMessage(text);
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
        showAlertDialog(builder);
    }

    public void showDialProgress() {

        if (this.isFinishing() || progressDialog != null) {
            return;
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(LocaleController.getString("dialing", R.string.dialing));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
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

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }
}