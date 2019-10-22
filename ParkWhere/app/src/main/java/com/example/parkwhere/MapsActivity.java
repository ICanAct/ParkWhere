package com.example.parkwhere;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private FusedLocationProviderClient fusedLocationClient;
    private LatLng user;
    protected ArrayList<CarPark> nearbyCarParks = new ArrayList<>();
    private GoogleMap mMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        String availability = "https://api.data.gov.sg/v1/transport/carpark-availability";
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        new availabilityReq(getApplicationContext()).execute(availability);
        DBController controller = new DBController(getApplicationContext());
        controller.getWritableDatabase();
        fetchNearbyCarParks();//get nearby carparks first

        //new fetchNearbyCarParks().execute("");//async method not using.
        //private Place apporx_place;
        //private double max_likelihood = 0;
        // apporx_place = getLocation();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                user = new LatLng(location.getLatitude(), location.getLongitude());
                if (location != null){
                    Log.d("Location", "Location is not null");
                }
                else {

                }
            }
        });

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

        // Add a marker in Sydney and move the camera
        //LatLng user_pos = apporx_place.getLatLng();
      // final LatLng loc = null;

            googleMap.addMarker(new MarkerOptions().position(user).title("User Position Marker"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(user, 10));

        /*LatLng sydney = new LatLng(1.367551, 103.8535086);
        googleMap.addMarker(new MarkerOptions().position(sydney).title("Some SG place"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
        //setMarkers(nearbyCarParks, googleMap);
        mMap = googleMap;

        if(nearbyCarParks.size() >0)
        setMarkers(nearbyCarParks,mMap);

      // LatLng sydney = new LatLng(-34, 151);


    }

    /*private class  databaseRead extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... path) {
            DBController controller = new DBController(getApplicationContext());
            controller.getWritableDatabase();
            String name = "carparks";
            controller.readXLS(path[0], name);
            return "The values stored into the database successfully!";
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            // Anything to do after database process completes.
        }
    }*/

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
                            DBController controller = new DBController(getApplicationContext());
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

    private class fetchNearbyCarParks extends AsyncTask<String, Integer, Void> {
        //ArrayList<CarPark> nearbyCarParks = new ArrayList<>();
        @Override
        protected Void doInBackground(String... strings) {
            LatLng test = new LatLng(1.367551, 103.8535086);
            DBController dbController = new DBController(getApplicationContext());
            nearbyCarParks = dbController.getCarparks(test);
            if(nearbyCarParks != null)
            {

                for(int i=0;i<nearbyCarParks.size();i++)
                Log.d("nearbyCarpark",nearbyCarParks.get(i).getCar_park_no() + "Lat"+nearbyCarParks.get(i).getLatitude()+"Lng"+nearbyCarParks.get(i).getLongitude());

                for(int i=0;i<nearbyCarParks.size();i++) {
                    Log.d("nearbyCarpark", nearbyCarParks.get(i).getCar_park_no().toString() + "Lat" + nearbyCarParks.get(i).getLatitude() + "Lng" + nearbyCarParks.get(i).getLongitude());
                }

                int size = nearbyCarParks.size();
                Log.d("size", String.valueOf(size));

            }

            return null;
        }
    }
    private void fetchNearbyCarParks(){
        LatLng test = new LatLng(1.367551, 103.8535086);
        DBController dbController = new DBController(getApplicationContext());
        nearbyCarParks = dbController.getCarparks(test);
        if(nearbyCarParks != null)
        {
            for(int i=0;i<nearbyCarParks.size();i++) {
                Log.d("nearbyCarpark", nearbyCarParks.get(i).getCar_park_no().toString() + "Lat" + nearbyCarParks.get(i).getLatitude() + "Lng" + nearbyCarParks.get(i).getLongitude());
            }

            int size = nearbyCarParks.size();
            Log.d("size", String.valueOf(size));

        }
    }

    private void setMarkers(ArrayList<CarPark> nearbyCarParks, GoogleMap mMap){
        if(nearbyCarParks != null)
        {//create nearbycarpark markers
            for(int i=0;i<nearbyCarParks.size();i++)
            {
                String title = nearbyCarParks.get(i).getAddress().toString();
                double lat = nearbyCarParks.get(i).getLatitude();
                double lng = nearbyCarParks.get(i).getLongitude();
                LatLng markerSet = new LatLng(lat, lng);
                mMap.addMarker(new MarkerOptions().position(markerSet).title(title + " item" + (i)));

                //zoom on last marker for proof of zoom level
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerSet,15.0f));

                if(markerSet != null)
                Log.d("setMarkers","Marker has item");
                Log.d("setMarkers","Address Title " + title + "Lat" + String.valueOf(lat) + "Lng" + String.valueOf(lng));
            }

        }
    }



}


