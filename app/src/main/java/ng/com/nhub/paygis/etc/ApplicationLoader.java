package ng.com.nhub.paygis.etc;

/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 * ******************************
 * Adapter for nhub - by Retnan Daser, 3er Janvier 2016
 */

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;

import java.util.HashMap;

//import com.google.android.gms.analytics.GoogleAnalytics;
//import com.google.android.gms.analytics.Logger;
//import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.GooglePlayServicesUtil;
//import com.google.android.gms.gcm.GoogleCloudMessaging;

import ng.com.nhub.paygis.lib.AndroidUtilities;
import ng.com.nhub.paygis.lib.FileLog;
import ng.com.nhub.paygis.lib.Utilities;
import ng.com.nhub.paygis.services.NotificationsService;
import ng.com.nhub.paygis.services.ScreenReceiver;

public class ApplicationLoader extends Application {

    public static volatile Handler applicationHandler = null;
    public static volatile Context appContext = null;
    public static volatile boolean isScreenOn = false;
    private static volatile boolean applicationRunning = false;

//    private GoogleCloudMessaging gcm;
//    private String regid;


    /**
     * Enum used to identify the tracker that needs to be used for tracking.
     *
     * A single tracker is usually enough for most purposes. In case you do need multiple trackers,
     * storing them all in Application object helps ensure that they are created only once per
     * application instance.
     */
//    public enum TrackerName {
//        APP_TRACKER, // Tracker used only in this app.
//        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
//        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
//    }

//    static HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();
//
//    public synchronized static Tracker getTracker(TrackerName trackerId, Context context) {
//        if (!mTrackers.containsKey(trackerId)) {
//
//            GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
//
//            if(BuildVars.DEBUG_VERSION){
//                GoogleAnalytics.getInstance(context).getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
//            }
//
//            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(BuildVars.PROPERTY_ID)
//                    : null;
//            mTrackers.put(trackerId, t);
//
//        }
//        return mTrackers.get(trackerId);
//    }

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
            FileLog.e("tmessages", e);
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
        ApplicationLoader app = (ApplicationLoader)ApplicationLoader.appContext;
        app.initPlayServices();


        FileLog.e("tmessages", "app initied");
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

        startPushService();

    }

    private void initPlayServices() {
//        if (checkPlayServices()) {
//            gcm = GoogleCloudMessaging.getInstance(this);
//            regid = getRegistrationId();
//
//            if (regid.length() == 0) {
//                registerInBackground();
//            } else {
//                sendRegistrationIdToBackend(false);
//            }
//        } else {
//            FileLog.d("tmessages", "No valid Google Play Services APK found.");
//        }
    }

    private void registerInBackground() {
//        AsyncTask<String, String, Boolean> task = new AsyncTask<String, String, Boolean>() {
//            @Override
//            protected Boolean doInBackground(String... objects) {
//                if (gcm == null) {
//                    gcm = GoogleCloudMessaging.getInstance(appContext);
//                }
//                int count = 0;
//                while (count < 1000) {
//                    try {
//                        count++;
//                        regid = gcm.register(BuildVars.GCM_SENDER_ID);
//                        sendRegistrationIdToBackend(true);
//                        storeRegistrationId(appContext, regid);
//                        return true;
//                    } catch (Exception e) {
//                        FileLog.e("tmessages", e);
//                    }
//                    try {
//                        if (count % 20 == 0) {
//                            Thread.sleep(60000 * 30);
//                        } else {
//                            Thread.sleep(5000);
//                        }
//                    } catch (InterruptedException e) {
//                        FileLog.e("tmessages", e);
//                    }
//                }
//                return false;
//            }
//        };
//
//        if (android.os.Build.VERSION.SDK_INT >= 11) {
//            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null, null, null);
//        } else {
//            task.execute(null, null, null);
//        }
    }

    private void sendRegistrationIdToBackend(final boolean isNew) {

//        Utilities.stageQueue.postRunnable(new Runnable() {
//            @Override
//            public void run() {
//
//                // if (AppData.getCurrentUser().mobile != null) {
//
//                if(true){
//
//                    AndroidUtilities.runOnUIThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            // RETNAN TODO: Upploading GCM regId to remote server: APA91bEZ_YxxRyBs8Ixnhy_DjEydASgaKExzm5k9mQCKh396sC87A0ZSOVZSvqjWgjTOfsc1Fq05pvqzACAp_4WnI8onjXEJyvGxXdFuIfboW5YcnJjfSYELUc3AvodrEnzRiKvNn-Qd
//                            FileLog.e("tmessages", "To Upload GCM regId to remote server: " + regid);
//                        }
//                    });
//                }
//            }
//        });
    }

    private void storeRegistrationId(Context context, String regId) {
//        final SharedPreferences prefs = getGCMPreferences(context);
//        int appVersion = getAppVersion();
//
//        FileLog.e("tmessages", "Saving regId on app version " + appVersion);
//
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString(BuildVars.PROPERTY_REG_ID, regId);
//        editor.putInt(BuildVars.PROPERTY_APP_VERSION, appVersion);
//        editor.commit();
    }

//    private boolean checkPlayServices() {
//        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
//        return resultCode == ConnectionResult.SUCCESS;

        /*if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("tmessages", "This device is not supported.");
            }
            return false;
        }
        return true;*/
//    }

//    private String getRegistrationId() {
//        final SharedPreferences prefs = getGCMPreferences(appContext);
//        String registrationId = prefs.getString(BuildVars.PROPERTY_REG_ID, "");
//
//        if (registrationId.length() == 0) {
//            FileLog.d("tmessages", "Registration not found.");
//            return "";
//        }
//        int registeredVersion = prefs.getInt(BuildVars.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
//        int currentVersion = getAppVersion();
//
//        if (registeredVersion != currentVersion) {
//            FileLog.d("tmessages", "App version changed.");
//            return "";
//        }
//        return registrationId;
//    }

    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(ApplicationLoader.class.getSimpleName(), Context.MODE_PRIVATE);
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

    public static void startPushService() {

//        SharedPreferences preferences = appContext.getSharedPreferences("Notifications", MODE_PRIVATE);
//
//        if (preferences.getBoolean("pushService", true)) {
//
//            appContext.startService(new Intent(appContext, NotificationsService.class));
//
//            if (Build.VERSION.SDK_INT >= 19) {
//
//                FileLog.e("tmessages", "starting PushService");
//
//                PendingIntent pintent = PendingIntent.getService(appContext, 0, new Intent(appContext, NotificationsService.class), 0);
//                AlarmManager alarm = (AlarmManager)appContext.getSystemService(Context.ALARM_SERVICE);
//                alarm.cancel(pintent);
//            }
//
//        } else {
//            stopPushService();
//        }
    }

    public static void stopPushService() {

//        FileLog.e("tmessages", "Stopping PushService");
//
//        appContext.stopService(new Intent(appContext, NotificationsService.class));
//        PendingIntent pintent = PendingIntent.getService(appContext, 0, new Intent(appContext, NotificationsService.class), 0);
//        AlarmManager alarm = (AlarmManager)appContext.getSystemService(Context.ALARM_SERVICE);
//        alarm.cancel(pintent);
    }
}
