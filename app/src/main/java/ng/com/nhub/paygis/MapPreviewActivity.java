package ng.com.nhub.paygis;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import ng.com.nhub.paygis.lib.LocaleController;

public class MapPreviewActivity extends AppCompatActivity implements
        GoogleMap.OnMapClickListener,
        OnMapReadyCallback {

    static final int MAP_TYPE_TERRAIN = 0;
    static final int MAP_TYPE_SATELLITE = 1;

    FloatingActionButton fab;
    SupportMapFragment mapFragment;

    int mapViewState = 0;
    double longitude;
    double latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_preview);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        Intent intent = getIntent();
        longitude = intent.getDoubleExtra("longitude", 0.0);
        latitude = intent.getDoubleExtra("latitude", 0.0);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (mapViewState % 2) {
                    case MapPreviewActivity.MAP_TYPE_TERRAIN: {
                        mapFragment.getMap().setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        mapViewState++;
                        return;
                    }
                    case MapPreviewActivity.MAP_TYPE_SATELLITE: {
                        mapFragment.getMap().setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        mapViewState++;
                        return;
                    }
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish(); //No parent
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap map) {

        // Log.i("info", "retnan locations: " + String.valueOf(latitude) + ", " +  String.valueOf(longitude));

        LatLng userLocation = new LatLng(latitude, longitude);

        // map.setMyLocationEnabled(true);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16));
        map.addMarker(new MarkerOptions()
                .title(LocaleController.getString("address_label", R.string.address_label))
                .snippet(LocaleController.getString("your_roof_top", R.string.your_roof_top))
                .position(userLocation));
        // Other supported types include: MAP_TYPE_NORMAL,
        // MAP_TYPE_TERRAIN, MAP_TYPE_HYBRID and MAP_TYPE_NONE
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
//        mapObject = map;
        fab.setVisibility(View.VISIBLE);

        Toast.makeText(getApplicationContext(),
                LocaleController.getString("LoadingWait",
                        R.string.LoadingWait),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mapFragment.getMap().animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }
}
