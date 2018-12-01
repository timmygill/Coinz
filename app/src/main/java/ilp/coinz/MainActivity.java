package ilp.coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;



import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

private FirebaseAuth mAuth;
final String tag = "MainActivity";

private MapFragment mapFragment;
private Fragment bankFragment;
private Fragment shopFragment;
private Fragment profileFragment;

private FragmentManager fragmentManager = getSupportFragmentManager();

private String currentFragment;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Mapbox.getInstance(this, getString(R.string.access_token));

        bankFragment = new BankFragment();
        shopFragment = new ShopFragment();
        profileFragment = new ProfileFragment();


            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            //create fragments but do not show

        transaction.add(R.id.fragment_container, bankFragment, "bankFragment");
        transaction.add(R.id.fragment_container, shopFragment, "shopFragment");
        transaction.add(R.id.fragment_container, profileFragment, "profileFragment");
        transaction.detach(bankFragment);
        transaction.detach(shopFragment);
        transaction.detach(profileFragment);




            //Make map fragment, shown by default
            MapboxMapOptions options = new MapboxMapOptions()
                    .styleUrl(Style.MAPBOX_STREETS)
                    .camera(new CameraPosition.Builder()
                            .target(new LatLng(55.944, -3.188396))
                            .zoom(15)
                            .build());
            mapFragment = MapFragment.newInstance(options);

            transaction.add(R.id.fragment_container, mapFragment, "mapFragment");
            transaction.commit();
            currentFragment = "mapFragment";


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);




        mAuth = FirebaseAuth.getInstance();
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_map:
                    loadFragment("mapFragment");
                    Log.d(tag, "map");
                    return true;
                case R.id.navigation_bank:
                    loadFragment("bankFragment");
                    Log.d(tag, "bank");
                    return true;
                case R.id.navigation_shop:
                    loadFragment("shopFragment");
                    Log.d(tag, "shop");
                    return true;
                case R.id.navigation_profile:
                    loadFragment("profileFragment");
                    Log.d(tag, "profile");
                    return true;
            }
            return false;
        }
    };


    private void loadFragment(String tag) {
        // load fragment
        if(fragmentManager.findFragmentByTag(tag) != null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            Fragment oldFrag = fragmentManager.findFragmentByTag(currentFragment);
            Fragment newFrag = fragmentManager.findFragmentByTag(tag);
            transaction.detach(oldFrag);
            transaction.attach(newFrag);
            currentFragment = tag;
            transaction.commit();
        }
    }


    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onStop(){
        super.onStop();
    }
}
