package ilp.coinz;

public class Player {

    private int multi;
    private int radius;
    private int lifetimeCoins;
    private double lifetimeGold;
    private float lifetimeDistance;

    public Player(int multi, int radius, int lifetimeCoins, double lifetimeGold, float lifetimeDistance) {
        this.multi = multi;
        this.radius = radius;
        this.lifetimeCoins = lifetimeCoins;
        this.lifetimeGold = lifetimeGold;
        this.lifetimeDistance = lifetimeDistance;
    }

    public int getMulti() {
        return multi;
    }

    public void setMulti(int multi) {
        this.multi = multi;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getLifetimeCoins() {
        return lifetimeCoins;
    }

    public void setLifetimeCoins(int lifetimeCoins) {
        this.lifetimeCoins = lifetimeCoins;
    }

    public double getLifetimeGold() {
        return lifetimeGold;
    }

    public void setLifetimeGold(double lifetimeGold) {
        this.lifetimeGold = lifetimeGold;
    }

    public float getLifetimeDistance() {
        return lifetimeDistance;
    }

    public void setLifetimeDistance(float lifetimeDistance) {
        this.lifetimeDistance = lifetimeDistance;
    }
}


