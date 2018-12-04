package ilp.coinz;

import com.mapbox.mapboxsdk.annotations.BaseMarkerOptions;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Coin {


    private String id;
    private Double value;
    private String currency;
    private String symbol;
    private Double longitude;
    private Double latitude;
    private boolean collected;
    private boolean banked;
    private boolean transferred;

    public Coin(JSONObject coinjson){
        try {
            JSONObject tempprops = coinjson.getJSONObject("properties");
            this.id = tempprops.getString("id");
            this.value = Double.valueOf(tempprops.getString("value"));
            this.currency = tempprops.getString("currency");
            this.symbol = tempprops.getString("marker-symbol");
            JSONObject tempgeometry = coinjson.getJSONObject("geometry");
            JSONArray coords = tempgeometry.getJSONArray("coordinates");
            this.longitude = Double.valueOf(coords.getString(0));
            this.latitude = Double.valueOf(coords.getString(1));
            this.collected = false;
            this.banked = false;
            this.transferred = false;
        } catch (JSONException e){
            return;
        }
    }


    public String getId() {
        return id;
    }

    public Double getValue() {
        return value;
    }

    public String  getCurrency() {
        return currency;
    }

    public String getSymbol() {
        return symbol;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public boolean isCollected() {
        return collected;
    }

    public boolean isBanked() {
        return banked;
    }

    public boolean isTransferred() {
        return transferred;
    }

    public void setTransferred(boolean transferred) {
        this.transferred = transferred;
    }

    public void setCollected(boolean collected){
        this.collected = collected;
    }

    public void setBanked(boolean banked) {
        this.banked = banked;
    }
}
