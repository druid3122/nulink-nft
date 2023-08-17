package com.project.nulinknft.entity;
import javax.persistence.*;

@Entity
@Table(name = "blind_box")
public class BlindBox extends BaseEntity {

    @Column(name = "tx_hash", nullable = false)
    private String txHash;

    @Column(name = "user", nullable = false)
    private String user;

    @Column(name = "box_amount")
    private int boxAmount;

    @Column(name = "pay_amount")
    private int payAmount;

    @Column(name = "time")
    private String time;

    @Column(name = "recommender")
    private String recommender;

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getBoxAmount() {
        return boxAmount;
    }

    public void setBoxAmount(int boxAmount) {
        this.boxAmount = boxAmount;
    }

    public int getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(int payAmount) {
        this.payAmount = payAmount;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRecommender() {
        return recommender;
    }

    public void setRecommender(String recommender) {
        this.recommender = recommender;
    }
}
