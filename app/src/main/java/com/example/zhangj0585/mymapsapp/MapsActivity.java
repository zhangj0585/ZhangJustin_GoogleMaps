package com.example.zhangj0585.mymapsapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private Location myLocation;
    private EditText locationSearch;
    private GoogleMap mMap;
    private LocationManager locationManager;

    private boolean gotMyLocationOneTIme;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 5;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0.0F;
    private static final int MY_LOC_ZOOM_FACTOR = 17;
    private boolean isNetworkEnabled = false;
    private boolean isGPSEnabled = false;
    private boolean notTrackingMyLocation = true;
    private ArrayList<LatLng> locationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        locationList = new ArrayList<LatLng>();
        // Add a marker in Sydney and move the camera
        LatLng sanDiego = new LatLng(32.855, -117.2255);
        mMap.addMarker(new MarkerOptions().position(sanDiego).title("Born here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sanDiego));
        locationSearch = (EditText) findViewById(R.id.editText_addr);

        gotMyLocationOneTIme = false;
        getLocation();

    }

    public void changeView(View view) {
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //Adda view button and method to switch between satellite and map views

    }


    public void onSearch(View v) {
        String location = locationSearch.getText().toString();

        List<Address> addressList = null;
        List<Address> addressListZip = null;
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);
        Log.d("MyMapsApp", "on Search: location = " + location);
        Log.d("MyMapsApp", "on Search: provider = " + provider);

        LatLng userlocation = null;


        try {
            if (service != null) {
                Log.d("MyMapsApp", "onSearch: LocationManger is not null");
            }
            if ((myLocation = service.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)) != null) {
                userlocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                Log.d("MyMapsApp", "onSearch: using NETWORK_PROVIDER userLocation is:" + myLocation.getLatitude() + " " + myLocation.getLongitude());
                Toast.makeText(this, "UserLog" + myLocation.getLatitude() + myLocation.getLongitude(), Toast.LENGTH_SHORT);
            } else if ((myLocation = service.getLastKnownLocation(LocationManager.GPS_PROVIDER)) != null) {
                userlocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                Log.d("MyMapsApp", "onSearch: using GPS_PROVIDER userLocation is:" + myLocation.getLatitude() + " " + myLocation.getLongitude());
                Toast.makeText(this, "UserLog" + myLocation.getLatitude() + myLocation.getLongitude(), Toast.LENGTH_SHORT);
            } else {
                Log.d("MyMapsApp", "onSearch: myLocation is null");

            }
        } catch (SecurityException | IllegalArgumentException e) {
            Log.d("MyMapsApp", "Expection on getLastKnownLocation");

        }

        if (!location.matches("")) {
            Geocoder geocoder = new Geocoder(this, Locale.US);

            try {
                addressListZip = geocoder.getFromLocationName("92130", 5);
                double la = addressListZip.get(0).getLatitude();
                double lo = addressListZip.get(0).getLongitude();

                addressList = geocoder.getFromLocationName(location, 20,la-5.0/60.0,lo-5.0/60.0,la+5.0/60.0,lo+5.0/60.0);

                Log.d("MyMapsApp", "created addressList");

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!addressList.isEmpty()) {
                Log.d("MyMapsApp", "Addres list size: " + addressList.size());
                for (int i = 0; i < addressList.size(); i++) {
                    Address address = addressList.get(i);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latLng).title(address.getAddressLine(i)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            }
        }

    }

    public void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            //get GPS status
            //isProviderEndabled returns true if user has gps
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled) {
                Log.d("MyMapsApp", "getLocation: GPS is enabled");
            }
            //get Network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (isNetworkEnabled) {
                Log.d("MyMapsApp", "getLocation: Network is enabled");
            }

            if (!isGPSEnabled && !isNetworkEnabled) {

                Log.d("MyMapsApp", "getLocation: no Provider is enabled");

            } else {
                if (isNetworkEnabled) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    Log.d("MyMapsApp", "getLocation: Network is enabled");

                }
                if (isGPSEnabled) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
                    Log.d("MyMapsApp", "getLocation: Network is enabled");
                }
            }
        } catch (Exception e) {
            Log.d("MyMapsApp", "Exception in GPS");
            e.printStackTrace();

        }
    }

    //locationListener is an anonymous inner class
    //setup for callbacks from the requestLocationUpdates
    LocationListener locationListenerNetwork = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            Log.d("MyMapsApp", "getLocation: Network is enabled");
            dropAmarker(LocationManager.NETWORK_PROVIDER);

            if (gotMyLocationOneTIme == false) {
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerGPS);
                gotMyLocationOneTIme = true;

            } else {
                //check self permission
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MyMapsApp", "locationListenerNetwork: status change");

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

    };

    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            dropAmarker(LocationManager.GPS_PROVIDER);

            if(gotMyLocationOneTIme == false){
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerNetwork);
                gotMyLocationOneTIme = true;
            }
            else {
                if(ActivityCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
            }


        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MyMapsApp","locationListenerNetwork: status change");
            Toast.makeText(MapsActivity.this,"status change",Toast.LENGTH_LONG).show();
            switch(status){
                case LocationProvider.AVAILABLE:
                    Log.d("MyMapsApp","locationListenerNetwork: GPS available");
                    Toast.makeText(MapsActivity.this,"location provider available",Toast.LENGTH_LONG).show();
                    break;

                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("MyMapsApp","locationListenerNetwork: GPS out of service");
                    Toast.makeText(MapsActivity.this,"status change",Toast.LENGTH_LONG).show();
                    if(ActivityCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    break;

                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    if(ActivityCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    break;
                default:


            }
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
    public void dropAmarker(String provider) {
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            myLocation = locationManager.getLastKnownLocation(provider);
            LatLng userLocation = null;
            if (myLocation == null) {
                Log.d("MyMapsApp", "dropAMarker: location is null");

            } else {
                userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);  // its set to 17
                if (provider == LocationManager.GPS_PROVIDER) {
                    mMap.addCircle(new CircleOptions().center(userLocation).radius(1).strokeColor(Color.RED).strokeWidth(2).fillColor(Color.RED));
                    mMap.addCircle(new CircleOptions().center(userLocation).radius(3).strokeColor(Color.RED).strokeWidth(2).fillColor(Color.TRANSPARENT));
                } else {
                    mMap.addCircle(new CircleOptions().center(userLocation).radius(1).strokeColor(Color.BLUE).strokeWidth(2).fillColor(Color.BLUE));
                    mMap.addCircle(new CircleOptions().center(userLocation).radius(3).strokeColor(Color.BLUE).strokeWidth(2).fillColor(Color.TRANSPARENT));

                }

                locationList.add(userLocation);
                if(locationList.size()>2)
                {
                    locationList.remove(0);
                }
                mMap.animateCamera(update);
            }
        }


    }


    public void trackMyLocation(View view){

        if(notTrackingMyLocation){

            getLocation();
            Toast.makeText(this, "tracking", Toast.LENGTH_SHORT).show();

            notTrackingMyLocation = false;

        } else if(!notTrackingMyLocation) {
            locationManager.removeUpdates(locationListenerGPS);
            locationManager.removeUpdates(locationListenerNetwork);
            Toast.makeText(this, "tracking", Toast.LENGTH_SHORT).show();

            notTrackingMyLocation = true;
        }
    }


public void clearMarkers (View view){
        mMap.clear();
    }
}