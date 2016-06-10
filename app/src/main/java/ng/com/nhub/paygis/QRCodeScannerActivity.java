package ng.com.nhub.paygis;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;

import ng.com.nhub.paygis.etc.BuildVars;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class QRCodeScannerActivity extends AppCompatActivity implements QRCodeReaderView.OnQRCodeReadListener {


    private QRCodeReaderView decoderView;
    private TextView tview;
    private ImageView scanLineImage;

    final int PERMISSIONS_REQUEST_USE_CAMERA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(BuildVars.DEBUG_VERSION){
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setTheme(R.style.AppDebugTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_scanner);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        if(Build.VERSION.SDK_INT >= 23 ){

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
//                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                        Manifest.permission.CAMERA)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

//                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            PERMISSIONS_REQUEST_USE_CAMERA);

//                }
            }else{
                showDecoderView();
            }

        }else{
            showDecoderView();
        }
//        tview = (TextView) findViewById(R.id.qrcode_howto);
    }

    private void showDecoderView(){
        // Most view in the layouts are NOT VISIBLE BY DEFAULT - Made VISIBLE at the end for performance reason
        decoderView = (QRCodeReaderView) findViewById(R.id.qrdecoderview);
        decoderView.setOnQRCodeReadListener(this);
        decoderView.setVisibility(View.VISIBLE);

        scanLineImage = (ImageView) findViewById(R.id.scan_line_image);
        ImageView scanBoxView = (ImageView) findViewById(R.id.scan_box);
        int h = scanBoxView.getDrawable().getIntrinsicHeight();
        int scanlineMarginTopBottom;

        // I don't trust android to return the exact image dimension
        if(h >= 550 && h <= 650){
            //margin-top of scan line: 28px
            scanlineMarginTopBottom = 28;
        }else if(h >= 450 && h <= 550){
            scanlineMarginTopBottom = 23;
        }else if(h >= 350 && h <= 450){
            scanlineMarginTopBottom = 18;
        }else if(h >= 250 && h <= 350){
            scanlineMarginTopBottom = 14;
        }else{
            scanlineMarginTopBottom = (18 + 14 + 23 + 28) / 4;
        }

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) scanLineImage.getLayoutParams();
        layoutParams.setMargins(0, scanlineMarginTopBottom, 0, scanlineMarginTopBottom);
        scanLineImage.setLayoutParams(layoutParams);

        TranslateAnimation mAnimation = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.9f);

        mAnimation.setDuration(1500);
        mAnimation.setRepeatCount(-1);
        mAnimation.setRepeatMode(Animation.REVERSE);
        mAnimation.setInterpolator(new LinearInterpolator());
        scanLineImage.setAnimation(mAnimation);

        scanBoxView.setVisibility(View.VISIBLE);
        scanLineImage.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_USE_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    showDecoderView();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    finish();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    // Called when a QR is decoded
    // "text" : the text encoded in QR
    // "points" : points where QR control points are placed

    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        // Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        Intent data = new Intent();
        data.putExtra("decodedQRCode", text);
        setResult(RESULT_OK, data);
        finish(); // ends current activity
    }

    @Override
    public void cameraNotFound() {

    }

    @Override
    public void QRCodeNotFoundOnCamImage() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(decoderView != null){
            decoderView.getCameraManager().startPreview();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(decoderView != null) {
            decoderView.getCameraManager().stopPreview();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
