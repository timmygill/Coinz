package ilp.coinz;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

import javax.annotation.Nullable;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

@SuppressWarnings("ConstantConditions")
public class BankFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private final String tag = "BankFragment";

    public MainActivity activity;


    private int selectedIndex;
    private ArrayList<Coin> spinnerItems;
    private Spinner spinner;

    public BankFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (MainActivity) getActivity();

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bank, container, false);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){

        updateText();

        spinnerItems = new ArrayList<>();

        spinner = getView().findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        Button ratesButton = getView().findViewById(R.id.buttonRates);
        ratesButton.setOnClickListener(v -> {
            if (activity.isCoinsReady()){ spawnRatesPopup(); }
        });

        Button button = getView().findViewById(R.id.bankButton);
        button.setOnClickListener(v -> {
            if (!(spinnerItems.size() > 0 && selectedIndex < spinnerItems.size())) {

                Snackbar.make(getView(), R.string.bank_no_coins, Snackbar.LENGTH_LONG).show();

            } else if (activity.getPlayer().getBankedCount() < 25 && spinnerItems.get(selectedIndex) != null) {
                Coin tempCoin = spinnerItems.get(selectedIndex);
                tempCoin.setBanked(true);

                activity.getPlayer().setBankedCount(activity.getPlayer().getBankedCount() + 1);

                assert tempCoin != null;

                Double value = activity.getPlayer().getMulti() * tempCoin.getGoldValue();
                Log.d(tag, "Banked coin worth " + value + " gold");
                activity.getPlayer().setGoldBalance(activity.getPlayer().getGoldBalance() + value);
                activity.getPlayer().setLifetimeGold( activity.getPlayer().getLifetimeGold() + value);

                updateText();

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("user").document(activity.getPlayer().getEmail()).collection("Wallet").document(Objects.requireNonNull(tempCoin.getId())).set(Objects.requireNonNull(tempCoin));
                db.collection("user").document(activity.getPlayer().getEmail()).collection("Player").document(activity.getPlayer().getEmail()).set(activity.getPlayer());


                spinnerItems.remove(selectedIndex);

                ArrayAdapter<Coin> spinnerArrayAdapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), R.layout.spinner_item, spinnerItems);
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                spinner.setAdapter(spinnerArrayAdapter);

            } else {
                Snackbar.make(getView(), R.string.bank_all_used, Snackbar.LENGTH_LONG).show();

                Fragment fragment = new TransferFragment();
                activity.loadFragment(fragment, "transferFragment");

            }
        });

        setSpinner();

    }

    private void setSpinner(){
        //gets all collected but unbanked coins
        for (String id : activity.getCollectedIDs()){
            if (activity.getCoinsCollection().containsKey(id)) {
                Coin tempCoin = activity.getCoinsCollection().get(id);
                assert tempCoin != null;
                if(!tempCoin.isBanked()) {
                    spinnerItems.add(tempCoin);
                }
            }
        }

        //sort coins on gold value descending
        spinnerItems.sort((a, b) ->
                a.getGoldValue() < b.getGoldValue()  ? 1 : -1);

        ArrayAdapter<Coin> spinnerArrayAdapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), R.layout.spinner_item, spinnerItems);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(spinnerArrayAdapter);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void updateText(){
        TextView bankValue = Objects.requireNonNull(getView()).findViewById(R.id.balanceValue);
        bankValue.setText(String.format("%.3f", activity.getPlayer().getGoldBalance()) + " Gold");
        TextView countValue = getView().findViewById(R.id.countValue);
        countValue.setText(activity.getPlayer().getBankedCount() + "/25");
    }

    @SuppressLint("ClickableViewAccessibility")
    private void spawnRatesPopup(){

        LayoutInflater inflater = (LayoutInflater)
                activity.getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.rates_popup, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);


        TextView curr1 = popupView.findViewById(R.id.textCurrency1);
        curr1.setText("SHIL: " + activity.getExchangeRates().get("SHIL"));
        TextView curr2 = popupView.findViewById(R.id.textCurrency2);
        curr2.setText("DOLR: " + activity.getExchangeRates().get("DOLR"));
        TextView curr3 = popupView.findViewById(R.id.textCurrency3);
        curr3.setText("QUID: " + activity.getExchangeRates().get("QUID"));
        TextView curr4 = popupView.findViewById(R.id.textCurrency4);
        curr4.setText("PENY: " + activity.getExchangeRates().get("PENY"));


        // show the popup window
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener((v, event) -> {

            popupWindow.dismiss();

            return true;
        });
    }


    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
        selectedIndex = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }
}
