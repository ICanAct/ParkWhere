package com.example.parkwhere;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.Places;
import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "";
    private GoogleMap mMap;
    private Place apporx_place;
    private double max_likelihood = 0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

       apporx_place = getLocation();
    }

    private Place getLocation(){
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

        // Add a marker in Sydney and move the camera
        LatLng user_pos = apporx_place.getLatLng();
       // LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(user_pos).title("User Position Marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(user_pos));
    }
}
