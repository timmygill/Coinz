package ilp.coinz;


import android.arch.lifecycle.Lifecycle;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.mapboxsdk.utils.MapFragmentUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MapFragment extends Fragment implements OnMapReadyCallback, LocationEngineListener {

    final private String tag = "MapFragment";

    public MainActivity activity;


    private MapView mapView;
    private MapboxMap map;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<OnMapReadyCallback> mapReadyCallbackList = new ArrayList<>();
    private com.mapbox.mapboxsdk.maps.MapFragment.OnMapViewReadyCallback mapViewReadyCallback;

    public LocationEngine locationEngine;

    private Location oldLocation;

    private double collectionRadius = 25;



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

        activity = (MainActivity) getActivity();



        Mapbox.getInstance(Objects.requireNonNull(getActivity()), getString(R.string.access_token));

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Context context = inflater.getContext();
        mapView = new MapView(context, MapFragmentUtils.resolveArgs(context, getArguments()));
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // notify listeners about mapview creation
        if (mapViewReadyCallback != null) {
            mapViewReadyCallback.onMapViewReady(mapView);
        }
        return mapView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        if (mapboxMap == null) {
            Log.d(tag, "[onMapReady] mapboxMap is null");
        } else {
            Log.d(tag, "[onMapReady] mapboxMap is ready");
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

        activity.downloadTodaysMap();
        enableLocation();
    }



    public void enableLocation() {
        if(activity.isLocationGranted()) {
            initializeLocationEngine();
            initializeLocationLayer();
        } else {
            activity.requestLocation();
        }
    }



    @SuppressWarnings("MissingPermission")
    private void initializeLocationEngine(){
        locationEngine = new LocationEngineProvider(getActivity()).obtainBestLocationEngineAvailable();
        locationEngine.setInterval(5000);
        locationEngine.setFastestInterval(1000);
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        oldLocation = locationEngine.getLastLocation();
        if (oldLocation != null){
            setCameraPosition(oldLocation);
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
                LocationLayerPlugin locationLayerPlugin = new LocationLayerPlugin(mapView, map, locationEngine);
                locationLayerPlugin.setLocationLayerEnabled(true);
                locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
                locationLayerPlugin.setRenderMode(RenderMode.NORMAL);
                Lifecycle lifecycle = getLifecycle();
                lifecycle.addObserver(locationLayerPlugin);
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
        } else if(activity.isCoinsReady()){
            Log.d(tag, "[onLocationChanged] location is not null, radius: " + collectionRadius);

            collectionRadius = activity.getPlayer().getRadius();
            setCameraPosition(location);

            if (oldLocation != null){
                float[] distanceMoved  = new float[2];
                Location.distanceBetween(location.getLatitude(),location.getLongitude(),oldLocation.getLatitude(),oldLocation.getLongitude(),distanceMoved);
                activity.getPlayer().setLifetimeDistance(activity.getPlayer().getLifetimeDistance() + distanceMoved[0]);

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String email = Objects.requireNonNull(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail());
                db.collection("user").document(email).collection("Player").document(email).update("lifetimeDistance", activity.getPlayer().getLifetimeDistance());
            }

            oldLocation = location;

            for (Coin c : activity.getCoinsCollection().values()){
                float[] distance = new float[2];
                Location.distanceBetween(location.getLatitude(),location.getLongitude(),c.getLatitude(),c.getLongitude(),distance);

                if (distance[0] <= collectionRadius && !(c.isCollected()) && activity.isCoinsReady())
                {
                    c.setCollected(true);
                    activity.getCollectedIDs().add(c.getId());
                    Log.d(tag, "Collected coin: " + c.getId());

                    if (activity.getMarkers().containsKey(c.getId())) { map.removeMarker(Objects.requireNonNull(activity.getMarkers().get(c.getId()))); }

                    activity.getPlayer().setLifetimeCoins(activity.getPlayer().getLifetimeCoins() + 1);

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    String email = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                    db.collection("user").document(email).collection("Wallet").document(c.getId()).set(c);
                    db.collection("user").document(email).collection("Player").document(email).update("lifetimeCoins", activity.getPlayer().getLifetimeCoins());

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


    public void addCoinsToMap() {
        IconFactory iconFactory = IconFactory.getInstance(Objects.requireNonNull(getContext()));
        Icon icon = iconFactory.fromResource(R.drawable.coinmarker);
        for (Coin coin : activity.getCoinsCollection().values()) {
            if (!(activity.getCollectedIDs().contains(coin.getId()))){
                LatLng pos = new LatLng(coin.getLatitude(), coin.getLongitude());
                String snip = "VALUE: " + String.format("%.3f", coin.getValue());
                String tit = coin.getCurrency();
                MarkerOptions mo = new MarkerOptions().position(pos).title(tit).snippet(snip).icon(icon);
                try {
                    activity.getMarkers().put(coin.getId(), map.addMarker(mo));
                } catch (NullPointerException e) {
                    Log.d(tag, e.toString());
                }
            }
        }
        Log.d(tag, "Coins ready");
        activity.setCoinsReady(true);
    }

    public void displayJsonError(){
        Snackbar.make(Objects.requireNonNull(getView()),R.string.map_json_error, Snackbar.LENGTH_LONG).show();
        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            Log.d(tag, e.getMessage());
        }
        activity.finish();
    }

    public void displayPermissionError(){
        Snackbar.make(Objects.requireNonNull(getView()), R.string.map_perm_explain, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();

        if(locationEngine != null){
            try {
                locationEngine.requestLocationUpdates();
            } catch(SecurityException ignored) {}
            locationEngine.addLocationEngineListener(this);
        }

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

        if(locationEngine != null) {
            locationEngine.removeLocationEngineListener(this);
            locationEngine.removeLocationUpdates();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}

