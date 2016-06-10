package ng.com.nhub.paygis.etc;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.facebook.accountkit.AccountKit;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.interceptors.ParseLogInterceptor;

import ng.com.nhub.paygis.Sociale.Comment;
import ng.com.nhub.paygis.Sociale.UserRating;
import ng.com.nhub.paygis.Sociale.UserSpace;
import ng.com.nhub.paygis.lib.FileLog;
import ng.com.nhub.paygis.lib.ParseLib;
import ng.com.nhub.paygis.services.ScreenReceiver;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class ApplicationLoader extends Application {

    public static volatile Handler applicationHandler = null;
    public static volatile Context appContext = null;
    public static volatile boolean isScreenOn = false;
    private static volatile boolean applicationRunning = false;

    public static void postInitApplication() {

        if (applicationRunning) {
            // This ensures this function is called ones.
            return;
        }

        applicationRunning = true;

        try {
            final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            final BroadcastReceiver mReceiver = new ScreenReceiver();
            appContext.registerReceiver(mReceiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            PowerManager pm = (PowerManager)ApplicationLoader.appContext.getSystemService(Context.POWER_SERVICE);
            isScreenOn = pm.isScreenOn();
            FileLog.e("tmessages", "screen state = " + isScreenOn);
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }

        AppData.loadUserData();
        AppData.loadHomeData();
        AppData.loadWorkData();
        AppData.loadRemoteData();

        if(AppData.getCurrentUser().username != null){
            ParseUser currentUser = ParseUser.getCurrentUser();
            if (currentUser == null) {
                // FailSafe - It doesn't have to get here
                Log.e("RETNAN", "Investigate - ApplicationLoader");
                ParseLib.parseSignUpLogin(AppData.getCurrentUser());
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT < 11) {
            java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
            java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");
        }
        appContext = getApplicationContext();
        applicationHandler = new Handler(appContext.getMainLooper());

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        AccountKit.initialize(getApplicationContext());

        Parse.enableLocalDatastore(getApplicationContext());
        com.parse.ParseObject.registerSubclass(Comment.class);
        com.parse.ParseObject.registerSubclass(UserSpace.class);
        com.parse.ParseObject.registerSubclass(UserRating.class);
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(BuildVars.ParseApiKey) // should correspond to APP_ID env variable
                .clientKey(null)  // set explicitly unless clientKey is explicitly configured on Parse server
                .addNetworkInterceptor(new ParseLogInterceptor())
                .server(BuildVars.LaSocialServer + "/parse/").build());

        ParseInstallation.getCurrentInstallation().saveInBackground();
        Fresco.initialize(this);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/CenturyGothic.ttf")
                .build()
        );
    }

    public static int getAppVersion() {
        try {

            PackageInfo packageInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
            return packageInfo.versionCode;

        } catch (PackageManager.NameNotFoundException e) {

            FileLog.d("tmessages", "Could not get package version: ");
            throw new RuntimeException("Could not get package version: " + e);
        }
    }

    public static String getAppVersionName() {

        try {

            PackageInfo packageInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
            return packageInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {

            FileLog.d("tmessages", "Could not get package name: " + e);
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

}
