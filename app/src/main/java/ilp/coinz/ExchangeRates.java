package ilp.coinz;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

import static ilp.coinz.Currency.SHIL;

public class ExchangeRates {
    private HashMap<Currency, Double> exchanges = new HashMap<Currency, Double>();

    public ExchangeRates(JSONObject rates) {

        for (int i = 0; i < rates.length(); i++) {
            Iterator<String> keys = rates.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                try {
                    exchanges.put(Currency.valueOf(key), (Double) rates.get(key));
                } catch (JSONException e) {
                    return;
                }
            }
        }
    }
    public Double getRate(Currency c){
        return exchanges.get(c);
    }
}
