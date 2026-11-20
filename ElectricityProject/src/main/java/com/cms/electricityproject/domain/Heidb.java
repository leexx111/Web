package com.cms.electricityproject.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "heidb")
/**
 * 历史/高压电气数据的数据模型。
 */
public class Heidb {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "h_id", nullable = false)
    private Integer id;

    @Column(name = "h_name")
    private String hName;

    @Column(name = "h_grouping")
    private String hGrouping;

    @Column(name = "h_data", nullable = false)
    private Instant hData;

    @ColumnDefault("0")
    @Column(name = "Ua")
    private Double ua;

    @ColumnDefault("0")
    @Column(name = "Ub")
    private Double ub;

    @ColumnDefault("0")
    @Column(name = "Uc")
    private Double uc;

    @ColumnDefault("0")
    @Column(name = "Uab")
    private Double uab;

    @ColumnDefault("0")
    @Column(name = "Ubc", nullable = false)
    private Double ubc;

    @ColumnDefault("0")
    @Column(name = "Uca", nullable = false)
    private Double uca;

    @ColumnDefault("0")
    @Column(name = "la", nullable = false)
    private Double la;

    @ColumnDefault("0")
    @Column(name = "lb")
    private Double lb;

    @Column(name = "lc", nullable = false)
    private Double lc;

    @ColumnDefault("0")
    @Column(name = "Pa", nullable = false)
    private Double pa;

    @ColumnDefault("0")
    @Column(name = "Pb")
    private Double pb;

    @ColumnDefault("0")
    @Column(name = "Pc")
    private Double pc;

    @ColumnDefault("0")
    @Column(name = "Ps")
    private Double ps;

    @ColumnDefault("0")
    @Column(name = "Qa")
    private Double qa;

    @ColumnDefault("0")
    @Column(name = "Qb", nullable = false)
    private Double qb;

    @ColumnDefault("0")
    @Column(name = "Qc")
    private Double qc;

    @ColumnDefault("0")
    @Column(name = "Qs")
    private Double qs;

    @ColumnDefault("0")
    @Column(name = "VAa")
    private Double vAa;

    @ColumnDefault("0")
    @Column(name = "VAb")
    private Double vAb;

    @ColumnDefault("0")
    @Column(name = "VAc")
    private Double vAc;

    @Column(name = "VAs")
    private Double vAs;

    @ColumnDefault("0")
    @Column(name = "PFa")
    private Double pFa;

    @ColumnDefault("0")
    @Column(name = "PFb")
    private Double pFb;

    @Column(name = "PFc")
    private Double pFc;

    @ColumnDefault("0")
    @Column(name = "PFs")
    private Double pFs;

    @ColumnDefault("0")
    @Column(name = "pinlv")
    private Double pinlv;

    @ColumnDefault("0")
    @Column(name = "zygDianDu")
    private Double zygDianDu;

    @ColumnDefault("0")
    @Column(name = "zxygDianDu")
    private Double zxygDianDu;

    @ColumnDefault("0")
    @Column(name = "fxygDianDu")
    private Double fxygDianDu;

    @ColumnDefault("0")
    @Column(name = "zwgDianDu")
    private Double zwgDianDu;

    @ColumnDefault("0")
    @Column(name = "zcwgDianDu")
    private Double zcwgDianDu;

    @ColumnDefault("0")
    @Column(name = "fxwgDianDu")
    private Double fxwgDianDu;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getHName() {
        return hName;
    }

    public void setHName(String hName) {
        this.hName = hName;
    }

    public String getHGrouping() {
        return hGrouping;
    }

    public void setHGrouping(String hGrouping) {
        this.hGrouping = hGrouping;
    }

    public Instant getHData() {
        return hData;
    }

    public void setHData(Instant hData) {
        this.hData = hData;
    }

    public Double getUa() {
        return ua;
    }

    public void setUa(Double ua) {
        this.ua = ua;
    }

    public Double getUb() {
        return ub;
    }

    public void setUb(Double ub) {
        this.ub = ub;
    }

    public Double getUc() {
        return uc;
    }

    public void setUc(Double uc) {
        this.uc = uc;
    }

    public Double getUab() {
        return uab;
    }

    public void setUab(Double uab) {
        this.uab = uab;
    }

    public Double getUbc() {
        return ubc;
    }

    public void setUbc(Double ubc) {
        this.ubc = ubc;
    }

    public Double getUca() {
        return uca;
    }

    public void setUca(Double uca) {
        this.uca = uca;
    }

    public Double getLa() {
        return la;
    }

    public void setLa(Double la) {
        this.la = la;
    }

    public Double getLb() {
        return lb;
    }

    public void setLb(Double lb) {
        this.lb = lb;
    }

    public Double getLc() {
        return lc;
    }

    public void setLc(Double lc) {
        this.lc = lc;
    }

    public Double getPa() {
        return pa;
    }

    public void setPa(Double pa) {
        this.pa = pa;
    }

    public Double getPb() {
        return pb;
    }

    public void setPb(Double pb) {
        this.pb = pb;
    }

    public Double getPc() {
        return pc;
    }

    public void setPc(Double pc) {
        this.pc = pc;
    }

    public Double getPs() {
        return ps;
    }

    public void setPs(Double ps) {
        this.ps = ps;
    }

    public Double getQa() {
        return qa;
    }

    public void setQa(Double qa) {
        this.qa = qa;
    }

    public Double getQb() {
        return qb;
    }

    public void setQb(Double qb) {
        this.qb = qb;
    }

    public Double getQc() {
        return qc;
    }

    public void setQc(Double qc) {
        this.qc = qc;
    }

    public Double getQs() {
        return qs;
    }

    public void setQs(Double qs) {
        this.qs = qs;
    }

    public Double getVAa() {
        return vAa;
    }

    public void setVAa(Double vAa) {
        this.vAa = vAa;
    }

    public Double getVAb() {
        return vAb;
    }

    public void setVAb(Double vAb) {
        this.vAb = vAb;
    }

    public Double getVAc() {
        return vAc;
    }

    public void setVAc(Double vAc) {
        this.vAc = vAc;
    }

    public Double getVAs() {
        return vAs;
    }

    public void setVAs(Double vAs) {
        this.vAs = vAs;
    }

    public Double getPFa() {
        return pFa;
    }

    public void setPFa(Double pFa) {
        this.pFa = pFa;
    }

    public Double getPFb() {
        return pFb;
    }

    public void setPFb(Double pFb) {
        this.pFb = pFb;
    }

    public Double getPFc() {
        return pFc;
    }

    public void setPFc(Double pFc) {
        this.pFc = pFc;
    }

    public Double getPFs() {
        return pFs;
    }

    public void setPFs(Double pFs) {
        this.pFs = pFs;
    }

    public Double getPinlv() {
        return pinlv;
    }

    public void setPinlv(Double pinlv) {
        this.pinlv = pinlv;
    }

    public Double getZygDianDu() {
        return zygDianDu;
    }

    public void setZygDianDu(Double zygDianDu) {
        this.zygDianDu = zygDianDu;
    }

    public Double getZxygDianDu() {
        return zxygDianDu;
    }

    public void setZxygDianDu(Double zxygDianDu) {
        this.zxygDianDu = zxygDianDu;
    }

    public Double getFxygDianDu() {
        return fxygDianDu;
    }

    public void setFxygDianDu(Double fxygDianDu) {
        this.fxygDianDu = fxygDianDu;
    }

    public Double getZwgDianDu() {
        return zwgDianDu;
    }

    public void setZwgDianDu(Double zwgDianDu) {
        this.zwgDianDu = zwgDianDu;
    }

    public Double getZcwgDianDu() {
        return zcwgDianDu;
    }

    public void setZcwgDianDu(Double zcwgDianDu) {
        this.zcwgDianDu = zcwgDianDu;
    }

    public Double getFxwgDianDu() {
        return fxwgDianDu;
    }

    public void setFxwgDianDu(Double fxwgDianDu) {
        this.fxwgDianDu = fxwgDianDu;
    }

}