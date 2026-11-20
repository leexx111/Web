package com.cms.electricityproject.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "dianliang")
/**
 * 电量及分时电价记录的数据模型。
 */
public class Dianliang {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "d_time")
    private Instant dTime;

    @Column(name = "jfpg")
    private Integer jfpg;

    @Column(name = "dl")
    private Double dl;

    @Column(name = "df")
    private Double df;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getDTime() {
        return dTime;
    }

    public void setDTime(Instant dTime) {
        this.dTime = dTime;
    }

    public Integer getJfpg() {
        return jfpg;
    }

    public void setJfpg(Integer jfpg) {
        this.jfpg = jfpg;
    }

    public Double getDl() {
        return dl;
    }

    public void setDl(Double dl) {
        this.dl = dl;
    }

    public Double getDf() {
        return df;
    }

    public void setDf(Double df) {
        this.df = df;
    }

}