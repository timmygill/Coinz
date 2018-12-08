package ilp.coinz;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import javax.annotation.Nullable;

public class ShopFragment extends Fragment {

    public MainActivity activity;

    private TextView balance;
    private TextView multi;
    private TextView radius;
    private TextView multiNext;
    private TextView radiusNext;

    private int nextMultiCost;
    private int nextRadiusCost;

    private int maxMulti = 100;
    private int maxRadius = 100;



    public ShopFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shop, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        calculatePrices();
        updateText();
        setButtons();
    }

    public void calculatePrices(){
        nextMultiCost = activity.getPlayerMulti() * activity.getPlayerMulti() * 10000;
        nextRadiusCost = (activity.getPlayerRadius() - 24) * (activity.getPlayerRadius() - 24) * 10000;
    }

    public void updateText(){
        balance = (TextView) getView().findViewById(R.id.shopBalance);
        String balanceText = getString(R.string.shop_balance) + " " + String.format("%.3f", activity.getGoldBalance());
        balance.setText(balanceText);

        multi = (TextView) getView().findViewById(R.id.shopMulti);
        String multiText = getString(R.string.shop_multi) + " " + activity.getPlayerMulti() + " / "  + maxMulti;
        multi.setText(multiText);

        radius = (TextView) getView().findViewById(R.id.shopRadius);
        String radiusText = getString(R.string.shop_radius) + " " + activity.getPlayerRadius() + " / " + maxRadius;
        radius.setText(radiusText);

        multiNext = (TextView) getView().findViewById(R.id.shopNextMulti);
        String multiNextText = nextMultiCost + " Gold";
        multiNext.setText(multiNextText);

        radiusNext = (TextView) getView().findViewById(R.id.shopNextRadius);
        String radiusNextText = nextRadiusCost + " Gold";
        radiusNext.setText(radiusNextText);
    }

    public void setButtons(){
        Button multiButton = (Button) getView().findViewById(R.id.shopButtonMulti);
        multiButton.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v ) {
                if(activity.getPlayerMulti() < maxMulti) {
                    if (activity.getGoldBalance() > nextMultiCost) {
                        activity.setGoldBalance(activity.getGoldBalance() - nextMultiCost);
                        activity.setPlayerMulti(activity.getPlayerMulti() + 1);

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        String email = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                        db.collection("user").document(email).collection("Player").document(email).set(new Player(activity.getGoldBalance(), activity.getPlayerMulti(), activity.getPlayerRadius(), activity.getLifetimeCoins(), activity.getLifetimeGold(), activity.getLifetimeDistance()));

                        calculatePrices();
                        updateText();

                    } else {
                        Snackbar.make(getView(),R.string.shop_more_gold, Snackbar.LENGTH_LONG);
                    }
                } else {
                    Snackbar.make(getView(),R.string.shop_maxed, Snackbar.LENGTH_LONG);
                }
            }
        });
        Button radiusButton = (Button) getView().findViewById(R.id.shopButtonRadius);
        radiusButton.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v ) {
                if(activity.getPlayerRadius() < maxRadius) {
                    if (activity.getGoldBalance() > nextRadiusCost) {
                        activity.setGoldBalance(activity.getGoldBalance() - nextRadiusCost);
                        activity.setPlayerRadius(activity.getPlayerRadius() + 1);

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        String email = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                        db.collection("user").document(email).collection("Player").document(email).set(new Player(activity.getGoldBalance(), activity.getPlayerMulti(), activity.getPlayerRadius(), activity.getLifetimeCoins(), activity.getLifetimeGold(), activity.getLifetimeDistance()));

                        calculatePrices();
                        updateText();

                    } else {
                        Snackbar.make(getView(),R.string.shop_more_gold, Snackbar.LENGTH_LONG);
                    }
                } else {
                    Snackbar.make(getView(),R.string.shop_maxed, Snackbar.LENGTH_LONG);
                }
            }
        });





    }


}
