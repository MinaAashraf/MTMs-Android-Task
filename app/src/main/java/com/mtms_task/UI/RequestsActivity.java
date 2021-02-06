package com.mtms_task.UI;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.mtms_task.Adapters.RequestsAdapter;
import com.mtms_task.R;
import com.mtms_task.POJO.Request;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class RequestsActivity extends AppCompatActivity implements OnMapReadyCallback {

    //**************************************************************************************************
    private GoogleMap mMap;
    RequestsViewModel viewModel;
    Snackbar snackbar;
    View customSnackBar;
    ArrayList<Request> requestsList = new ArrayList<>();
    RequestsAdapter requestsAdapter;
    ViewPager requestsViewPager;
    private static final String Driver_Id = "d" + (new Random().nextInt(3) + 1);    //generate driver id using random numbers from 1 to 3 ->  d1 : d3
    private static final int LOCATION_PERMISSION_REQUEST = 1;
    FusedLocationProviderClient fusedLocationProviderClient;

    //**************************************************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setActionBarTitleStyle("Delivery On Fire");   // set font and title to the action bar title

        //**************************************************************************************************
        // navigation drawer part
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //**************************************************************************************************
        // take reference of map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //**************************************************************************************************
        //build custom snack bar to have vertical view pager inside it to swipe between requests
        buildCustomSncakBar();

        //**************************************************************************************************
        // change the map locations markers in every time swipe the view pager
        requestsViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                try {
                    Request request = requestsList.get(position);
                    try {
                        setMarkers(request);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //**************************************************************************************************
        //interact with view model and live data
        viewModel = ViewModelProviders.of(this).get(RequestsViewModel.class);
        // observe updates of the requests
        viewModel.readRequests(Driver_Id).observe(this, new Observer<ArrayList<Request>>() {
            @Override
            public void onChanged(ArrayList<Request> requests) {
                requestsList.clear();
                if (!requests.isEmpty()) {
                    if (requestsViewPager.getCurrentItem() == 0) { // set marker of the first request in the map
                        Request firstRequest = requests.get(0);
                        try {
                            setMarkers(firstRequest);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        requestsList.addAll(requests);
                        requestsAdapter.setRequestsList(requestsList);
                        requestsAdapter.notifyDataSetChanged();
                        snackbar.show();
                    }
                } else // there is no requests -> hide the snack bar..
                    snackbar.dismiss();
            }
        });

    }

    //**************************************************************************************************
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.dark_map)); // make map in dark mode style
        mMap.getUiSettings().setZoomControlsEnabled(true);    // show zoom button on the map
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setPadding(0, 400, 0, 1000);      // this row for reposition the zoom button on the map

        checkLocationPermission();         // check location permission to get current location of the user
    }

    //**************************************************************************************************
    // set custom markers to the map
    Marker sourMarker = null, desMarker = null;

    private void setMarkers(Request request) throws IOException {
        if (mMap != null) {

            //remove last markers if exist
            if (sourMarker != null)
                sourMarker.remove();
            if (desMarker != null)
                desMarker.remove();


            LatLng source = new LatLng(request.getSourceLatitude(), request.getSourceLongitude());
            // add custom marker with circle background and text
            sourMarker = mMap.addMarker(new MarkerOptions().position(source)
                    .icon(BitmapDescriptorFactory.fromBitmap(generateBitmap("" + "S", R.drawable.circle2, Color.BLACK)))
                    .title("Source").flat(true));

            LatLng destination = new LatLng(request.getDestinationLatitude(), request.getDestinationLongitude());
            desMarker = mMap.addMarker(new MarkerOptions().position(destination)
                    .icon(BitmapDescriptorFactory.fromBitmap(generateBitmap("" + request.getClientName().charAt(0), R.drawable.circle, Color.WHITE)))
                    .title("Destination").flat(true));

            LatLng latLng = new LatLng((request.getDestinationLatitude() + request.getSourceLatitude()) / 2
                    , (request.getDestinationLongitude() + request.getSourceLongitude()) / 2);
            //animate camera
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));

            // draw line between source and destination
            drawPolyline(source, destination);
        }
    }

    //**************************************************************************************************
    // generate bitmap to the custom marker
    public Bitmap generateBitmap(String text, int background, int color) {
        View cluster = LayoutInflater.from(this).inflate(R.layout.custom_marker,
                null);

        TextView clusterText = (TextView) cluster.findViewById(R.id.marker_text);
        clusterText.setText(text);
        clusterText.setTextColor(color);
        clusterText.setBackgroundResource(background);

        cluster.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        cluster.layout(0, 0, cluster.getMeasuredWidth(), cluster.getMeasuredHeight());

        final Bitmap clusterBitmap = Bitmap.createBitmap(cluster.getMeasuredWidth(),
                cluster.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(clusterBitmap);
        cluster.draw(canvas);

        return clusterBitmap;
    }

    //**************************************************************************************************

    Polyline line = null;

    // draw line between two points
    public void drawPolyline(LatLng latLng1, LatLng latLng2) {
        // remove the last line if exist
        if (line != null)
            line.remove();

        PolylineOptions options = new PolylineOptions().width(15).color(Color.GREEN).geodesic(true);
        options.add(latLng1);
        options.add(latLng2);
        line = mMap.addPolyline(options);

    }

    //**************************************************************************************************
    class NoSwipeBehavior extends BaseTransientBottomBar.Behavior {

        @Override
        public boolean canSwipeDismissView(View child) {
            return false;
        }
    }

    private void buildCustomSncakBar() {
        CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinatorLay);
        snackbar = Snackbar.make(coordinatorLayout, "", Snackbar.LENGTH_INDEFINITE)
                .setBehavior(new NoSwipeBehavior());
        customSnackBar = getLayoutInflater().inflate(R.layout.custom_snackbar, null);
        requestsViewPager = customSnackBar.findViewById(R.id.requests_pager);             // take reference of the view pager in custom snack bar layout
        requestsAdapter = new RequestsAdapter(this, Driver_Id);
        requestsViewPager.setAdapter(requestsAdapter);
        snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
        snackbarLayout.setPadding(0, 0, 0, 0);
        snackbarLayout.addView(customSnackBar, 0);
    }

    //**************************************************************************************************
    // set custom font and size to action bar title
    public void setActionBarTitleStyle(String txt) {
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        LayoutInflater inflator = LayoutInflater.from(this);
        View view = inflator.inflate(R.layout.actionbar_title_style, null);
        TextView title = view.findViewById(R.id.title);
        title.setText(txt);
        getSupportActionBar().setCustomView(view);
    }

    //**************************************************************************************************
    //  get current location to the user
    // check location permission or request user to permit the app to access location
    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkDeviceGps();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    //**************************************************************************************************
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
            else {
                checkDeviceGps();
            }
        }
    }

    //**************************************************************************************************
    // get last location from fusedLocationProviderClient object
    private void getLastLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this
                , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null)
                            updateUi(location);
                        else
                             requestLocationUpdates();       // track location until get not null location
                    }
                });
    }

    //**************************************************************************************************
    // put the location cooridinates on the map
    private void updateUi(Location location) {

        if (ActivityCompat.checkSelfPermission(this
                , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "return", Toast.LENGTH_SHORT).show();
            return;
        }
        if (location != null) {
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.setMyLocationEnabled(true);
            mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Me"));
        }

    }

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;


    public void requestLocationUpdates() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                        updateUi(location);
                    }
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return ;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    //**************************************************************************************************
    // check if gps is enabled or not
    public void checkDeviceGps() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        } else
            getLastLocation();
    }

    //**************************************************************************************************
    // build dialog to ask user to open gps
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {

                        startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    //**************************************************************************************************
    //check if user open gps or not
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                getLastLocation();
            }
        }
    }
}
