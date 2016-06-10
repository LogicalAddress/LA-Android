package ng.com.nhub.paygis.etc;

/**
 * Created by retnan on 1/3/16.
 */

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

import ng.com.nhub.paygis.address.Home;
import ng.com.nhub.paygis.address.Remote;
import ng.com.nhub.paygis.address.Work;
import ng.com.nhub.paygis.lib.FileLog;

public class AppData {

    private static User currentUser;
    private static Home homeAddress;
    private static Work workAddress;
    private static Remote remoteAddress;

    private final static Object sync = new Object();

    public static void loadUserData() {

        currentUser = new User();

        SharedPreferences preferences = ApplicationLoader.appContext.getSharedPreferences("user", Context.MODE_PRIVATE);

        currentUser.userId = preferences.getString("user_id", null);
        currentUser.username = preferences.getString("username", null);
        currentUser.platform = preferences.getString("platform", null);
        currentUser.accessToken = preferences.getString("access_token", null);

        currentUser.pushString = preferences.getString("user_gcm_push_string", null);
        currentUser.registeredForPush = preferences.getBoolean("register_for_push", false);
    }

    public static User getCurrentUser() {

        synchronized (sync) {
            return currentUser;
        }
    }

    public static void setCurrentUser(User user) {

        synchronized (sync) {
            currentUser = user;
        }
    }

    public static void saveUserData(boolean flag) {

        synchronized (sync) {
            try {
                SharedPreferences preferences = ApplicationLoader.appContext.getSharedPreferences("user", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("user_id", currentUser.userId);
                editor.putString("username", currentUser.username);
                editor.putString("platform", currentUser.platform);
                editor.putString("access_token", currentUser.accessToken);

                editor.putString("user_gcm_push_string", currentUser.pushString);
                editor.putBoolean("register_for_push", currentUser.registeredForPush);

                editor.commit();
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
        }
    }

    public static boolean isClientActivated() {
        synchronized (sync) {
            return currentUser.accessToken != null;
        }
    }

    public static void clearUserData() {
        currentUser = null;
    }

    public static Home getHome() {

        synchronized (sync) {
            return homeAddress;
        }
    }

    public static Work getWork() {

        synchronized (sync) {
            return workAddress;
        }
    }

    public static Remote getRemote() {

        synchronized (sync) {
            return remoteAddress;
        }
    }

    public static void loadHomeData() {

        homeAddress = new Home();

        SharedPreferences preferences = ApplicationLoader.appContext.getSharedPreferences("home", Context.MODE_PRIVATE);

        homeAddress.address = preferences.getString("address", null);
        homeAddress.city = preferences.getString("city", null);
        homeAddress.traceCode = preferences.getString("trace_code", null);
        homeAddress.longitude = Double.parseDouble(preferences.getString("longitude", "0.0"));
        homeAddress.latitude = Double.parseDouble(preferences.getString("latitude", "0.0"));
        homeAddress.shortcode = preferences.getString("short_code", null);
    }

    public static void loadRemoteData() {

        remoteAddress = new Remote();

        SharedPreferences preferences = ApplicationLoader.appContext.getSharedPreferences("remote", Context.MODE_PRIVATE);

        remoteAddress.traceCode = preferences.getString("trace_code", null);
        remoteAddress.longitude = Double.parseDouble(preferences.getString("longitude", "0.0"));
        remoteAddress.latitude =  Double.parseDouble(preferences.getString("latitude", "0.0"));
    }

    public static void setRemoteAddress(Remote remote) {

        synchronized (sync) {
            remoteAddress = remote;
        }
    }

    public static void saveRemoteAddress(boolean flag) {

        synchronized (sync) {
            try {
                SharedPreferences preferences = ApplicationLoader.appContext.getSharedPreferences("remote", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();

                editor.putString("trace_code", remoteAddress.traceCode);
                editor.putString("longitude", remoteAddress.longitude.toString());
                editor.putString("latitude", remoteAddress.latitude.toString());


                editor.commit();

            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
        }
    }


    public static void setHomeAddress(Home home) {

        synchronized (sync) {
            homeAddress = home;
        }
    }

    public static void saveHomeAddress(boolean flag) {

        synchronized (sync) {
            try {
                SharedPreferences preferences = ApplicationLoader.appContext.getSharedPreferences("home", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();

                editor.putString("address", homeAddress.address);
                editor.putString("city", homeAddress.city);
                editor.putString("trace_code", homeAddress.traceCode);
                editor.putString("longitude", homeAddress.longitude.toString());
                editor.putString("latitude", homeAddress.latitude.toString());
                editor.putString("short_code", homeAddress.shortcode);

                editor.commit();

            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
        }
    }

    public static void setWorkAddress(Work work) {

        synchronized (sync) {
            workAddress = work;
        }
    }

    public static void saveWorkAddress(boolean flag) {

        synchronized (sync) {
            try {
                SharedPreferences preferences = ApplicationLoader.appContext.getSharedPreferences("work", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();

                editor.putString("title", workAddress.title);
                editor.putString("address", workAddress.address);
                editor.putString("city", workAddress.city);
                editor.putString("trace_code", workAddress.traceCode);
                editor.putString("longitude", workAddress.longitude.toString());
                editor.putString("latitude", workAddress.latitude.toString());
                editor.putString("short_code", workAddress.shortcode);

                editor.commit();

            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
        }
    }

    public static void loadWorkData() {

        workAddress = new Work();

        SharedPreferences preferences = ApplicationLoader.appContext.getSharedPreferences("work", Context.MODE_PRIVATE);

        workAddress.title = preferences.getString("title", null);
        workAddress.address = preferences.getString("address", null);
        workAddress.city = preferences.getString("city", null);
        workAddress.traceCode = preferences.getString("trace_code", null);
        workAddress.longitude = Double.parseDouble(preferences.getString("longitude", "0.0"));
        workAddress.latitude = Double.parseDouble(preferences.getString("latitude", "0.0"));
        workAddress.shortcode = preferences.getString("short_code", null);
    }

    public static void saveParseUserState(Map<String, String> parseState) {
        synchronized (sync) {
            try {
                SharedPreferences preferences = ApplicationLoader.appContext.getSharedPreferences("parse", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                for (Map.Entry<String, String> entry : parseState.entrySet()) {
                    editor.putString(entry.getKey(), entry.getValue());
                }
                editor.commit();
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
        }
    }

}
