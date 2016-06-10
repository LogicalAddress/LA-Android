package ng.com.nhub.paygis.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import ng.com.nhub.paygis.etc.ApplicationLoader;
import ng.com.nhub.paygis.lib.FileLog;

/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 * ******************************
 * Adapted for nhub - by Retnan Daser, 3er Janvier 2016
 */
public class NotificationsService extends Service {

    @Override
    public void onCreate() {
        FileLog.e("tmessages", "service started");
        ApplicationLoader.postInitApplication();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {

        FileLog.e("tmessages", "service destroyed");

        SharedPreferences preferences = ApplicationLoader.appContext.getSharedPreferences("Notifications", MODE_PRIVATE);

        if (preferences.getBoolean("pushService", true)) {
            Intent intent = new Intent("ng.com.gamechat.start");
            sendBroadcast(intent);
        }
    }
}
