package com.itshareplus.googlemapdemo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import Modules.DirectionFinder;
import Modules.DirectionFinderListener;
import Modules.Route;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;





public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener, NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap mMap;
    private Button btnFindPath;
    private EditText etOrigin;
    private EditText etDestination;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Marker> stopMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;


    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastlocation;
    private Marker currentLocationmMarker;
    private DrawerLayout drawer;

    int PROXIMITY_RADIUS = 10000;
    public static final int REQUEST_LOCATION_CODE = 99;
    double latitude = 40.906432, longitude= -73.110239;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission();

        }
        //navigation bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);





        btnFindPath = (Button) findViewById(R.id.btnFindPath);
        etOrigin = (EditText) findViewById(R.id.etOrigin);
        etDestination = (EditText) findViewById(R.id.etDestination);

        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });
    }
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_event:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new EventFragment()).commit();
                break;
            case R.id.nav_fuel:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new FuelFragment()).commit();
                break;
            case R.id.nav_beenhere:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new beenhereFragment()).commit();
                break;
            case R.id.nav_share:
                Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_send:
                Toast.makeText(this, "Send", Toast.LENGTH_SHORT).show();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void sendRequest() {
        String origin = etOrigin.getText().toString();
        String destination = etDestination.getText().toString();
        if (origin.isEmpty()) {
            Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng hcmus = new LatLng(40.906432, -73.110239);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hcmus, 15));
        originMarkers.add(mMap.addMarker(new MarkerOptions()
                .title("Chapin Apartments, G Block")
                .position(hcmus)));

//        LatLng event_1 = new LatLng(40.906432, -73.110239);
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(event_1, 18));
 //       originMarkers.add(mMap.addMarker(new MarkerOptions()
  //              .title("Concert")
   //             .position(event_1)));
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

// Store a data object with the polyline, used here to indicate an arbitrary type.

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
    }


    public void onClick(View v)
    {
        Object dataTransfer[] = new Object[2];
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();

        switch(v.getId())
        {
            case R.id.b_fuel_station:
                mMap.clear();
                String fuelStation = "fuel stations";
                String url = getUrl(latitude, longitude,fuelStation);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                getNearbyPlacesData.execute(dataTransfer);
                Toast.makeText(MapsActivity.this, "Showing Nearby Fuel Stations", Toast.LENGTH_SHORT).show();
                break;

            case R.id.b_events:
                mMap.clear();
               // String events = "concerts";
                //url = getUrl(latitude, longitude,events);
                //dataTransfer[0] = mMap;
                //dataTransfer[1] = url;
                //getNearbyPlacesData.execute(dataTransfer);
                //Toast.makeText(MapsActivity.this, "Showing Nearby events", Toast.LENGTH_SHORT).show();
                LatLng event1 = new LatLng(40.6828991,-73.9763336);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(event1, 15));
                originMarkers.add(mMap.addMarker(new MarkerOptions()
                        .title("SHAWN MENDES: THE TOUR,Aug 10 2018,Barclays Center,Brooklyn,NY")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_loudspeaker))
                        .position(event1)));

                LatLng event2 = new LatLng(40.8753968,-73.1968616);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(event1, 15));
                originMarkers.add(mMap.addMarker(new MarkerOptions()
                        .title("AMC Loews Stony Brook ,NY")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_loudspeaker))
                        .position(event2)));

                LatLng event3 = new LatLng(40.8810322,-73.4232376);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(event1, 15));
                originMarkers.add(mMap.addMarker(new MarkerOptions()
                        .title("AMC Loews, Huntington ,NY")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_loudspeaker))
                        .position(event3)));

                LatLng event4 = new LatLng(40.7724385,-73.9718553);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(event1, 15));
                originMarkers.add(mMap.addMarker(new MarkerOptions()
                        .title("Liam Payne and J Balvin: Concert,May 15 2018,Central Park,NY")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_loudspeaker))
                        .position(event4)));
                break;

        }
    }

    private String getUrl(double latitude, double longitude, String nearbyPlace ){

        StringBuilder googlePlcaeUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlcaeUrl.append("location="+latitude+","+longitude);
        googlePlcaeUrl.append("&radius="+ PROXIMITY_RADIUS);
        googlePlcaeUrl.append("&type="+nearbyPlace);
        googlePlcaeUrl.append("&sensor=true");
        googlePlcaeUrl.append("&key="+"AIzaSyCJLUhVMQoqa90o6zX8ruaura9KP3eiCKs");
        Log.d("url" , googlePlcaeUrl.toString());
        return googlePlcaeUrl.toString();

    }


    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
       mMap.clear();
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();
        stopMarkers = new ArrayList<>();
        int a = 200;
        List<Route> routes2 = new ArrayList<>();


        for (Route route : routes) {

        }

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            ((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            ((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                    .title(route.startAddress)
                    .position(route.startLocation)));
            if (a > 100) {
                stopMarkers.add(mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_gas_station_48))
                        .title("fuel stop: Gas Station, Stony Brook Road, 11790")
                        .position(new LatLng(40.8679869,-73.1330191))));

                stopMarkers.add(mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_gas_station_48))
                        .title("Shells, Stony Brook Road, 11790")
                        .position(new LatLng(40.9176380,-73.0917510))));

                stopMarkers.add(mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_gas_station_48))
                        .title("fuel ")
                        .position(new LatLng(40.,-73.))));

                stopMarkers.add(mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_gas_station_48))
                        .title("fuel stop")
                        .position(new LatLng(40.,-73.))));

                stopMarkers.add(mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_gas_station_48))
                        .title("fuel stop")
                        .position(new LatLng(40.8679869,-73.1330191))));

                stopMarkers.add(mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_gas_station_48))
                        .title("fuel stop")
                        .position(new LatLng(40.8679869,-73.1330191))));
            }



            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);

            for (int i = 0; i < route.points.size(); i++){
               // if(i == 2){
                  //  polylineOptions.add(new LatLng(40.768805, -73.530591));
                //}
                    polylineOptions.add(route.points.get(i));
            }
            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }
    protected synchronized void bulidGoogleApiClient() {
        client = new GoogleApiClient.Builder(this).addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this).addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this).addApi(LocationServices.API).build();
        client.connect();

    }


    public void onLocationChanged(Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        lastlocation = location;
        if(currentLocationmMarker != null)
        {
            currentLocationmMarker.remove();

        }
        Log.d("lat = ",""+latitude);
        LatLng latLng = new LatLng(location.getLatitude() , location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        currentLocationmMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(10));

        if(client != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, (LocationListener) this);
        }
    }


    public void onConnected(@Nullable Bundle bundle) {

        locationRequest = new LocationRequest();
        locationRequest.setInterval(100);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, (LocationListener) this);
        }
    }

    public boolean checkLocationPermission()
    {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)  != PackageManager.PERMISSION_GRANTED )
        {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION },REQUEST_LOCATION_CODE);
            }
            else
            {
                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION },REQUEST_LOCATION_CODE);
            }
            return false;

        }
        else
            return true;
    }
}
