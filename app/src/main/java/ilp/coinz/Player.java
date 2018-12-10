package ilp.coinz;

public class Player {

    private String email;
    private double goldBalance;
    private String lastLogin;
    private int consecLogins;
    private int bankedCount;
    private int multi;
    private int radius;
    private int lifetimeCoins;
    private double lifetimeGold;
    private float lifetimeDistance;

    public Player(){

    }


    public Player(String email, double goldBalance, String lastLogin, int consecLogins, int bankedCount, int multi, int radius, int lifetimeCoins, double lifetimeGold, float lifetimeDistance) {
        this.email = email;
        this.goldBalance = goldBalance;
        this.lastLogin = lastLogin;
        this.consecLogins = consecLogins;
        this.bankedCount = bankedCount;
        this.multi = multi;
        this.radius = radius;
        this.lifetimeCoins = lifetimeCoins;
        this.lifetimeGold = lifetimeGold;
        this.lifetimeDistance = lifetimeDistance;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getMulti() {
        return multi;
    }

    public void setMulti(int multi) {
        this.multi = multi;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    public int getConsecLogins() {
        return consecLogins;
    }

    public void setConsecLogins(int consecLogins) {
        this.consecLogins = consecLogins;
    }

    public int getBankedCount() {
        return bankedCount;
    }

    public void setBankedCount(int bankedCount) {
        this.bankedCount = bankedCount;
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


