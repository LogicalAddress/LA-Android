package ng.com.nhub.paygis;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import ng.com.nhub.paygis.etc.BuildVars;
import ng.com.nhub.paygis.lib.LocaleController;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(BuildVars.DEBUG_VERSION){
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setTheme(R.style.AppDebugTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView aboutView = (TextView) findViewById(R.id.about);
        aboutView.setText(Html.fromHtml(LocaleController.getString("about", R.string.about)));

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
