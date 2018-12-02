package ilp.coinz;

import java.io.Serializable;

public class GoldBalance implements Serializable {

    private Double balance;

    public GoldBalance(Double balance){
        this.balance = balance;
    }

    public void increaseBalance(Double increment){
        this.balance += increment;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }
}
