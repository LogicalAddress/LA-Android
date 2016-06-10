package ng.com.nhub.paygis;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;

import ng.com.nhub.paygis.etc.AppData;
import ng.com.nhub.paygis.etc.BuildVars;
import ng.com.nhub.paygis.lib.FileLog;
import ng.com.nhub.paygis.lib.FileUtil;
import ng.com.nhub.paygis.lib.LocaleController;
import ng.com.nhub.paygis.lib.ParseLib;
import okhttp3.OkHttpClient;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class NewPostActivity extends AppCompatActivity {

    private static final String TAG = "NewPostActivity";
    private ProgressDialog progressDialog;
    private AlertDialog visibleDialog = null;
    private boolean startPressed = false;
    private final OkHttpClient client = new OkHttpClient();

    private final int SELECT_FILE = 1;
    private String imagePath = null;
    private ImageView iv = null;
    private Bitmap bmImage = null;
    TextView etDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(BuildVars.DEBUG_VERSION){
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setTheme(R.style.AppDebugTheme);
        }
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView tvTitle = (TextView) findViewById(R.id.title);
        etDescription = (TextView) findViewById(R.id.description);
        etDescription.setFilters(new InputFilter[] {new InputFilter.LengthFilter(140)});
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null && currentUser.getString("handle") != null) {
            tvTitle.setText(currentUser.getString("handle"));
        } else {
            //Stick with the default for now
            ParseLib.parseSignUpLogin(AppData.getCurrentUser());
        }

        iv = (ImageView) findViewById(R.id.image);
        iv.setVisibility(View.GONE);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.newpost, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // - See more at: http://www.theappguruz.com/blog/android-take-photo-camera-gallery-code-sample#sthash.d2FJnRbQ.dpuf
            if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                String[] projection = {MediaStore.MediaColumns.DATA};
                CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null,
                        null);
                Cursor cursor = cursorLoader.loadInBackground();
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();
                String selectedImagePath = cursor.getString(column_index);
                imagePath = selectedImagePath;

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(selectedImagePath, options);
                final int REQUIRED_SIZE = 800;
                int scale = 1;
                while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                        && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;
                options.inSampleSize = scale;
                options.inJustDecodeBounds = false;
                bmImage = BitmapFactory.decodeFile(selectedImagePath, options);


//                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//                bmOptions.inJustDecodeBounds = true;
//                // bmImage = BitmapFactory.decodeFile(selectedImagePath, bmOptions);
//                bmImage = BitmapFactory.decodeFile(selectedImagePath);

//                if(bmImage.getWidth() > bmImage.getHeight()){
//                    if (bmImage.getWidth() > 720){
//                        bmImage = BitmapScaler.scaleToFitWidth(bmImage, 720);
//                    }
//                }else{
//                    if (bmImage.getHeight() > 622){
//                        bmImage = BitmapScaler.scaleToFitHeight(bmImage, 622);
//                    }
//                }
                iv.setImageBitmap(bmImage);
                iv.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void showAddPhoto(){
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        switch (id) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
            case R.id.action_next:{
                // TODO: broadcast the post
                if (startPressed) {
                    return true;
                }
                startPressed = true;
                if(validate()){
                    if(bmImage == null){
                        createPost();
                    }else{
                        createPost(imagePath);
                    }
                    Toast.makeText(this, "Broadcasting post", Toast.LENGTH_SHORT).show();
                }else{
                    startPressed = false;
                }
                return true;
            }
            case R.id.action_add_photo: {
                showAddPhoto();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean validate(){

        if (etDescription.getText().toString().trim().length() == 0 &&
                bmImage == null){
            needShowAlert("Please select an image or write in the text box to publish");
            return false;
        }

        if (AppData.getHome().latitude == 0 &&
                AppData.getHome().longitude == 0){
            // needShowAlert("Please refresh or setup your home address, then return to publish");
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            return false;
        }
        return true;
    }

    private void createPost() {
        showProgress();
        ParseUser user = ParseUser.getCurrentUser();
        ParseObject parseObjectObject = new ParseObject("Posts");
        parseObjectObject.put("content", etDescription.getText().toString().trim());
        parseObjectObject.put("contentType", "social");
        ParseGeoPoint point = new ParseGeoPoint(AppData.getHome().latitude,
                AppData.getHome().longitude);
        parseObjectObject.put("location", point);
        parseObjectObject.put("user", user);
        parseObjectObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                startPressed = false;
                hideProgress();
                if (e == null) {
                    Toast.makeText(NewPostActivity.this, "Sent", Toast.LENGTH_SHORT).show();
                    finish();
                }else{
                    needShowAlert("Please try again");
                    Log.i(TAG, e.getMessage());
                }
            }
        });
    }

    private void createPost(String imagePath) {
        showProgress();
         ParseFile file;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if (Bitmap.CompressFormat.WEBP == FileUtil.getCompressFormat(imagePath)){
            //gif image should not be compressed
            Log.e(TAG, "uploading gif.." + imagePath);
            file = new ParseFile(new File(imagePath));
        }else{
            if(bmImage.getWidth() > 800 || bmImage.getHeight() > 800){
                bmImage.compress(FileUtil.getCompressFormat(imagePath), 100, stream);
                byte[] byteArray = stream.toByteArray();
                file = new ParseFile(Uri.parse(imagePath).getLastPathSegment(), byteArray);
            }else{
                file = new ParseFile(new File(imagePath));
            }
        }
        ParseUser user = ParseUser.getCurrentUser();
        ParseObject parseObjectObject = new ParseObject("Posts");
        if (etDescription.getText().toString().trim().length() != 0){
            parseObjectObject.put("content", etDescription.getText().toString().trim());
        }else{
            parseObjectObject.put("content", "");
        }
        parseObjectObject.put("contentType", "social");
        ParseGeoPoint point = new ParseGeoPoint(AppData.getHome().latitude,
                AppData.getHome().longitude);
        parseObjectObject.put("location", point);
        parseObjectObject.put("user", user);
        parseObjectObject.put("image", file);
        parseObjectObject.put("image", file);
        parseObjectObject.put("rating", 0); //toRemove this
        parseObjectObject.put("rating", 0); //toRemove this
        parseObjectObject.put("rateCount", 0); //toRemove this (Do this in liveQuery)
        parseObjectObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                startPressed = false;
                hideProgress();
                if (e == null) {
                    Toast.makeText(NewPostActivity.this, "Sent", Toast.LENGTH_SHORT).show();
                    bmImage.recycle();
                    finish();
                }else{
                    needShowAlert("Please try again");
                    Log.i(TAG, e.getMessage());
                }
            }
        });
    }

    public void needShowAlert(final String text) {
        if (text == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(LocaleController.getString("app_name", R.string.app_name));
        builder.setMessage(text);
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
        showAlertDialog(builder);
    }

    public void showProgress() {
        if (this.isFinishing() || progressDialog != null) {
            return;
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(LocaleController.getString("Loading", R.string.Loading));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void hideProgress() {
        if (progressDialog == null) {
            return;
        }
        try {
            progressDialog.dismiss();
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        progressDialog = null;
    }

    private void showAlertDialog(AlertDialog.Builder builder) {

        try {
            visibleDialog = builder.show();
            visibleDialog.setCanceledOnTouchOutside(true);
            visibleDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    visibleDialog = null;
                    onDialogDismiss();
                }
            });
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
    }

    private void onDialogDismiss() {

    }
}
