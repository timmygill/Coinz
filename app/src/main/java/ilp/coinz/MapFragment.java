package ilp.coinz;


import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.mapboxsdk.utils.MapFragmentUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class MapFragment extends Fragment implements PermissionsListener, OnMapReadyCallback, LocationEngineListener, DownloadFileResponse {

    final private String tag = "MapFragment";

    private MapView mapView;
    private MapboxMap map;
    private final List<OnMapReadyCallback> mapReadyCallbackList = new ArrayList<>();
    private com.mapbox.mapboxsdk.maps.MapFragment.OnMapViewReadyCallback mapViewReadyCallback;

    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;

    private String todaysDate = ""; //YYYY/MM/DD
    private String downloadDate = ""; //YYYY/MM/DD
    private final String preferencesFile = "MyPrefsFile";

    private HashMap<String, Coin> coinsCollection = new HashMap<>();
    private HashMap<String, Marker> markers = new HashMap<>();
    private ArrayList<String> collectedIDs = new ArrayList<>();
    private ExchangeRates exchangeRates;
    private String jsonResult;

    private boolean coinsReady = false;

    private float collectionRadius = 25;



    public MapFragment() {
    }



    public static MapFragment newInstance(MapboxMapOptions options) {
        MapFragment mapFragment = new MapFragment();
        mapFragment.setArguments(MapFragmentUtils.createFragmentArgs(options));
        return mapFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof com.mapbox.mapboxsdk.maps.MapFragment.OnMapViewReadyCallback) {
            mapViewReadyCallback = (com.mapbox.mapboxsdk.maps.MapFragment.OnMapViewReadyCallback) context;
        }
    }

    @Override
    public void onInflate(@NonNull Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        setArguments(MapFragmentUtils.createFragmentArgs(MapboxMapOptions.createFromAttributes(context, attrs)));
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        todaysDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Calendar.getInstance().getTime());

        Mapbox.getInstance(getActivity(), getString(R.string.access_token));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Context context = inflater.getContext();
        mapView = new MapView(context, MapFragmentUtils.resolveArgs(context, getArguments()));
        return mapView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // notify listeners about mapview creation
        if (mapViewReadyCallback != null) {
            mapViewReadyCallback.onMapViewReady(mapView);
        }
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        if (mapboxMap == null) {
            Log.d(tag, "[onMapReady] mapboxMap is null");
        } else {
            this.map = mapboxMap;
            for (OnMapReadyCallback onMapReadyCallback : mapReadyCallbackList) {
                onMapReadyCallback.onMapReady(mapboxMap);
            }
        }

        //Set user interface options
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setZoomGesturesEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setRotateGesturesEnabled(false);
        map.getUiSettings().setTiltGesturesEnabled(false);

        downloadTodaysMap();
        enableLocation();
    }


    private void enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(getContext())) {
            Log.d(tag, "Permissions are granted");
            initializeLocationEngine();
            initializeLocationLayer();
        } else {
            Log.d(tag, "Permissions are not granted");
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }


    @SuppressWarnings("MissingPermission")
    private void initializeLocationEngine(){
        locationEngine = new LocationEngineProvider(getActivity()).obtainBestLocationEngineAvailable();
        locationEngine.setInterval(5000);
        locationEngine.setFastestInterval(1000);
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null){
            setCameraPosition(lastLocation);
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void initializeLocationLayer(){
        if(mapView == null){
            Log.d(tag, "mapView is null") ;
        } else {
            if (map == null){
                Log.d(tag, "map is null");
            }else{
                locationLayerPlugin = new LocationLayerPlugin(mapView, map, locationEngine);
                locationLayerPlugin.setLocationLayerEnabled(true);
                locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
                locationLayerPlugin.setRenderMode(RenderMode.NORMAL);
            }
        }
    }

    private void setCameraPosition(Location location){
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void onLocationChanged(Location location){
        if(location == null){
            Log.d(tag, "[onLocationChanged] location is null");
        } else {
            Log.d(tag, "[onLocationChanged] location is not null");
            setCameraPosition(location);

            for (Coin c : coinsCollection.values()){
                float[] distance = new float[2];
                Location.distanceBetween(location.getLatitude(),location.getLongitude(),c.getLatitude(),c.getLongitude(),distance);

                if (distance[0] <= collectionRadius && !(c.isCollected()) && coinsReady)
                {
                    c.setCollected(true);
                    Log.d(tag, "Collected coin" + c.getId());

                    if (markers.containsKey(c.getId())) { map.removeMarker(markers.get(c.getId())); }

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    String email = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                    db.collection("user").document(email).collection("Wallet").add(c);

                }
            }
        }
    }


    @Override
    @SuppressWarnings("MissingPermission")
    public void onConnected(){
        Log.d(tag, "[onConnected] requesting location updates");
        locationEngine.requestLocationUpdates();
        Log.d(tag, "[onConnected] location updates granted");
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain){
        Log.d(tag, "Permissions: " + permissionsToExplain.toString());
    }

    @Override
    public void onPermissionResult(boolean granted){
        Log.d(tag, "[onPermissionResult] granted == " + granted);
        if(granted){
            enableLocation();
        } else {
            //TODO: dialogue with user
        }
    }

    //Download todays coin map and update preferences
    private void downloadTodaysMap(){

        //Restore preferences
        SharedPreferences settings = this.getActivity().getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        downloadDate = settings.getString("lastDownloadDate", "");
        Log.d(tag, "[onStart] Recalled lastDownloadDate is '" + downloadDate + "'");

        //On new map, delete old coins from Firebase and update most recent map date.
        if (!(todaysDate.equals(downloadDate))) {
            clearFBWallet();

            downloadDate = todaysDate;

            SharedPreferences.Editor editor = settings.edit();
            editor.putString("lastDownloadDate", downloadDate);
            editor.commit();
        } else {
            downloadMap(todaysDate);
        }
    }

    public void downloadMap(String date){
        String downloadString = "https://homepages.inf.ed.ac.uk/stg/coinz/" + date + "/coinzmap.geojson";
        DownloadFileTask downloadMapTask = new DownloadFileTask();
        downloadMapTask.delegate = this;
        downloadMapTask.execute(downloadString);
    }

    @Override
    public void downloadFinish(String result){

        jsonResult = result;
        downloadFBWallet();


    }
    public void parseJson(){
        try {
            JSONObject coindatajson = new JSONObject(jsonResult);
            exchangeRates = new ExchangeRates(coindatajson.getJSONObject("rates"));

            JSONArray coinsjson = coindatajson.getJSONArray("features");
            for (int i = 0; i < coinsjson.length(); i++) {
                Coin tempCoin = new Coin(coinsjson.getJSONObject(i));
                coinsCollection.put(tempCoin.getId(), tempCoin);
            }
            for (String id : collectedIDs){
                if(coinsCollection.containsKey(id)){ coinsCollection.get(id).setCollected(true); }
            }

        } catch (JSONException e){
            Log.d(tag, e.toString());
        }
        addCoinsToMap();
    }

    public void addCoinsToMap() {
        for (Coin coin : coinsCollection.values()) {
            if (!(collectedIDs.contains(coin.getId()))){
                LatLng pos = new LatLng(coin.getLatitude(), coin.getLongitude());
                String snip = "VALUE: " + coin.getValue().toString();
                String tit = coin.getCurrency().toString();
                MarkerOptions mo = new MarkerOptions().position(pos).title(tit).snippet(snip);
                try {
                    markers.put(coin.getId(), map.addMarker(mo));
                } catch (NullPointerException e) {
                    Log.d(tag, e.toString());
                }
            }
        }
        coinsReady = true;
    }

    public void clearFBWallet(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String email = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        db.collection("/user/" + email + "/Wallet")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot doc : task.getResult()){
                                doc.getReference().delete();
                            }
                            downloadMap(todaysDate);
                        } else {
                            Log.d(tag, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    public void downloadFBWallet(){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String email = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        db.collection("/user/" + email + "/Wallet")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                collectedIDs.add(doc.get("id").toString());
                            }
                            parseJson();
                        } else {
                            Log.d(tag, "Error getting documents: ", task.getException());
                        }

                    }
                });

    }



    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }
}

