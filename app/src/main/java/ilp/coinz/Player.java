package ilp.coinz;

public class Player {

    private int multi;
    private int radius;

    public Player(int multi, int radius) {
        this.multi = multi;
        this.radius = radius;
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
}
