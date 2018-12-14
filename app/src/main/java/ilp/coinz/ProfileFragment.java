package ilp.coinz;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import javax.annotation.Nullable;

public class ProfileFragment extends Fragment {

    public MainActivity activity;

    public ProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){

        //generates messages with statistics from current player instance

        TextView loggedInAs = Objects.requireNonNull(getView()).findViewById(R.id.profileLIA);
        String prompt = getString(R.string.profile_logged_in_as) + " " + activity.getPlayer().getEmail();
        loggedInAs.setText(prompt);

        TextView coinsLabel = getView().findViewById(R.id.profileCoins);
        coinsLabel.setText(getString(R.string.profile_coins) + " " + activity.getPlayer().getLifetimeCoins() + " Coins");

        TextView goldLabel = getView().findViewById(R.id.profileGold);
        goldLabel.setText(getString(R.string.profile_gold) + " " + String.format("%.3f", activity.getPlayer().getLifetimeGold()) + " Gold");

        TextView distanceLabel = getView().findViewById(R.id.profileDistance);
        distanceLabel.setText(getString(R.string.profile_distance) + " " + activity.getPlayer().getLifetimeDistance() + "m");



        Button button = getView().findViewById(R.id.profileLOButton);
        button.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            if(FirebaseAuth.getInstance().getCurrentUser() == null){
                startActivity(new Intent(getContext(), LoginActivity.class));
                //ignore movement when changing user
                activity.setCoinsReady(false);
            }

        });
    }

}
