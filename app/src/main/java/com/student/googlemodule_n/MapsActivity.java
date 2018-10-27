package com.student.googlemodule_n;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;


import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMarkerClickListener,  //for route
        GoogleMap.OnMarkerDragListener{

    private static final String[] COUNTRIES = new String[]{
      "Afghanistan","Albania","Algeria","Andorra","Angola"
    };

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location lastLocation;
    private Marker currLocMarker;

    double end_latitude, end_longitude;//route
    Marker mCurrLocationMarker;
    double latitude, longitude; //route

    boolean btn_traffc_clicked=false;
    ArrayList<LatLng> listPoints;
    private int proximityRadius = 8000;

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final int Request_User_Location_Code=99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Check location permission
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        //Check if Google Play Services Available or not
        if (!CheckGooglePlayServices()) {
            Log.d("onCreate", "Finishing test case since Google Play Services are not available");
            finish();
        }
        else {
            Log.d("onCreate","Google Play Services available.");
        }


        //autocomplete searching with predefined results
        String[] countries =getResources().getStringArray(R.array.countries); //get data from string rsrc file


        listPoints = new ArrayList<>();

        AutoCompleteTextView editText = findViewById(R.id.actv);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, countries);
        editText.setAdapter(adapter);

        editText.setAdapter(adapter);
        //autocomplete searching with predefined results

        if (!isGooglePlayServicesAvailable()) {
            finish();
        }

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            checkUserLocPermission();
        }


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry("PH")
                .build();

        autocompleteFragment.setFilter(typeFilter);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {


                String address = place.getAddress().toString();

                Toast.makeText(MapsActivity.this,"Place: " + place.getName() +" address: ",Toast.LENGTH_LONG).show();

//                List<Address> addressList = null;

                MarkerOptions userMarkerOptions = new MarkerOptions();

                LatLng latLng = new LatLng(place.getLatLng().latitude,place.getLatLng().longitude); //lat long


                userMarkerOptions.position(latLng);
                userMarkerOptions.title(address);
                userMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                mMap.addMarker(userMarkerOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
            }

        @Override
        public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("", "An error occurred: " + status);
            }
        });


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private boolean CheckGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        0).show();
            }
            return false;
        }
        return true;
    }


    private String getRequestUrl(LatLng origin, LatLng dest) {
        //Value of origin
        String str_org = "origin=" + origin.latitude +","+origin.longitude;
        //Value of destination
        String str_dest = "destination=" + dest.latitude+","+dest.longitude;
        //Set value enable the sensor
        String sensor = "sensor=false";
        //Mode for find direction
        String mode = "mode=driving";
        //Build the full param
        String param = str_org +"&" + str_dest + "&" +sensor+"&" +mode;
        //Output format
        String output = "json";
        //Create url to request
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param;
        return url;
    }


    public void onClick(View v){
        String company = "Incorporated";
        Object transferData[] = new Object[2];
        CompaniesNearby companiesNearby = new CompaniesNearby();

        Object dataTransfer[] = new Object[2]; //route

        switch(v.getId()){
            case R.id.btn_companies_nearby:
                mMap.clear(); //remove all markers in map
                String url = getUrl(latitude, longitude, company);
                transferData[0] = mMap;
                transferData[1] = url;

                companiesNearby.execute(transferData);
                Toast.makeText(this,"Searching for nearby companies..",Toast.LENGTH_LONG).show();
                Toast.makeText(this,"Showing nearby companies....",Toast.LENGTH_LONG).show();
                break;
            case R.id.btn_traffic:

                if(btn_traffc_clicked)
                { btn_traffc_clicked=false;
                    mMap.setTrafficEnabled(false);
                }
                else
                {btn_traffc_clicked=true;
                    mMap.setTrafficEnabled(true);
                }

                break;
//route
            case R.id.btn_show_route:
                dataTransfer = new Object[3];
                url = getDirectionsUrl();
                GetDirectionsData getDirectionsData = new GetDirectionsData();
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                dataTransfer[2] = new LatLng(end_latitude, end_longitude);
                getDirectionsData.execute(dataTransfer);

                break;
                //route
        }

    }

    private String getDirectionsUrl()
    {
        //https://maps.googleapis.com/maps/api/directions/json?
        StringBuilder googleDirectionsUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionsUrl.append("origin="+latitude+","+longitude);

        Toast.makeText(this,"origin="+latitude+","+longitude,Toast.LENGTH_LONG).show();
        Log.i("origin is here: ", "origin="+latitude+","+longitude);


        googleDirectionsUrl.append("&destination="+end_latitude+","+end_longitude);
        Toast.makeText(this,"destination="+end_latitude+","+end_longitude,Toast.LENGTH_LONG).show();
        Log.i("dest is here: ", "destination="+end_latitude+","+end_longitude);
        googleDirectionsUrl.append("&key="+"AIzaSyB1AnOLlzbuFSbZymIrBixFbIe68Kt0j4o"); //noheal My Project

        return googleDirectionsUrl.toString();
    }


   /* public void onClick(View v){
        switch(v.getId()){
            case R.id.btn_search:
                EditText addressEditTxt = findViewById(R.id.location_search);
                String address = addressEditTxt.getText().toString();

                List<Address> addressList = null;

                MarkerOptions userMarkerOptions = new MarkerOptions();


                if(!TextUtils.isEmpty(address))//if not null
                {
                    Geocoder geocoder = new Geocoder(this);
                    try {
                        Toast.makeText(this,"Processing search",Toast.LENGTH_LONG).show();
                        addressList=geocoder.getFromLocationName(address, 6);

                        if(addressList!=null){

                            for (int i =0; i<addressList.size(); i++){
                                Address userAddress = addressList.get(i);
                                LatLng latLng = new LatLng(userAddress.getLatitude(),userAddress.getLongitude()); //lat long

                                userMarkerOptions.position(latLng);
                                userMarkerOptions.title(address);
                                //   markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                                userMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                                mMap.addMarker(userMarkerOptions);
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

                            }
                        }else{
                            Toast.makeText(this,"Location not found",Toast.LENGTH_LONG).show();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(this,"Please input location",Toast.LENGTH_LONG).show();
                }
                break;
        }

    }*/












    @Override //global search of location
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i("", "Place: " + place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i("", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMarkerDragListener(this);
        mMap.setOnMarkerClickListener(this);

    }


    //for requesting directions
    private String getUrl(Double latitude, Double longitude, String nearbyCompany){


        // StringBuilder googleUrL = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522,151.1957362&radius=1500&type=restaurant&keyword=cruise&key=AIzaSyAICdIMoPcFTpJd9TgVKEaNH6YSUAzCcTE");

        StringBuilder googleUrL = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googleUrL.append("location=" + latitude +","+ longitude);
        googleUrL.append("&radius="+proximityRadius);
        googleUrL.append("&sensor=true");
        googleUrL.append("&type="+ nearbyCompany);
        googleUrL.append("&key="+ "AIzaSyDumwR86WZSPcMeg5wbWNUOo3x4mZVF4ag"); //directions api key //MyProjectNoheal
        Log.d("MapsActivity","url="+ googleUrL.toString());
        return googleUrL.toString();

    }



    public boolean checkUserLocPermission(){ //checks permission
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
                {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
                }
            else{
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
                }
            return false;
        }else
            {
            return true;
            }
    }


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case Request_User_Location_Code:
                if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
                    {
                        if(googleApiClient==null){
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }else //permission denied
                {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
                return;
        }
    }


    protected synchronized void buildGoogleApiClient(){
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }


    @Override
    public void onLocationChanged(Location location) {

        latitude = location.getLatitude();
        longitude=location.getLongitude(); //origin is set here



        lastLocation = location;
        if (currLocMarker!=null)
        {  currLocMarker.remove();}

        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude()); //lat, long
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.draggable(true);
        markerOptions.title("Your are here");
     //   markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker());

        currLocMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(11));

        Toast.makeText(MapsActivity.this,"Your Current Location", Toast.LENGTH_LONG).show();

        if(googleApiClient!=null){
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
            Log.d("onLocationChanged", "Removing Location Updates");
        }
       // mMap.setTrafficEnabled(true); //shows traffic onCreate


    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1100); //seconds
        locationRequest.setFastestInterval(1100);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,this);

       /*     mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations, this can be null.
                            location.getLongitude();
                            location.getLatitude();
                            if (location != null) {
                                Toast.makeText(MapsActivity.this, "Failed to retrieve location.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });*/
        }

    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.setDraggable(true);
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        end_latitude = marker.getPosition().latitude;
        end_longitude =  marker.getPosition().longitude;

        Log.d("end_lat",""+end_latitude);
        Log.d("end_lng",""+end_longitude);

        Toast.makeText(this,"org_lat: "+latitude+" org_long: "+longitude +" end_lat: "+end_latitude+" end_long: "+end_longitude,Toast.LENGTH_LONG).show();
//14.688292, 121.122253 paraiso
    }

    public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>> > {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionsParser directionsParser = new DirectionsParser();
                routes = directionsParser.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            //Get list route and display it into the map

            ArrayList points = null;

            PolylineOptions polylineOptions = null;

            for (List<HashMap<String, String>> path : lists) {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                for (HashMap<String, String> point : path) {
                    double lat = Double.parseDouble(point.get("lat"));
                    double lon = Double.parseDouble(point.get("lon"));

                    points.add(new LatLng(lat,lon));
                }

                polylineOptions.addAll(points);
                polylineOptions.width(15);
                polylineOptions.color(Color.BLUE);
                polylineOptions.geodesic(true);
            }

            if (polylineOptions!=null) {
                mMap.addPolyline(polylineOptions);
            } else {
                Toast.makeText(getApplicationContext(), "Direction not found!", Toast.LENGTH_SHORT).show();
            }

        }
    }
}





