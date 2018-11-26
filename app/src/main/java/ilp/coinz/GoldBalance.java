package ilp.coinz;

import java.io.Serializable;

public class GoldBalance implements Serializable {

    private Double balance;

    public GoldBalance(){
        this.balance = 0.0;
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
