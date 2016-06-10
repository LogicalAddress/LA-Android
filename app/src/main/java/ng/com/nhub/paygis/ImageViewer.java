package ng.com.nhub.paygis;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import com.facebook.drawee.view.SimpleDraweeView;

import ng.com.nhub.paygis.etc.BuildVars;

public class ImageViewer extends AppCompatActivity {

    SimpleDraweeView iv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(BuildVars.DEBUG_VERSION){
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setTheme(R.style.AppDebugTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        iv = (SimpleDraweeView) findViewById(R.id.image);
        Intent intent = getIntent();
        String strUri = intent.getStringExtra("imageUri");
        Uri imageFilePath;
        if (strUri != null){
            imageFilePath = Uri.parse(strUri);
        }else{
            imageFilePath = Uri.parse("asset://images/avater.png");
        }
        // Point screenSize = AndroidUtilities.getRealScreenSize();
        // Log.e("RETNAN", screenSize.x + "," + screenSize.y);
        iv.setImageURI(imageFilePath);
    }
}
