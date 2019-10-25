package com.example.parkwhere;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, LocationListener {

    private static final String TAG = MapsActivity.class.getSimpleName() ;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LatLng user_loc;
    private DBController controller;
    protected ArrayList<CarPark> nearbyCarParks = new ArrayList<>();
    protected ArrayList<CarPark> searchCarparks = new ArrayList<>();
    private GoogleMap mMap;
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    private LocationCallback mLocationCallback;
    private Location mLastKnownLocation;
    protected Place destination;
    private Context context;
    private boolean zoomedToLoc = false;

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

        context = getApplicationContext();
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.api_key), Locale.US);
        }

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);


        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                destination = place;
                Log.i(TAG, "Place: " + destination.getName() + ", " + destination.getId()+ destination.getLatLng());
                Log.i(TAG, "Place: " + destination.getName() + ", " + place.getLatLng());

                fetchNearbyCarParks(destination.getLatLng());
                setMarkers(nearbyCarParks,mMap);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination.getLatLng(),15.0f));
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }


        });

        //async fragments to map layout
        mapFragment.getMapAsync(this);

        //set location request to seek location update on interval
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener(){
            @Override
            public boolean onMyLocationButtonClick()
            {
                //mMap.clear();//clear any other previous markers
                Log.d("user_loc","at MYLocationButtonClick"+user_loc.latitude + user_loc.longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(user_loc,15.0f));
                //fetchNearbyCarParks(user_loc);
                //setMarkers(nearbyCarParks,mMap);

                return false;
            }
        });
        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    private void handleNewLocation(Location location) {
        //mMap.clear();//clears map of all markers even circle drawings and such first before updating more markers.
        if(location != null)
            user_loc = new LatLng(location.getLatitude(), location.getLongitude());
        Log.d("user_loc","at handleNewLocation lat "+user_loc.latitude +"Lng "+ user_loc.longitude);
        fetchNearbyCarParks(user_loc);
        setMarkers(nearbyCarParks,mMap);
        if(!zoomedToLoc){//so that only zooms once in the entire app when handlenewlocation
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(user_loc,15.0f));
        zoomedToLoc = true;
        }
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
        nearbyCarParks.clear();//clears array of previous location nearbycarparks items
        nearbyCarParks = controller.getCarparks(user_loc);
        Log.d("size", String.valueOf(nearbyCarParks.size()));
    }

    private void setMarkers(ArrayList<CarPark> nearbyCarParks, GoogleMap mMap){

        if(nearbyCarParks.size()>0)
        {//create nearbycarpark markers
            for(int i=0;i<nearbyCarParks.size();i++)
            {
                CarPark carPark = nearbyCarParks.get(i);
                String title = nearbyCarParks.get(i).getAddress();
                double lat = nearbyCarParks.get(i).getLatitude();
                double lng = nearbyCarParks.get(i).getLongitude();
                LatLng markerSet = new LatLng(lat, lng);
                String carPark_info = "Car Park Type: "+ carPark.getCar_park_type()+"\nFree Parking: "+carPark.getFree_parking()+"\nTotal Parking Lots: "+carPark.getParking_lots()+"\nAvailable Lots: "+carPark.getFree_lots();
                mMap.addMarker(new MarkerOptions().position(markerSet).title(title).snippet(carPark_info));
                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        LinearLayout info = new LinearLayout(context);
                        info.setOrientation(LinearLayout.VERTICAL);

                        TextView title = new TextView(context);
                        title.setTextColor(Color.BLACK);
                        title.setGravity(Gravity.CENTER);
                        title.setTypeface(null, Typeface.BOLD);
                        title.setText(marker.getTitle());

                        TextView snippet = new TextView(context);
                        snippet.setTextColor(Color.GRAY);
                        snippet.setText(marker.getSnippet());

                        info.addView(title);
                        info.addView(snippet);
                        return info;
                    }
                });
                //zoom on last marker for proof of zoom level
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerSet,15.0f));

                if(markerSet != null)
                Log.d("setMarkers","Marker has item");
                Log.d("setMarkers","Address Title " + title + "Lat" + String.valueOf(lat) + "Lng" + String.valueOf(lng));
            }

        }
        else
            Log.d("setMarkers","No Nearby Carparks");

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


