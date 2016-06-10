package ng.com.nhub.paygis;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;

import java.util.Map;

import ng.com.nhub.paygis.etc.AppData;
import ng.com.nhub.paygis.etc.ApplicationLoader;
import ng.com.nhub.paygis.etc.BuildVars;

public class LaunchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if(BuildVars.DEBUG_VERSION){
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setTheme(R.style.AppDebugTheme);
        }

        ApplicationLoader.postInitApplication();

        if (!AppData.isClientActivated()) {
            Intent intent = getIntent();

            if (intent != null && !intent.getBooleanExtra("fromIntro", false)) {
                SharedPreferences preferences = ApplicationLoader.appContext.getSharedPreferences("logininfo", MODE_PRIVATE);
                Map<String, ?> state = preferences.getAll();

                if (state.isEmpty()) {
                    Intent intent2 = new Intent(this, IntroActivity.class);
                    startActivity(intent2);
                    super.onCreate(savedInstanceState);
                    finish();
                    return;
                }
            }
        }


        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if(intent != null && intent.getAction() != null){
            handleIntent(getIntent(), false, savedInstanceState != null);
        }else{
            handleDefault();
        }
    }

    private void handleIntent(Intent intent, boolean isNew, boolean restore) {

        if (AppData.isClientActivated() && (intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
            if (intent != null && intent.getAction() != null && !restore) {

                if (Intent.ACTION_SEND.equals(intent.getAction())) {

                    boolean error = false;
                    String type = intent.getType();

                    if (type != null && type.equals("text/plain") && intent.getStringExtra(Intent.EXTRA_TEXT) != null) {

                        String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                        String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);

                        if (text != null && text.length() != 0) {

                            if (text.startsWith("trace://") && subject != null && subject.length() != 0) {
                                text = text + ", " + subject;
                            }

                            handleSendText(text);
                            return;
                        }

                    }else if(type != null && type.startsWith("image/")){

                        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);

                        if(imageUri != null){
                            handleSendImage(imageUri);
                            return;
                        }
                    }
                }else if(false){
                    //if getActivity is not overridden in PushReceiver, Handle it here
                }
            }
        }

        handleDefault();
        return;
    }

    private void handleDefault(){
        Intent mainIntent = new Intent(this, PrincipalActivity.class);
        startActivity(mainIntent);
        finish();
    }

    void handleSendText(String logicalAddress) {
        Intent mainIntent = new Intent(this, PrincipalActivity.class);
        mainIntent.putExtra("logicalAddress", logicalAddress);
        startActivity(mainIntent);
        finish();
    }

    void handleSendImage(Uri imageUri) {
        Intent mainIntent = new Intent(this, PrincipalActivity.class);
        mainIntent.putExtra("logicalQRAddress", imageUri);
        startActivity(mainIntent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


}
