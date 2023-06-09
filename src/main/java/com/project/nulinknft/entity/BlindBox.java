package com.project.nulinknft.entity;
import javax.persistence.*;

@Entity
@Table(name = "blind_box")
public class BlindBox extends BaseEntity {

    @Column(name = "user")
    private String user;

    @Column(name = "box_amount")
    private int boxAmount;

    @Column(name = "pay_amount")
    private int payAmount;

    @Column(name = "time")
    private String time;

    @Column(name = "recommender")
    private String recommender;
}
