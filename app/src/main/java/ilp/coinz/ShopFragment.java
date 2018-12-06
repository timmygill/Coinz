package ilp.coinz;

import android.os.Bundle;
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
        nextMultiCost = activity.playerMulti * activity.playerMulti * 10000;
        nextRadiusCost = (activity.playerRadius - 24) * (activity.playerRadius - 24) * 10000;
    }

    public void updateText(){
        balance = (TextView) getView().findViewById(R.id.shopBalance);
        String balanceText = getString(R.string.shop_balance) + " " + activity.goldBalance;
        balance.setText(balanceText);

        multi = (TextView) getView().findViewById(R.id.shopMulti);
        String multiText = getString(R.string.shop_multi) + " " + activity.playerMulti + " / "  + maxMulti;
        multi.setText(multiText);

        radius = (TextView) getView().findViewById(R.id.shopRadius);
        String radiusText = getString(R.string.shop_radius) + " " + activity.playerRadius + " / " + maxRadius;
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
                if(activity.goldBalance > nextMultiCost && activity.playerMulti < maxMulti){
                    activity.goldBalance -= nextMultiCost;
                    activity.playerMulti += 1;

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    String email = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                    db.collection("user").document(email).collection("Bank").document(email).set(new GoldBalance(activity.goldBalance));
                    db.collection("user").document(email).collection("Player").document(email).set(new Player(activity.playerMulti, activity.playerRadius));

                    calculatePrices();
                    updateText();

                }

            }
        });
        Button radiusButton = (Button) getView().findViewById(R.id.shopButtonRadius);
        radiusButton.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v ) {
                if(activity.goldBalance > nextRadiusCost && activity.playerRadius < maxRadius){
                    activity.goldBalance -= nextRadiusCost;
                    activity.playerRadius += 1;

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    String email = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                    db.collection("user").document(email).collection("Bank").document(email).set(new GoldBalance(activity.goldBalance));
                    db.collection("user").document(email).collection("Player").document(email).set(new Player(activity.playerMulti, activity.playerRadius));

                    calculatePrices();
                    updateText();

                }

            }
        });





    }


}
