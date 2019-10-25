package com.example.parkwhere;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, LocationListener {

    private static final String TAG = MapsActivity.class.getSimpleName() ;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LatLng user_loc;
    private DBController controller;
    protected ArrayList<CarPark> nearbyCarParks = new ArrayList<>();
    private GoogleMap mMap;
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    private LocationCallback mLocationCallback;
    private Location mLastKnownLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        String availability = "https://api.data.gov.sg/v1/transport/carpark-availability";
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        mLocationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationCallback = new LocationCallback(){
            public void onLocationResult(LocationResult locationResult){
                handleNewLocation(locationResult.getLastLocation());
            }
        };
        controller = new DBController(getApplicationContext());
        new availabilityReq(getApplicationContext()).execute(availability);

        //new fetchNearbyCarParks().execute("");//async method not using.
        //private Place apporx_place;
        //private double max_likelihood = 0;
        // apporx_place = getLocation();


         //user = new LatLng(user_loc.getLatitude(), user_loc.getLongitude());
    }

   /* private Place getLocation(){
        Places.initialize(getApplicationContext(), "AIzaSyDyZWootdcmcMONm6MfBirKqIrOjGuRLoE");
        PlacesClient placesClient = Places.createClient(this);
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME);
        FindCurrentPlaceRequest request =
                FindCurrentPlaceRequest.builder(placeFields).build();
        // Call findCurrentPlace and handle the response (first check that the user has granted permission).
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            placesClient.findCurrentPlace(request).addOnSuccessListener((new OnSuccessListener<FindCurrentPlaceResponse>() {
                @Override
                public void onSuccess(FindCurrentPlaceResponse response) {
                    for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                        if (placeLikelihood.getLikelihood() > max_likelihood) {
                            max_likelihood = placeLikelihood.getLikelihood();
                            apporx_place = placeLikelihood.getPlace();
                        }

                    }
                }
            })).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                    }
                }
            });
        } else {
            // A local method to request required permissions;
            // See https://developer.android.com/training/permissions/requesting

            getLocationPermission();

        }
        return apporx_place;
    }

    private void getLocationPermission(){

           // NEED TO WRITE LOGIC TO ASK USER FOR PERMISSIONS FOR THE ACCESS.
    }*/

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

           // googleMap.addMarker(new MarkerOptions().position(user).title("User Position Marker"));
           // googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(user, 10));

        mMap = googleMap;
        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();


    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    private void handleNewLocation(Location location) {
        user_loc = new LatLng(location.getLatitude(), location.getLongitude());
        fetchNearbyCarParks(user_loc);
        setMarkers(nearbyCarParks,mMap);
        return;
    }


    private class availabilityReq extends AsyncTask<String, Integer, String> {

        private Context context;

        private availabilityReq(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... strings) {

            RequestQueue queue = Volley.newRequestQueue(context);
            String URL = strings[0];
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, URL, null, response -> {
                        try {
                            JSONObject carPark_info = (JSONObject) response.getJSONArray("items").get(0);
                            JSONArray info = carPark_info.getJSONArray("carpark_data");
                            DBController controller = new DBController(context);
                            controller.getWritableDatabase();
                            String name = "availability";
                            controller.carParkAvailability(info, name);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }, error -> {
                        // TODO: Handle error

                    });
            queue.add(jsonObjectRequest);
            return null;
        }



    }


    private void fetchNearbyCarParks(LatLng user_loc){
        nearbyCarParks = controller.getCarparks(user_loc);
        Log.d("size", String.valueOf(nearbyCarParks.size()));
    }

    private void setMarkers(ArrayList<CarPark> nearbyCarParks, GoogleMap mMap){
        if(nearbyCarParks != null)
        {//create nearbycarpark markers
            for(int i=0;i<nearbyCarParks.size();i++)
            {
                String title = nearbyCarParks.get(i).getAddress();
                double lat = nearbyCarParks.get(i).getLatitude();
                double lng = nearbyCarParks.get(i).getLongitude();
                LatLng markerSet = new LatLng(lat, lng);
                mMap.addMarker(new MarkerOptions().position(markerSet).title(title).snippet("Parking Lots : "+nearbyCarParks.get(i).getParking_lots()).snippet("Free Lots: "+ nearbyCarParks.get(i).getFree_lots()));
                //zoom on last marker for proof of zoom level
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerSet,15.0f));

                if(markerSet != null)
                Log.d("setMarkers","Marker has item");
                Log.d("setMarkers","Address Title " + title + "Lat" + String.valueOf(lat) + "Lng" + String.valueOf(lng));
            }

        }

    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {

                mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,mLocationCallback,Looper.myLooper());
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }



    }


}


