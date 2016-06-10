package ng.com.nhub.paygis.services;

/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 * ******************************
 * Adapted for nhub - by Retnan Daser, 3er Janvier 2016
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ng.com.nhub.paygis.etc.ApplicationLoader;
import ng.com.nhub.paygis.lib.ConnectionsManager;
import ng.com.nhub.paygis.lib.FileLog;

public class ScreenReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            FileLog.e("tmessages", "screen off");
            ConnectionsManager.getInstance().setAppPaused(true, true);
            ApplicationLoader.isScreenOn = false;
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            FileLog.e("tmessages", "screen on");
            ConnectionsManager.getInstance().setAppPaused(false, true);
            ApplicationLoader.isScreenOn = true;
        }
    }
}
