package ng.com.nhub.paygis;

import android.app.Dialog;
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
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;

import ng.com.nhub.paygis.address.Remote;
import ng.com.nhub.paygis.etc.AppData;
import ng.com.nhub.paygis.etc.ApplicationLoader;
import ng.com.nhub.paygis.etc.BuildVars;
import ng.com.nhub.paygis.lib.FileLog;
import ng.com.nhub.paygis.lib.LocaleController;
import ng.com.nhub.paygis.lib.ParseLib;
import ng.com.nhub.paygis.lib.ShakeDetector;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PrincipalActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        HomeFragment.OnFragmentInteractionListener,
        DialPadFragment.HostInterface,
        PostsFragment.OnMessageListScrollListener,
        PostsFragment.OnMessageListFragmentInteractionListener,
        DialPadFragment.OnFragmentInteractionListener, View.OnClickListener{


    public static final int NOTIFICATION_ID = 0;
    private ProgressDialog progressDialog;
    private AlertDialog visibleDialog = null;
//    private ActionBarLayout actionBarLayout;
    ActionBarDrawerToggle drawerToggle;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient();
    ImageButton floatingMenuDialPad;
    FrameLayout floatingDialpadContainer;

    private boolean mIsDialpadShown = false;
    private DialPadFragment mDialpadFragment;

    private boolean isCommentFragShown = false;
    /**
     * Fragment containing the speed dial list, recents list, and all contacts list.
     */
    private PostsFragment mListsFragment = new PostsFragment();
    Toolbar toolbar;

    Dialog setUserDialog;

    private SensorManager mSensorManager;
    Sensor mAccelerometer;
    ShakeDetector mShakeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(BuildVars.DEBUG_VERSION){
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setTheme(R.style.AppDebugTheme);
        }
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_principal);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        floatingMenuDialPad = (ImageButton) findViewById(R.id.floating_action_menu_dial_pad);
        floatingDialpadContainer = (FrameLayout) findViewById((R.id.floating_action_dialpad_container));
        if(BuildVars.DEBUG_VERSION){
            floatingDialpadContainer.setBackgroundResource(R.drawable.fab_debug);
        }
        floatingMenuDialPad.setOnClickListener(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerLayout = navigationView.getHeaderView(0);
        if(BuildVars.DEBUG_VERSION){
            headerLayout.setBackgroundResource(R.drawable.side_nav_bar_debug);
        }

        Intent intent = getIntent();
        String lAViaIntent = intent.getStringExtra("logicalAddress");
        Uri lQRAddressViaIntent = intent.getParcelableExtra("logicalQRAddress");

        if(intent.getBooleanExtra("newLogin", false)){
            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
            installation.put("username", AppData.getCurrentUser().username);
            installation.saveInBackground();
            ParsePush.subscribeInBackground("news");
        }

        if(intent.getBooleanExtra("push_available", false)){
            Toast.makeText(this, "Push_available is true", Toast.LENGTH_LONG);
        }

        if(lAViaIntent != null){
            processTextWithLogicalAddress(lAViaIntent);
        }

        if(lQRAddressViaIntent != null){
            processImageWithQRCode(lQRAddressViaIntent);
        }

        if(intent != null) {

            if (intent.getExtras()!= null &&
                    getIntent().getExtras().getBoolean("parse")) {
                // Get Parse Notification here if interested.
                Toast.makeText(this, "default: get parse notification", Toast.LENGTH_LONG).show();
            }
        }

        // Add the favorites fragment but only if savedInstanceState is null. Otherwise the
        // fragment manager is responsible for recreating it.
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_news_container, mListsFragment)
                    .commit();
        }

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {
            boolean flag = false;
            @Override
            public void onShake(int count) {
                if (flag){
                    hideFloatingDialpad();
                    flag = false;
                }else{
                    showFloatingDialpad();
                    flag = true;
                }
            }
        });
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

    @Override
    public void onClickOnNewPost() {
        Intent intent = new Intent(this, NewPostActivity.class);
        startActivity(intent);
    }

    @Override
    public void openSetUserNameDialog() {
        setUserDialog = new Dialog(this);
        setUserDialog.setContentView(R.layout.set_username_dialog);
        setUserDialog.setCancelable(false);
        Button btnSave = (Button) setUserDialog.findViewById(R.id.btnSave);
        TextView btnCancel = (TextView) setUserDialog.findViewById(R.id.btnCancel);
        btnCancel.setText(Html.fromHtml("<u>"+LocaleController.getString("Cancel", R.string.Cancel)+"</u>"));
        btnSave.setText(LocaleController.getString("Save", R.string.Save));

        final EditText userHandle = (EditText) setUserDialog.findViewById(R.id.userHandle);
        setUserDialog.show();

        btnCancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(setUserDialog != null){
                    //Which ever works.
                    setUserDialog.dismiss();
                    setUserDialog.cancel();
                    setUserDialog = null;
                }
                onClickOnNewPost();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String handle = userHandle.getText().toString();
                if (handle != null && handle.length() >= 3){
                    ParseUser currentUser = ParseUser.getCurrentUser();
                    if (currentUser != null) {
                        currentUser.put("handle", handle);
                        currentUser.saveEventually();
                    }else{
                        Log.e("RETNAN", "Investigate - PrincipalActivity");
                        ParseLib.parseSignUpLogin(AppData.getCurrentUser());
                    }
                    if(setUserDialog != null){
                        //Which ever works.
                        setUserDialog.dismiss();
                        setUserDialog.cancel();
                        setUserDialog = null;
                    }
                    onClickOnNewPost();
                }else{
                    Toast.makeText(PrincipalActivity.this, "Must be greater than 3 letters", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showDialpadFragment(){

        if (mIsDialpadShown){
            return;
        }
        mIsDialpadShown = true;

//        mListsFragment.setUserVisibleHint(false);

        final android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if (mDialpadFragment == null) {
            mDialpadFragment = new DialPadFragment();
            mDialpadFragment.setArguments(getIntent().getExtras());
            ft.add(R.id.fragment_dialer_container, mDialpadFragment);
        } else {
            ft.show(mDialpadFragment);
        }
        ft.commit();
        hideFloatingDialpad();
        toolbar.setVisibility(View.GONE);
    }


    public void hideDialpadFragment(boolean animate, boolean clearDialpad) {
        if (mDialpadFragment == null || mDialpadFragment.getView() == null) {
            return;
        }
        if (clearDialpad) {
//            mDialpadFragment.clearDialpad();
        }
        if (!mIsDialpadShown) {
            return;
        }
        mIsDialpadShown = false;
        commitDialpadFragmentHide();
//        mDialpadFragment.setAnimate(animate);
//        mListsFragment.setUserVisibleHint(true);
//        mListsFragment.sendScreenViewForCurrentPosition();

//        mFloatingActionButtonController.align(getFabAlignment(), animate);
//        if (animate) {
//            mDialpadFragment.getView().startAnimation(mSlideOut);
//        } else {
//            commitDialpadFragmentHide();
//        }
        showFloatingDialpad();
        toolbar.setVisibility(View.VISIBLE);
    }

    /**
     * Finishes hiding the dialpad fragment after any animations are completed.
     */
    private void commitDialpadFragmentHide() {
        if (mDialpadFragment != null && !mDialpadFragment.isHidden()) {
            final android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.hide(mDialpadFragment);
            ft.commit();
        }
//        mFloatingActionButtonController.scaleIn(AnimUtils.NO_DELAY);
    }

    @Override
    public void onClick(View v) {
        int res = v.getId();
        switch (res){
            case R.id.floating_action_menu_dial_pad:
                showDialpadFragment();
                break;
            default:
                Log.d("PrincipalActivity", "Invalid View Clicked");
                Log.d("PrincipalActivity", "resId: "+String.valueOf(res));
        }
    }

    @Override
    public boolean onDialpadSpacerTouchWithEmptyQuery() {
        hideDialpadFragment(false, false);
        return false;
    }

    @Override
    public void onMessageListFragmentInteraction(int actionCode) {
        if (actionCode == PostsFragment.COMMENT_FRAG_OPEN){
            isCommentFragShown = true;
            hideFloatingDialpad();
            toolbar.setVisibility(View.GONE);
        }
    }

    private void hideFloatingDialpad(){
        floatingDialpadContainer.setVisibility(View.GONE);
    }

    private void showFloatingDialpad(){
        floatingDialpadContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMessageListScroll(RecyclerView recyclerView, int newState) {

//        LinearLayoutManager linearManager = (LinearLayoutManager) recyclerView.getLayoutManager();
//        if(linearManager.findFirstCompletelyVisibleItemPosition() == 0){
//            toolbar.setVisibility(View.VISIBLE);
//        }else{
//            if(recyclerView.getChildCount() > 2){
//                toolbar.setVisibility(View.GONE);
//            }
//        }

//        if(RecyclerView.SCROLL_STATE_DRAGGING == newState){
//            if(linearManager.findFirstCompletelyVisibleItemPosition() == 0){
//                toolbar.setVisibility(View.VISIBLE);
//            }else{
//                toolbar.setVisibility(View.INVISIBLE);
//            }
//        }else if(RecyclerView.SCROLL_STATE_SETTLING == newState){
//           if(linearManager.findFirstCompletelyVisibleItemPosition() == 0){
//                toolbar.setVisibility(View.VISIBLE);
//            }else{
//                toolbar.setVisibility(View.INVISIBLE);
//            }
//        }else{
//            if(linearManager.findFirstCompletelyVisibleItemPosition() == 0){
//                toolbar.setVisibility(View.VISIBLE);
//            }else{
//                toolbar.setVisibility(View.INVISIBLE);
//            }
//        }
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
        }

        if (mIsDialpadShown) {
            hideDialpadFragment(true, false);
        } else if(isCommentFragShown){
            hideCommentFragment(true);
        }else{
            super.onBackPressed();
        }

        drawerToggle.setDrawerIndicatorEnabled(true);
    }

    private void hideCommentFragment(boolean animate){
        if (!isCommentFragShown) {
            return;
        }

        if (mListsFragment != null && !mListsFragment.isHidden()) {
            FragmentTransaction ft = mListsFragment.getChildFragmentManager().beginTransaction();
            List<Fragment> fragments = mListsFragment.getChildFragmentManager().getFragments();
            for(Fragment fragment : fragments){
                if(fragment.isVisible()){
                    ft.detach(fragment);
                }
            }
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            ft.commit();
        }
        isCommentFragShown = false;
        showFloatingDialpad();
        toolbar.setVisibility(View.VISIBLE);
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
            }case R.id.action_write:{
                ParseUser currentUser = ParseUser.getCurrentUser();
                if(currentUser.getString("handle") == "Anonymous" ||
                        currentUser.getString("handle").equals("Anonymous")){
                    openSetUserNameDialog();
                }else{
                    onClickOnNewPost();
                }
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
        if(mListsFragment.isVisible()){
            mListsFragment.hideWelcomeCardIfVisible();
        }
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mShakeDetector);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}