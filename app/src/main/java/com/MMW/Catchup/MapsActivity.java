package com.MMW.Catchup;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.ArraySet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;

import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.MMW.Catchup.models.Route;
import com.MMW.Catchup.models.User;
import com.MMW.Catchup.modules.DirectionFinder;
import com.MMW.Catchup.modules.DirectionFinderListener;
import com.MMW.Catchup.modules.Routes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MapsActivity extends AppCompatActivity implements View.OnClickListener,OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener,DirectionFinderListener,GoogleMap.OnMarkerClickListener {

    private static final int MY_PERMISSION_REQUEST_FINE_LOCATION = 101; // Request Code which will be passed to onRequestPermissionResult
    private static final int MY_PERMISSION_REQUEST_COARSE_LOCATION = 102;
    Marker marker,busMarker;
    private GoogleMap mMap;
    private FusedLocationProviderApi locationProvider = LocationServices.FusedLocationApi;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Double myLatitude;
    private Double myLongitude;
    private boolean permissionIsGranted = false;
    private EditText searchBar;
    private Button searchButton,findRoute;
    private Circle circle;

    ArrayList markerPoints= new ArrayList();

    FirebaseAuth mAuth;
    List<User> coordinate;
    FirebaseDatabase database;
    DatabaseReference myRef;

    boolean isSharing;
    Calendar calendar;
    SimpleDateFormat simpleDateFormat;


    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarker = new ArrayList<>();
    private List<Polyline> polyLinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;

    Map<String,Route> availableBuses = new HashMap<>();

    Ringtone r;

    enum colors{
        RED("-65536"),
        BLUE ("-16776961"),
        GREEN ("-16711936");
        // declaring private variable for getting values
        private String color;

        // getter method
        public String getColor() {
            return this.color;
        }

        // enum constructor - cannot be public or protected
        private colors(String color) {
            this.color = color;
        }
    }

    int selectedColor=-65536;

    User fetchUser= new User();

    boolean notificationBar=false;

    private static final String[] busTypes = new String[]{"NORMAL", "SEMI LUXURY","LUXURY","SUPER LUXURY"};

    private static String selectedBusTypeIcon=null;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);



        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Driver");
        searchBar = findViewById(R.id.search_bar);
        searchButton = findViewById(R.id.buttonSearch);

        //Bus icons in map activity
        findViewById(R.id.normal_bus_icon).setOnClickListener(this);
        findViewById(R.id.luxury_bus_icon).setOnClickListener(this);
        findViewById(R.id.semiluxury_bus_icon).setOnClickListener(this);
        findViewById(R.id.super_luxury_bus_icon).setOnClickListener(this);

        coordinate = new ArrayList<>();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        //Visibility of search bar
        FirebaseDatabase.getInstance().getReference("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        fetchUser = dataSnapshot.getValue(User.class);

                        if (fetchUser.getUserType().equals("Passenger")) {
                            findViewById(R.id.search_bar).setVisibility(View.VISIBLE);
                            findViewById(R.id.buttonSearch).setVisibility(View.VISIBLE);
                            findViewById(R.id.textViewDistance).setVisibility(View.VISIBLE);
                            findViewById(R.id.textViewTime).setVisibility(View.VISIBLE);
                            findViewById(R.id.buttonFind_route).setVisibility(View.VISIBLE);

                        } else if (fetchUser.getUserType().equals("Driver")) {

                            DisplayMetrics displaymetrics = new DisplayMetrics();
                            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                            int height = displaymetrics.heightPixels;

                            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

                            mapFragment = (SupportMapFragment) (getSupportFragmentManager()
                                    .findFragmentById(R.id.map));
                            ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();


                            findViewById(R.id.tableRow1).setVisibility(View.GONE);
                            findViewById(R.id.search_bar).setVisibility(View.GONE);
                            findViewById(R.id.buttonSearch).setVisibility(View.GONE);
                            findViewById(R.id.textViewDistance).setVisibility(View.GONE);
                            findViewById(R.id.textViewTime).setVisibility(View.GONE);
                            findViewById(R.id.buttonFind_route).setVisibility(View.GONE);
                            findViewById(R.id.clock).setVisibility(View.GONE);
                            findViewById(R.id.distance).setVisibility(View.GONE);


                            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                            mapFragment.getView().setLayoutParams(params);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
//                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        synchronized (this){
            mapFragment.getMapAsync(this);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_FINE_LOCATION);
        } else {
            permissionIsGranted = true;
            //Toast.makeText(getApplicationContext(), "Location permission already granted! ", Toast.LENGTH_SHORT).show();
        }
        googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10 * 1000); //Looking to the national provider every 10 seconds.
        locationRequest.setFastestInterval(5 * 1000); // See if the location available so we're gonna set up fastest interval
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // Set priority to which provider we use.
        myLatitude = 26.9363;   // Initializing with latitute and longitude of LNMIIT, Jaipur.
        myLongitude = 75.9235;  // 26.9363° N, 75.9235° E


        searchButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selectedBusTypeIcon=null;
                mMap.clear();
                searchBusRoute(String.valueOf(searchBar.getText()));
            }
        });

        findRoute = findViewById(R.id.buttonFind_route);
        findRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, WebLoadingPage.class);
                startActivity(intent);
                finish();
                return;
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonSearch:
                break;
            case R.id.normal_bus_icon:
                selectedBusTypeIcon="NORMAL";
                searchBusRoute(String.valueOf(searchBar.getText()));
                break;
            case R.id.luxury_bus_icon:
                selectedBusTypeIcon="LUXURY";
                searchBusRoute(String.valueOf(searchBar.getText()));
                break;
            case R.id.super_luxury_bus_icon:
                selectedBusTypeIcon="SUPER LUXURY";
                searchBusRoute(String.valueOf(searchBar.getText()));
                break;
            case R.id.semiluxury_bus_icon:
                selectedBusTypeIcon="SEMI LUXURY";
                searchBusRoute(String.valueOf(searchBar.getText()));
                break;

        }
    }

    public synchronized void searchBusRoute(final String keyWord){

        if (keyWord.isEmpty()) {
            Toast.makeText(this, "Please enter route Number", Toast.LENGTH_SHORT).show();
            return;
        }

        synchronized (this){
            availableBuses.clear();

            //Fetch Bus info
            FirebaseDatabase.getInstance().getReference("Routes").addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                                Route route = postSnapshot.getValue(Route.class);
                                if(keyWord.equals(String.valueOf(route.getRouteNumber()))){
                                    availableBuses.put(postSnapshot.getKey(),route);
                                }
                            }

                            for(final Map.Entry<String,Route> entry : availableBuses.entrySet()){

                                synchronized (this){
                                    FirebaseDatabase.getInstance().getReference("Driver").addListenerForSingleValueEvent(
                                            new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    Route route=entry.getValue();
                                                    String origin = route.getOrigin();
                                                    String destination = route.getDestination();

                                                    String userId = entry.getKey();

                                                    sendRequest(origin,destination,110,route.getBusType());

                                                    Set<User> allRouteInfo = new HashSet<>();

                                                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                                                        User user = postSnapshot.getValue(User.class);

                                                        if(user.getUserId().equals(userId)){
                                                            allRouteInfo.add(user);
                                                        }
                                                    }
                                                    if(allRouteInfo.size()>0){
                                                        Iterator itr = allRouteInfo.iterator();
                                                        User lastElement = (User) itr.next();

                                                        while(itr.hasNext()) {
                                                            lastElement = (User) itr.next();
                                                        }
                                                        sendRequest(lastElement.getCoordinates(),destination,101,route.getBusType());
                                                    }
                                                }
                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
//                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                                                }
                                            });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w("getUser:onCancelled", databaseError.toException());
                        }
                    });
        }
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

    ///////////////////////////////
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng sydney = new LatLng(myLatitude, myLongitude);
        marker = mMap.addMarker(new MarkerOptions().position(sydney).title("Marker is your location."));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap.setOnMarkerClickListener(MapsActivity.this);

//        busMarker = mMap.addMarker(new MarkerOptions().position(sydney).title("Marker is your location."));

        int reqCode = 1;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, reqCode);
            return;
        }
//        mMap.setMyLocationEnabled(true);


    }

    private void sendRequest(String origin, String destination,int color,String busType) {
        switch (color){
            case 101:
                selectedColor=-16776961;
                break;
            case 110:
                selectedColor=-65536;
                break;
        }
        if (origin.isEmpty()) {
            Toast.makeText(this, "Please enter origin!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()){
            Toast.makeText(this, "Please enter destination!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            new DirectionFinder(this, origin, destination,busType).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


////////////////////////////////////////////////

    @Override
    public void onConnected(Bundle bundle) {
        requestLocationUpdates();
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_FINE_LOCATION);
            } else {
                permissionIsGranted = true;
            }
            return;
        }
        // Location Request will setup here.
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        myLatitude = location.getLatitude();
        myLongitude = location.getLongitude();

        //Visibility of search bar
        FirebaseDatabase.getInstance().getReference("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        fetchUser = dataSnapshot.getValue(User.class);



                        Toast.makeText(getApplicationContext(), getString(R.string.values_updated), Toast.LENGTH_SHORT).show();
                        marker.remove();
                        final LatLng next = new LatLng(myLatitude, myLongitude);

                        MarkerOptions mk = new MarkerOptions().position(next).title(getString(R.string.myLoc));

                        if (fetchUser.getUserType().equals("Passenger")) {
                            mk.icon(BitmapDescriptorFactory.fromResource(R.drawable.user_icon));
                        } else if (fetchUser.getUserType().equals("Driver")) {
                            mk.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon128));
                            //this method is actually performing the write operation
                            uploadCoordinates2FireBase();
                        }
                        marker = mMap.addMarker(mk);
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(next));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));

                        if(notificationBar){

                            if(circle!=null){
                                circle.remove();
                            }
                            circle = mMap.addCircle(new CircleOptions()
                                    .center(next)
                                    .radius(200)
                                    .strokeWidth(0f)
                                    .fillColor(0x550000FF));



                            Iterator<String> itr2 = availableBuses.keySet().iterator();
                            while (itr2.hasNext()) {
                                final String key = itr2.next();

                                FirebaseDatabase.getInstance().getReference("Driver").addListenerForSingleValueEvent(
                                        new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                for(DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                                                    User user = postSnapshot.getValue(User.class);

                                                    String cord=busMarker.getPosition().latitude+" , "+busMarker.getPosition().longitude;

                                                    if(user.getCoordinates().equals(cord) && user.getUserId().equals(key)){
                                                        break;

                                                    }
                                                }

                                                FirebaseDatabase.getInstance().getReference("Driver").addListenerForSingleValueEvent(
                                                        new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                                Set<User> allRouteInfo = new HashSet<>();

                                                                for(DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                                                                    User user = postSnapshot.getValue(User.class);

                                                                    allRouteInfo.add(user);
                                                                }

                                                                if(allRouteInfo.size()>0){
                                                                    Iterator itr = allRouteInfo.iterator();
                                                                    User lastElement = (User) itr.next();

                                                                    while(itr.hasNext()) {
                                                                        lastElement = (User) itr.next();
                                                                    }
                                                                    String[] cordinates = lastElement.getCoordinates().split(",");

                                                                    //Create distance alert
                                                                    float[] distance = new float[2];

                                                                    Location.distanceBetween( Double.parseDouble(cordinates[0].trim()),Double.parseDouble(cordinates[1].trim()),
                                                                            circle.getCenter().latitude, circle.getCenter().longitude, distance);

                                                                    if( distance[0] > circle.getRadius()  ){
                                                                        Toast.makeText(getBaseContext(), "Outside", Toast.LENGTH_LONG).show();
                                                                    } else {
                                                                        try {
                                                                            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                                                            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                                                                            r.play();

                                                                        } catch (Exception e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                    }

                                                                }


                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {
//                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                                                            }
                                                        });

                                                //Create distance alert
                                                float[] distance = new float[2];

                                                Location.distanceBetween( busMarker.getPosition().latitude, busMarker.getPosition().longitude,
                                                        circle.getCenter().latitude, circle.getCenter().longitude, distance);

                                                if( distance[0] > circle.getRadius()  ){
                                                    Toast.makeText(getBaseContext(), "Outside", Toast.LENGTH_LONG).show();
                                                } else {
                                                    try {
                                                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                                                        r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                                                        r.play();
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                                                        builder.setTitle("Stop Notification")
                                                                .setMessage("Bus Is Near By 10 kilometer!!!            Do you want to stop the sound ?")
                                                                .setCancelable(false)
                                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {

                                                                        r.stop();
                                                                        notificationBar=false;
                                                                    }
                                                                });
//                                                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
//                                                                    @Override
//                                                                    public void onClick(DialogInterface dialog, int which) {
//
//                                                                        Toast.makeText(MapsActivity.this,"Selected Option: No",Toast.LENGTH_SHORT).show();
//                                                                    }
//                                                                });
                                                        //Creating dialog box
                                                        AlertDialog dialog  = builder.create();
                                                        dialog.show();

                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
//                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                                            }
                                        });

                            }



                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
//                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });
    }

    /*
     * This method is saving a new set of Coordinates to the
     * Firebase Real Time Database
     * */
    private void uploadCoordinates2FireBase() {

        String coordin = myLatitude + " , " + myLongitude;
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);     // Date and Time in 24hrs
        String temp = simpleDateFormat.format(calendar.getTime());
        FirebaseUser firebaseUser = mAuth.getCurrentUser();



        if (firebaseUser != null && isSharing)      // Whenever a user is Logged In
        {
            //getting a unique id using push().getKey() method
            //it will create a unique id and we will use it as the Primary Key for our Storage
            String id = myRef.push().getKey();

            String userId=firebaseUser.getUid();

            // Creating an user object
            User user = new User(userId, coordin, temp);
            // saving the user
            myRef.child(id).setValue(user);
            // displaying a success toast
            Toast.makeText(this, getString(R.string.successfullyUploaded) + myLatitude + "° N, " + myLongitude + "° E", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (permissionIsGranted) {
            if (googleApiClient.isConnected()) {
                requestLocationUpdates();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (permissionIsGranted) // Suspend services on pause
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (permissionIsGranted) googleApiClient.disconnect();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                    permissionIsGranted = true;
                } else {
                    //permission denied
                    permissionIsGranted = false;
                    Toast.makeText(getApplicationContext(), "This app requires location permission to be granted", Toast.LENGTH_SHORT).show();
                    showSettingsAlert();
                }
                break;
            case MY_PERMISSION_REQUEST_COARSE_LOCATION:
                // do something for coarse location
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menuObj) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.goto_profile, menuObj);
        //Visibility of search bar
        FirebaseDatabase.getInstance().getReference("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        fetchUser = dataSnapshot.getValue(User.class);

                        if (fetchUser.getUserType().equals("Passenger")) {
                            menuObj.findItem(R.id.menuAccess).setVisible(false);
                            menuObj.findItem(R.id.booking).setVisible(false);
                        } else if (fetchUser.getUserType().equals("Driver")) {
                            menuObj.findItem(R.id.myBooking).setVisible(false);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
//                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menuLogout:
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.menuProfile:
                startActivity(new Intent(MapsActivity.this, ProfileActivity.class));
                break;
            case R.id.tripInfo:
                startActivity(new Intent(MapsActivity.this, ScheduleActivity.class));
                break;
            case R.id.booking:
                startActivity(new Intent(MapsActivity.this, BookActivity.class));
                break;
            case R.id.myBooking:
                startActivity(new Intent(MapsActivity.this, BookActivityUser.class));
                break;
            case R.id.menuAccess:
                if (item.getTitle().equals(getString(R.string.share))) {
                    isSharing = true;
                    item.setTitle(getString(R.string.dont_share));
                } else {
                    isSharing = false;
                    item.setTitle(getString(R.string.share));
                }
        }
        return true;
    }

    private void showSettingsAlert() {
        AlertDialog.Builder al = new AlertDialog.Builder(MapsActivity.this);
        al.setMessage(getString(R.string.permission_message)).setCancelable(false).setPositiveButton(getString(R.string.settingbtn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Opening Location Settings
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

                Intent switchTo = new Intent(MapsActivity.this, ProfileActivity.class);
                startActivity(switchTo);
            }
        });
        AlertDialog alertDialog = al.create();
        alertDialog.setTitle(getString(R.string.permission_title));
        alertDialog.show();

    }

    @Override
    public void onDirectionFinderStart() {
//        progressDialog = ProgressDialog.show(this, "Please wait", "Finding direction", true);
        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }
        if (destinationMarker != null) {
            for (Marker marker : destinationMarker) {
                marker.remove();
            }
        }
        if (polyLinePaths != null) {
            for (Polyline polylinePath : polyLinePaths) {
                polylinePath.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Routes> route) {
//        progressDialog.dismiss();
        polyLinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarker = new ArrayList<>();

        for (Routes routes : route) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(routes.startLocation, 16));
            ((TextView)findViewById(R.id.textViewDistance)).setText(routes.distance.text);
            ((TextView)findViewById(R.id.textViewTime)).setText(routes.duration.text);

            int res = 0;

            for (int i=0; i<routes.startAddress.toLowerCase().length(); i++) {
                // checking character in string
                if (routes.startAddress.toLowerCase().charAt(i) == ',')
                    res++;
            }

            if(res>1){
                if(routes.startAddress.toLowerCase().indexOf("stand")!=-1){ //Found
                    originMarkers.add(mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                            .title(routes.startAddress)
                            .position(routes.startLocation)));
                }else if (routes.startAddress.toLowerCase().indexOf("station")!=-1){
                    originMarkers.add(mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                            .title(routes.startAddress)
                            .position(routes.startLocation)));
                }
//                else if (routes.startAddress.toLowerCase().indexOf("stop")!=-1){
//                    originMarkers.add(mMap.addMarker(new MarkerOptions()
//                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
//                            .title(routes.startAddress)
//                            .position(routes.startLocation)));
//                }
                else{
                    //Set Searched bus icons

                    if(selectedBusTypeIcon!=null){
                        if(routes.busType.equals(selectedBusTypeIcon)){
                            routes.busType=selectedBusTypeIcon;
                        }else{
                            routes.busType="";
                        }

                    }

                    switch (routes.busType){
                        case "NORMAL":
                            originMarkers.add(mMap.addMarker(new MarkerOptions()
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.normal_bus_icon))
                                    .title(routes.startAddress)
                                    .position(routes.startLocation)));
                            break;
                        case "SEMI LUXURY":
                            originMarkers.add(mMap.addMarker(new MarkerOptions()
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.semi_luxury_bus_icon))
                                    .title(routes.startAddress)
                                    .position(routes.startLocation)));
                            break;
                        case "LUXURY":
                            originMarkers.add(mMap.addMarker(new MarkerOptions()
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.luxury_bus_icon))
                                    .title(routes.startAddress)
                                    .position(routes.startLocation)));
                            break;
                        case "SUPER LUXURY":
                            originMarkers.add(mMap.addMarker(new MarkerOptions()
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.super_luxury_bus_icon))
                                    .title(routes.startAddress)
                                    .position(routes.startLocation)));
                            break;
//                        default:
//                            originMarkers.add(mMap.addMarker(new MarkerOptions()
//                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon128))
//                                    .title(routes.startAddress)
//                                    .position(routes.startLocation)));
//                           break;
                    }

                }
            }

            if(selectedBusTypeIcon!=null){
                if(routes.busType.equals(selectedBusTypeIcon)){
                    destinationMarker.add(mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
                            .title(routes.endAddress)
                            .position(routes.endLocation)));

                    PolylineOptions polylineOptions = new PolylineOptions()
                            .geodesic(true)
                            .color(selectedColor)
                            .width(10);

                    for (int i = 0; i < routes.points.size(); i++) {
                        polylineOptions.add(routes.points.get(i));
                    }

                    polyLinePaths.add(mMap.addPolyline(polylineOptions));
                }else{

                }

            }else{
                destinationMarker.add(mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
                        .title(routes.endAddress)
                        .position(routes.endLocation)));

                PolylineOptions polylineOptions = new PolylineOptions()
                        .geodesic(true)
                        .color(selectedColor)
                        .width(10);

                for (int i = 0; i < routes.points.size(); i++) {
                    polylineOptions.add(routes.points.get(i));
                }

                polyLinePaths.add(mMap.addPolyline(polylineOptions));
            }


        }
    }


    @Override
    public boolean onMarkerClick(final Marker marker) {

        for(Marker mk:originMarkers){
            if(marker.equals(mk)){
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setTitle("Distance Alert")
                        .setMessage("You want to create notification with in 10KM ?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                notificationBar=true;
                                busMarker=marker;

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                notificationBar=false;
                                Toast.makeText(MapsActivity.this,"Selected Option: No",Toast.LENGTH_SHORT).show();
                            }
                        });
                //Creating dialog box
                AlertDialog dialog  = builder.create();
                dialog.show();
                return true;
            }else if(marker.equals(marker)){
                Toast.makeText(getApplicationContext(), "Passenger Marker Selected", Toast.LENGTH_SHORT).show();
                return true;
            }
        }

        return false;
    }
}