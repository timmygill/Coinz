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
        nextMultiCost = activity.getPlayer().getMulti() * activity.getPlayer().getMulti() * 10000;
        nextRadiusCost = (activity.getPlayer().getRadius() - 24) * (activity.getPlayer().getRadius() - 24) * 10000;
    }

    public void updateText(){
        TextView balance = getView().findViewById(R.id.shopBalance);
        String balanceText = getString(R.string.shop_balance) + " " + String.format("%.3f", activity.getPlayer().getGoldBalance());
        balance.setText(balanceText);

        TextView multi = getView().findViewById(R.id.shopMulti);
        String multiText = getString(R.string.shop_multi) + " " + activity.getPlayer().getMulti() + " / "  + maxMulti;
        multi.setText(multiText);

        TextView radius = getView().findViewById(R.id.shopRadius);
        String radiusText = getString(R.string.shop_radius) + " " + activity.getPlayer().getRadius() + " / " + maxRadius;
        radius.setText(radiusText);

        TextView multiNext = getView().findViewById(R.id.shopNextMulti);
        String multiNextText = nextMultiCost + " Gold";
        multiNext.setText(multiNextText);

        TextView radiusNext = getView().findViewById(R.id.shopNextRadius);
        String radiusNextText = nextRadiusCost + " Gold";
        radiusNext.setText(radiusNextText);
    }

    public void setButtons(){
        Button multiButton = getView().findViewById(R.id.shopButtonMulti);
        multiButton.setOnClickListener(v -> {
            if(activity.getPlayer().getMulti() < maxMulti) {
                if (activity.getPlayer().getGoldBalance() > nextMultiCost) {
                    activity.getPlayer().setGoldBalance(activity.getPlayer().getGoldBalance() - nextMultiCost);
                    activity.getPlayer().setMulti(activity.getPlayer().getMulti() + 1);

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    String email = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                    db.collection("user").document(email).collection("Player").document(email).set(activity.getPlayer());

                    calculatePrices();
                    updateText();

                } else {
                    Snackbar.make(getView(),R.string.shop_more_gold, Snackbar.LENGTH_LONG).show();
                }
            } else {
                Snackbar.make(getView(),R.string.shop_maxed, Snackbar.LENGTH_LONG).show();
            }
        });
        Button radiusButton = getView().findViewById(R.id.shopButtonRadius);
        radiusButton.setOnClickListener(v -> {
            if(activity.getPlayer().getRadius() < maxRadius) {
                if (activity.getPlayer().getGoldBalance() > nextRadiusCost) {
                    activity.getPlayer().setGoldBalance(activity.getPlayer().getGoldBalance() - nextRadiusCost);
                    activity.getPlayer().setRadius(activity.getPlayer().getRadius() + 1);

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    String email = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                    db.collection("user").document(email).collection("Player").document(email).set(activity.getPlayer());

                    calculatePrices();
                    updateText();

                } else {
                    Snackbar.make(getView(),R.string.shop_more_gold, Snackbar.LENGTH_LONG).show();
                }
            } else {
                Snackbar.make(getView(),R.string.shop_maxed, Snackbar.LENGTH_LONG).show();
            }
        });





    }


}
