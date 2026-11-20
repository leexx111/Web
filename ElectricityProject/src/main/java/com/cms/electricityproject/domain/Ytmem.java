package com.cms.electricityproject.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "ytmem")
/**
 * 电气测量记录的数据模型。
 */
public class Ytmem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "data", nullable = false)
    private Instant data;

    @Column(name = "fenzu")
    private String fenzu;

    @Column(name = "F")
    private Double f;

    @Column(name = "Q")
    private Double q;

    @Column(name = "S")
    private Double s;

    @Column(name = "W")
    private Double w;

    @Column(name = "AP")
    private Double ap;

    @Column(name = "AQ")
    private Double aq;

    @Column(name = "BP")
    private Double bp;

    @Column(name = "BQ")
    private Double bq;

    @Column(name = "CP")
    private Double cp;

    @Column(name = "CQ")
    private Double cq;

    @Column(name = "EP")
    private Double ep;

    @Column(name = "EQ")
    private Double eq;

    @Column(name = "Ia")
    private Double ia;

    @Column(name = "Ib")
    private Double ib;

    @Column(name = "Ic")
    private Double ic;

    @Column(name = "PF")
    private Double pf;

    @Column(name = "Qa")
    private Double qa;

    @Column(name = "Qb")
    private Double qb;

    @Column(name = "Qc")
    private Double qc;

    @Column(name = "Sa")
    private Double sa;

    @Column(name = "Sb")
    private Double sb;

    @Column(name = "Sc")
    private Double sc;

    @Column(name = "Wa")
    private Double wa;

    @Column(name = "Wb")
    private Double wb;

    @Column(name = "Wc")
    private Double wc;

    @Column(name = "DI1")
    private String di1;

    @Column(name = "PFa")
    private Double pFa;

    @Column(name = "PFb")
    private Double pFb;

    @Column(name = "PFc")
    private Double pFc;

    @Column(name = "Uab")
    private Double uab;

    @Column(name = "Uan")
    private Double uan;

    @Column(name = "Ubc")
    private Double ubc;

    @Column(name = "Ubn")
    private Double ubn;

    @Column(name = "Uca")
    private Double uca;

    @Column(name = "Ucn")
    private Double ucn;

    @Column(name = "ADSP")
    private Double adsp;

    @Column(name = "ADSQ")
    private Double adsq;

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

    public Instant getData() {
        return data;
    }

    public void setData(Instant data) {
        this.data = data;
    }

    public String getFenzu() {
        return fenzu;
    }

    public void setFenzu(String fenzu) {
        this.fenzu = fenzu;
    }

    public Double getF() {
        return f;
    }

    public void setF(Double f) {
        this.f = f;
    }

    public Double getQ() {
        return q;
    }

    public void setQ(Double q) {
        this.q = q;
    }

    public Double getS() {
        return s;
    }

    public void setS(Double s) {
        this.s = s;
    }

    public Double getW() {
        return w;
    }

    public void setW(Double w) {
        this.w = w;
    }

    public Double getAp() {
        return ap;
    }

    public void setAp(Double ap) {
        this.ap = ap;
    }

    public Double getAq() {
        return aq;
    }

    public void setAq(Double aq) {
        this.aq = aq;
    }

    public Double getBp() {
        return bp;
    }

    public void setBp(Double bp) {
        this.bp = bp;
    }

    public Double getBq() {
        return bq;
    }

    public void setBq(Double bq) {
        this.bq = bq;
    }

    public Double getCp() {
        return cp;
    }

    public void setCp(Double cp) {
        this.cp = cp;
    }

    public Double getCq() {
        return cq;
    }

    public void setCq(Double cq) {
        this.cq = cq;
    }

    public Double getEp() {
        return ep;
    }

    public void setEp(Double ep) {
        this.ep = ep;
    }

    public Double getEq() {
        return eq;
    }

    public void setEq(Double eq) {
        this.eq = eq;
    }

    public Double getIa() {
        return ia;
    }

    public void setIa(Double ia) {
        this.ia = ia;
    }

    public Double getIb() {
        return ib;
    }

    public void setIb(Double ib) {
        this.ib = ib;
    }

    public Double getIc() {
        return ic;
    }

    public void setIc(Double ic) {
        this.ic = ic;
    }

    public Double getPf() {
        return pf;
    }

    public void setPf(Double pf) {
        this.pf = pf;
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

    public Double getSa() {
        return sa;
    }

    public void setSa(Double sa) {
        this.sa = sa;
    }

    public Double getSb() {
        return sb;
    }

    public void setSb(Double sb) {
        this.sb = sb;
    }

    public Double getSc() {
        return sc;
    }

    public void setSc(Double sc) {
        this.sc = sc;
    }

    public Double getWa() {
        return wa;
    }

    public void setWa(Double wa) {
        this.wa = wa;
    }

    public Double getWb() {
        return wb;
    }

    public void setWb(Double wb) {
        this.wb = wb;
    }

    public Double getWc() {
        return wc;
    }

    public void setWc(Double wc) {
        this.wc = wc;
    }

    public String getDi1() {
        return di1;
    }

    public void setDi1(String di1) {
        this.di1 = di1;
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

    public Double getUab() {
        return uab;
    }

    public void setUab(Double uab) {
        this.uab = uab;
    }

    public Double getUan() {
        return uan;
    }

    public void setUan(Double uan) {
        this.uan = uan;
    }

    public Double getUbc() {
        return ubc;
    }

    public void setUbc(Double ubc) {
        this.ubc = ubc;
    }

    public Double getUbn() {
        return ubn;
    }

    public void setUbn(Double ubn) {
        this.ubn = ubn;
    }

    public Double getUca() {
        return uca;
    }

    public void setUca(Double uca) {
        this.uca = uca;
    }

    public Double getUcn() {
        return ucn;
    }

    public void setUcn(Double ucn) {
        this.ucn = ucn;
    }

    public Double getAdsp() {
        return adsp;
    }

    public void setAdsp(Double adsp) {
        this.adsp = adsp;
    }

    public Double getAdsq() {
        return adsq;
    }

    public void setAdsq(Double adsq) {
        this.adsq = adsq;
    }

}