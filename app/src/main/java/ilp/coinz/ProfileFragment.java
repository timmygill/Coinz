package ilp.coinz;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){

        TextView loggedInAs = (TextView) getView().findViewById(R.id.profileLIA);
        String prompt = getString(R.string.profile_logged_in_as) + " " + FirebaseAuth.getInstance().getCurrentUser().getEmail();
        loggedInAs.setText(prompt);

        TextView coinsLabel = (TextView) getView().findViewById(R.id.profileCoins);
        coinsLabel.setText(getString(R.string.profile_coins) + " " + activity.getLifetimeCoins() + " Coins");

        TextView goldLabel = (TextView) getView().findViewById(R.id.profileGold);
        goldLabel.setText(getString(R.string.profile_gold) + " " + String.format("%.3f", activity.getLifetimeGold()) + " Gold");

        TextView distanceLabel = (TextView) getView().findViewById(R.id.profileDistance);
        distanceLabel.setText(getString(R.string.profile_distance) + " " + activity.getLifetimeDistance() + "m");



        Button button = (Button) getView().findViewById(R.id.profileLOButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                if(FirebaseAuth.getInstance().getCurrentUser() == null){
                    startActivity(new Intent(getContext(), LoginActivity.class));
                }

            }
        });
    }

}
