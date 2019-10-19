package com.example.parkwhere;

import androidx.fragment.app.FragmentActivity;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;

import java.io.File;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "";
    private GoogleMap mMap;
    //private Place apporx_place;
    //private double max_likelihood = 0;

    private String path = "carpark_info.xlsx";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        new databaseRead().execute(path); // fires up the async task for the database creation.
      // apporx_place = getLocation();
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
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //LatLng user_pos = apporx_place.getLatLng();
       LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Sydney Position Marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private class databaseRead extends AsyncTask<String, Integer, String> {

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
    }
}
