package ng.com.nhub.paygis;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.audiofx.BassBoost;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import ng.com.nhub.paygis.lib.FileLog;
import ng.com.nhub.paygis.lib.LocaleController;

public class MapLocationPickerActivity extends AppCompatActivity implements
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerDragListener,
        LocationListener,
        OnMapReadyCallback, View.OnClickListener {

    final int PERMISSIONS_REQUEST_USE_LOCATION = 1;
    private ProgressDialog progressDialog;

    double currentLat; //User selected Latitude to be sent to previous activity
    double currentLong; //User selected Longitude to be sent to previous activity
    double currentAccuracy = 0;
    double currentAltitude = 0;

    GoogleMap gMap;


    Boolean useLocationManager = false;

    FloatingActionButton fab;
    SupportMapFragment mapFragment;

    int mapViewState = 0;
    private LocationManager locationManager;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;
    private boolean forceNetwork = false;
    private TextView halfHideTextView;
    private CardView helpCardView;
    private boolean locationServiceAvailable = false;
    private static final long MIN_TIME_BW_UPDATES = 0;//1000 * 60 * 1; // 1 minute
    //The minimum distance to change updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_location_picker);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        halfHideTextView = (TextView) findViewById(R.id.halfHide);
        helpCardView = (CardView) findViewById(R.id.help);
        Button okGotItBtn = (Button) findViewById(R.id.okGotIt);

        okGotItBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeHelpViews();
                SharedPreferences prefs = getApplicationContext().getSharedPreferences("general_settings",
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("location_picker_help", true);
                editor.commit();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        TextView okButton = (TextView) findViewById(R.id.okButton);
//        okButton.setOnClickListener(this);

        mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        currentLong = intent.getDoubleExtra("longitude", 0.0);
        currentLat = intent.getDoubleExtra("latitude", 0.0);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (mapViewState % 2) {
                    case MapPreviewActivity.MAP_TYPE_TERRAIN: {
                        gMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        mapViewState++;
                        return;
                    }
                    case MapPreviewActivity.MAP_TYPE_SATELLITE: {
                        gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        mapViewState++;
                        return;
                    }
                }
            }
        });


        if(Build.VERSION.SDK_INT >= 23 ){

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)
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
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSIONS_REQUEST_USE_LOCATION);
//                }
            }
        }

    }

    private void removeHelpViews(){
        fab.setVisibility(View.VISIBLE);
        helpCardView.setVisibility(View.GONE);
        halfHideTextView.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        getMenuInflater().inflate(R.menu.map_location_picker, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish(); //No parent
            return true;
        }else if (id == R.id.action_location_search) {
            useLocationManager = !useLocationManager;
            if(useLocationManager){
                showProgress();
                initLocationSearch();
            }
            return true;
        }else if (id == R.id.action_location_done) {
            stopLocationSearch();
            Intent data = new Intent();
            data.putExtra("longitude", currentLong);
            data.putExtra("latitude", currentLat);
            data.putExtra("accuracy", currentAccuracy);
            data.putExtra("altitude", currentAltitude);
            setResult(RESULT_OK, data);
            finish(); // ends current activity
            return true;
        }else if (id == R.id.action_back) {
            stopLocationSearch();
            finish(); // ends current activity
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initLocationSearch(){

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            try{

                //To setup location manager
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                //To request location updates
                // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, this);

                this.isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                this.isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (forceNetwork) isGPSEnabled = false;

                if (isGPSEnabled)  {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                }else{
                    displayPromptForEnablingGPS(this);
                }


                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                }

            }catch(Exception e){

            }

        }else{

            if(Build.VERSION.SDK_INT >= 23 ) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_USE_LOCATION);
            }
        }
    }

    public static void displayPromptForEnablingGPS(final Activity activity)
    {

        final AlertDialog.Builder builder =  new AlertDialog.Builder(activity);
        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
        final String message = LocaleController.getString("GPSSettingsOff",
                R.string.GPSSettingsOff);

        builder.setMessage(message)
                .setPositiveButton(LocaleController.getString("OK",
                                R.string.OK),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                activity.startActivity(new Intent(action));
                                d.dismiss();
                            }
                        })
                .setNegativeButton(LocaleController.getString("Cancel",
                                R.string.Cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.cancel();
                            }
                        });
        builder.create().show();
    }

    private void stopLocationSearch(){
        if(locationManager != null){
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission( this,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.removeUpdates(this);
            }
        }
    }

    private void updateCoordinates(Location location){

        currentLat = location.getLatitude();
        currentLong = location.getLongitude();
        currentAccuracy = Double.valueOf(location.getAccuracy());
        currentAltitude = Double.valueOf(location.getAltitude());
        LatLng gpsPoint = new LatLng(currentLat, currentLong);

        gMap.clear();
        gMap.addMarker(new MarkerOptions()
                .position(gpsPoint)
                .title(LocaleController.getString("address_label", R.string.address_label))
                .snippet(LocaleController.getString("roof_top", R.string.roof_top))
                .draggable(true)).showInfoWindow();

        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gpsPoint, 20));

        // Log.i("info", "retnan on drag end :" + currentLat + " dragLong :" + currentLong);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        // TODO Auto-generated method stub
        gMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void onMapReady(GoogleMap map) {

        LatLng nigeria = new LatLng(currentLat == 0.0 ? 9.072264 : currentLat,
                currentLong == 0.0 ? 7.491302 : currentLong);

        // map.setMyLocationEnabled(true);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(nigeria, 20));
        map.addMarker(new MarkerOptions()
                .title(LocaleController.getString("address_label", R.string.address_label))
                .snippet(LocaleController.getString("roof_top", R.string.roof_top))
                .position(nigeria)).showInfoWindow();

        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

//        Toast.makeText(getApplicationContext(),
//                LocaleController.getString("LoadingWait",
//                        R.string.LoadingWait),
//                Toast.LENGTH_SHORT).show();

        SharedPreferences prefs = this.getSharedPreferences("general_settings", Context.MODE_PRIVATE);
        Boolean shownBefore = prefs.getBoolean("location_picker_help", false);
//        Since it's not a frequently accessed screen. Show Help all the time.
        if(shownBefore){
//            removeHelpViews();
        }

        map.setOnMarkerDragListener(this);
        map.setOnMapLongClickListener(this);
        map.setOnMapClickListener(this);

        gMap = map;
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        gMap.clear();
        gMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(LocaleController.getString("address_label", R.string.address_label))
                .snippet(LocaleController.getString("roof_top", R.string.roof_top))
                .draggable(true)).showInfoWindow();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

        LatLng dragPosition = marker.getPosition();
        currentLat = dragPosition.latitude;
        currentLong = dragPosition.longitude;
        // Log.i("info", "retnan on drag end :" + currentLat + " dragLong :" + currentLong);
    }

    @Override
    public void onLocationChanged(Location location) {
        updateCoordinates(location);
        stopLocationSearch();
        hideProgress();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.okButton: {
//                Intent data = new Intent();
//                data.putExtra("longitude", currentLong);
//                data.putExtra("latitude", currentLat);
//                setResult(RESULT_OK, data);
//                finish(); // ends current activity
//                return;
//            }default: {
//                FileLog.e("tmessages", "Unexpected onClick() event from: " + view);
//                return;
//            }
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        stopLocationSearch();
    }

    public void showProgress() {
        if (this.isFinishing() || progressDialog != null) {
            return;
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(LocaleController.getString("Loading", R.string.Loading));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                hideProgress();
            }
        });
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_USE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    showProgress();//reading location
                    initLocationSearch();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}