package ng.com.nhub.paygis.lib;

/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 * ******************************
 * Adapted for nhub - by Retnan Daser, 3er Janvier 2016
 */

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import ng.com.nhub.paygis.etc.ApplicationLoader;
import ng.com.nhub.paygis.services.NotificationCenter;

/**
 * Created by retnan on 1/3/16.
 */
public class ConnectionsManager implements Action.ActionDelegate {

    private ArrayList<Action> actionQueue = new ArrayList<Action>();

    private boolean paused = false;
    private boolean appPaused = true;
    private long lastPauseTime = System.currentTimeMillis();


    private int currentAppVersion = 0;
    private volatile int connectionState = 2;


    @Override
    public void ActionDidFinishExecution(Action action, HashMap<String, Object> params) {

    }

    @Override
    public void ActionDidFailExecution(final Action action) {

        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                actionQueue.remove(action);
                action.delegate = null;
            }
        });

    }

    private static volatile ConnectionsManager Instance = null;

    public static ConnectionsManager getInstance() {

        ConnectionsManager localInstance = Instance;

        if (localInstance == null) {
            synchronized (ConnectionsManager.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new ConnectionsManager();
                }
            }
        }
        return localInstance;
    }

    private Runnable stageRunnable = new Runnable() {
        @Override
        public void run() {
            Utilities.stageQueue.handler.removeCallbacks(stageRunnable);
            Utilities.stageQueue.postRunnable(stageRunnable, 1000);
        }
    };

    public ConnectionsManager() {
        currentAppVersion = ApplicationLoader.getAppVersion();
        loadSession();

        if (!isNetworkOnline()) {
            connectionState = 1;
        }

        Utilities.stageQueue.postRunnable(stageRunnable, 1000);
    }


    public void doGetRequest(final String Uri) {

        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                FileLog.e("tmessages", "doing doRequest..." + Uri);
                try{
                    URL url = new URL(Uri);
                    URLConnection conn = url.openConnection();
                    HttpURLConnection httpConn = (HttpURLConnection) conn;
                    httpConn.setAllowUserInteraction(false);
                    httpConn.setInstanceFollowRedirects(true);
                    httpConn.setRequestMethod("GET");
                    httpConn.connect();
                    int response = httpConn.getResponseCode();

                    if (response == HttpURLConnection.HTTP_OK) {
                        InputStream inStrm = httpConn.getInputStream();
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.didFinishedDoRequest, inStrm);
                    }

                }catch(Exception e){
                    FileLog.e("tmessages", e);
                }

                NotificationCenter.getInstance().postNotificationName(NotificationCenter.didFinishedDoRequest, false);
            }
        });
    }


    public int getConnectionState() {
        return connectionState;
    }

    public void setConnectionState(int state) {
        connectionState = state;
    }

    private void resumeNetworkInternal() {
        if (paused) {
            lastPauseTime = System.currentTimeMillis();
            FileLog.e("tmessages", "wakeup network in background");
        } else if (lastPauseTime != 0) {
            lastPauseTime = System.currentTimeMillis();
            FileLog.e("tmessages", "reset sleep timeout");
        }
    }

    public void resumeNetworkMaybe() {
        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                resumeNetworkInternal();
            }
        });
    }

    public void switchBackend() {
        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    private void loadSession() {
        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    private void saveSession() {
        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    public void cleanUp() {
        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    public void initPushConnection() {
        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
//                Datacenter datacenter = datacenterWithId(currentDatacenterId);
//                if (datacenter != null) {
//                    if (datacenter.pushConnection == null) {
//                        datacenter.pushConnection = new TcpConnection(datacenter.datacenterId);
//                        datacenter.pushConnection.setSessionId(pushSessionId);
//                        datacenter.pushConnection.delegate = ConnectionsManager.this;
//                        datacenter.pushConnection.transportRequestClass = RPCRequest.RPCRequestClassPush;
//                        datacenter.pushConnection.connect();
//                        generatePing(datacenter, true);
//                    } else {
//                        if (UserConfig.isClientActivated() && !UserConfig.registeredForInternalPush) {
//                            registerForPush();
//                        }
//                    }
//                }
            }
        });
    }

    public static boolean isNetworkOnline() {
        try {
            ConnectivityManager cm = (ConnectivityManager)ApplicationLoader.appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && (netInfo.isConnectedOrConnecting() || netInfo.isAvailable())) {
                return true;
            }

            netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                return true;
            } else {
                netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if(netInfo != null && netInfo.isConnectedOrConnecting()) {
                    return true;
                }
            }
        } catch(Exception e) {
            FileLog.e("tmessages", e);
            return true;
        }
        return false;
    }

    public static boolean isRoaming() {
        try {
            ConnectivityManager cm = (ConnectivityManager)ApplicationLoader.appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null) {
                return netInfo.isRoaming();
            }
        } catch(Exception e) {
            FileLog.e("tmessages", e);
        }
        return false;
    }

    public static boolean isConnectedToWiFi() {
        try {
            ConnectivityManager cm = (ConnectivityManager)ApplicationLoader.appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                return true;
            }
        } catch(Exception e) {
            FileLog.e("tmessages", e);
        }
        return false;
    }

    public void setAppPaused(final boolean value, final boolean byScreenState) {

        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                if (!byScreenState) {
                    appPaused = value;
                    FileLog.e("tmessages", "app paused = " + value);
                }
                if (value) {
                    if (byScreenState) {
                        if (lastPauseTime == 0) {
                            lastPauseTime = System.currentTimeMillis();
                        }
                    } else {
                        lastPauseTime = System.currentTimeMillis();
                    }
                } else {
                    if (appPaused) {
                        return;
                    }
                    FileLog.e("tmessages", "reset app pause time");
                    if (lastPauseTime != 0 && System.currentTimeMillis() - lastPauseTime > 5000) {
                        // ContactsController.getInstance().checkContacts();
                    }
                    lastPauseTime = 0;
                    ConnectionsManager.getInstance().applicationMovedToForeground();
                }
            }
        });
    }

    public void applicationMovedToForeground() {

        Utilities.stageQueue.postRunnable(stageRunnable);
        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                if (paused) {
                    FileLog.e("tmessages", "reset timers by application moved to foreground");
                }
            }
        });
    }
}
