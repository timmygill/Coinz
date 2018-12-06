package ilp.coinz;


import android.arch.lifecycle.Lifecycle;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.mapboxsdk.style.light.Position;
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

import static java.lang.Math.cos;
import static java.lang.Math.sin;


public class MapFragment extends Fragment implements PermissionsListener, OnMapReadyCallback, LocationEngineListener, DownloadFileResponse {

    final private String tag = "MapFragment";

    public MainActivity activity;

    private MapView mapView;
    private MapboxMap map;
    private final List<OnMapReadyCallback> mapReadyCallbackList = new ArrayList<>();
    private com.mapbox.mapboxsdk.maps.MapFragment.OnMapViewReadyCallback mapViewReadyCallback;

    private PermissionsManager permissionsManager;
    public LocationEngine locationEngine;


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

        activity.todaysDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Calendar.getInstance().getTime());

        Mapbox.getInstance(getActivity(), getString(R.string.access_token));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        activity.downloadTodaysMap();
        enableLocationComponent();
    }

    @Override
    public void downloadFinish(String result){

        activity.jsonResult = result;
        activity.downloadFBWallet();

    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent() {
        if (PermissionsManager.areLocationPermissionsGranted(getContext())) {
            Log.d(tag, "Permissions are granted");

            LocationComponentOptions options = LocationComponentOptions.builder(getContext())
                    .trackingGesturesManagement(true)
                    .accuracyColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark))
                    .build();

            // Get an instance of the component
            LocationComponent locationComponent = map.getLocationComponent();

            // Activate with options
            locationComponent.activateLocationComponent(getContext(), options);

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.NORMAL);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        //TODO: tell user
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationComponent();
        } else {
            //TODO: close app
           activity.finish();
        }
    }


    @Override
    public void onLocationChanged(Location location){
        if(location == null){
            Log.d(tag, "[onLocationChanged] location is null");
        } else {
            Log.d(tag, "[onLocationChanged] location is not null, radius: " + collectionRadius);
            collectionRadius = activity.playerRadius;

            for (Coin c : activity.coinsCollection.values()){
                float[] distance = new float[2];
                Location.distanceBetween(location.getLatitude(),location.getLongitude(),c.getLatitude(),c.getLongitude(),distance);

                if (distance[0] <= collectionRadius && !(c.isCollected()) && activity.coinsReady)
                {
                    c.setCollected(true);
                    activity.collectedIDs.add(c.getId());
                    Log.d(tag, "Collected coin" + c.getId());

                    if (activity.markers.containsKey(c.getId())) { map.removeMarker(activity.markers.get(c.getId())); }

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    String email = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                    db.collection("user").document(email).collection("Wallet").document(c.getId()).set(c);

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
        for (Coin coin : activity.coinsCollection.values()) {
            if (!(activity.collectedIDs.contains(coin.getId()))){
                LatLng pos = new LatLng(coin.getLatitude(), coin.getLongitude());
                String snip = "VALUE: " + coin.getValue().toString();
                String tit = coin.getCurrency();
                MarkerOptions mo = new MarkerOptions().position(pos).title(tit).snippet(snip);
                try {
                    activity.markers.put(coin.getId(), map.addMarker(mo));
                } catch (NullPointerException e) {
                    Log.d(tag, e.toString());
                }
            }
        }
        activity.coinsReady = true;
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }
}

