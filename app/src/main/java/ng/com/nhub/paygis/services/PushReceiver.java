package ng.com.nhub.paygis.services;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import ng.com.nhub.paygis.Sociale.ContentTypes;
import ng.com.nhub.paygis.PrincipalActivity;
import ng.com.nhub.paygis.database.DatabaseHelper;
import ng.com.nhub.paygis.database.PostsModel;
import ng.com.nhub.paygis.etc.ApplicationLoader;
import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class PushReceiver extends ParsePushBroadcastReceiver {

    private static final String TAG = "PushReceiver";

    public PushReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        super.onPushReceive(context, intent);
        // do background task here - Populate Sqlite
        /*
        A notification is received and the com.parse.push.intent.OPEN Intent is fired,
        causing the ParsePushBroadcastReceiver to call onPushReceive. If either "alert" or "title"
        are specified in the push, then a Notification is constructed using getNotification.
        This Notification uses a small icon generated using getSmallIconId, which defaults to the
        icon specified by the com.parse.push.notification_icon metadata in your AndroidManifest.xml.
        The Notification's large icon is generated from getLargeIcon which defaults to null.
        The notification's contentIntent and deleteIntent are com.parse.push.intent.OPEN and
        com.parse.push.intent.DELETE respectively.
        */

        final String jsonData = intent.getExtras().getString("com.parse.Data");
        try {
            final JSONObject jsonObject = new JSONObject(jsonData);
            Log.i(TAG, jsonObject.toString());
//            String heading = jsonObject.getString("heading");
//            String dataString = jsonObject.getString("dataString");
//            String notificationText = json.getString("alert");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    DatabaseHelper dbHelper = new DatabaseHelper(ApplicationLoader.appContext);
                    SQLiteDatabase newsDb = dbHelper.getWritableDatabase();
                    try{
                        String postId, title, content, contentImagePath, logo,
                                jsonPollParams, pollExpiresTime;
                        Integer contentType, nPollItems;
                        Boolean pollExpires;

                        postId = jsonObject.getString("contentId");
                        title = jsonObject.getString("title");
                        content = jsonObject.getString("content");

                        if (jsonObject.has("contentImage")){
                            contentImagePath = jsonObject.getString("contentImage");
                        }else{
                            contentImagePath = "";
                        }

                        if (jsonObject.has("logo")){
                            logo = jsonObject.getString("logo");
                        }else{
                            logo = "";
                        }

                        if (jsonObject.has("contentType")){
                            if (jsonObject.getString("contentType").toString() == "poll"){
                                contentType = ContentTypes.POLL;
                            }else{
                                contentType = ContentTypes.DEFAULT;
                            }
                        }else{
                            contentType = ContentTypes.DEFAULT;
                        }

                        PostsModel postsModel;

                        switch (contentType){
                            case ContentTypes.POLL:{

                                jsonPollParams = jsonObject.getJSONObject("pollParams").toString();
                                nPollItems = Integer.valueOf(jsonObject.getInt("nPollParams"));
                                pollExpires = jsonObject.getBoolean("pollExpires");

                                if (pollExpires){
                                    pollExpiresTime = jsonObject.getString("expiredAt");
                                }else{
                                    pollExpiresTime = "";
                                }

                                postsModel = new PostsModel(postId, title, content, contentType,
                                        contentImagePath, logo, nPollItems, jsonPollParams,
                                        pollExpires, pollExpiresTime);
                                break;
                            }
                            default:
                                postsModel = new PostsModel(postId, title, content, contentType,
                                        contentImagePath, logo);
                                break;
                        }

                        Long _id = cupboard().withDatabase(newsDb).put(postsModel);
                        Log.i(TAG, "Created Row in PostsModel" + String.valueOf(_id));
                    }catch (Exception e){
                        Log.d(TAG, e.getMessage());
                    }
                }
            }).start();

        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e.getMessage());
        }
    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        super.onPushDismiss(context, intent);
        /*
        If the user dismisses a Notification, the com.parse.push.intent.DELETE Intent is fired.
        The ParsePushBroadcastReceiver calls onPushDismiss, which does nothing by default
         */
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        super.onPushOpen(context, intent);
        /*
        If the user taps on a Notification, the com.parse.push.intent.OPEN
        Intent is fired. The ParsePushBroadcastReceiver calls onPushOpen.
        The default implementation automatically sends an analytics event back to Parse tracking
        that this notification was opened. If the push contains a "uri" parameter,
        an activity is launched to navigate to that URI, otherwise the activity returned by
        getActivity is launched.
        */

//        Intent i = new Intent(context, PrincipalActivity.class);
//        i.putExtras(intent.getExtras());
//        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(i);
    }

    @Override
    protected Class<? extends Activity> getActivity(Context context, Intent intent) {
        Intent PrincipalIntent = new Intent(context, PrincipalActivity.class);
        PrincipalIntent.putExtra("push_available", true);
        return super.getActivity(context, PrincipalIntent);
    }
}