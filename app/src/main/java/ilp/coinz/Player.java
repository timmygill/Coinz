package ilp.coinz;

public class Player {

    private double goldBalance;
    private int multi;
    private int radius;
    private int lifetimeCoins;
    private double lifetimeGold;
    private float lifetimeDistance;


    public Player(double goldBalance, int multi, int radius, int lifetimeCoins, double lifetimeGold, float lifetimeDistance) {
        this.goldBalance = goldBalance;
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

    public double getGoldBalance() {
        return goldBalance;
    }

    public void setGoldBalance(double goldBalance) {
        this.goldBalance = goldBalance;
    }
}


