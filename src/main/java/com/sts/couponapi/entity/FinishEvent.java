package com.sts.couponapi.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name="finish_event")
@Getter
@Setter
@NoArgsConstructor
public class FinishEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @Column(name = "coupon_type")
    private String couponType;

    @Column(name = "date")
    private Long date;

    @Column(name = "count")
    private Long count;
}
