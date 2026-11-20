package com.cms.electricityproject.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "power_alarm")
/**
 * 报警记录的数据模型，保存报警内容、状态和时间信息。
 */
public class PowerAlarm {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "alarm_name")
    private String alarmName;

    @Column(name = "alarm_content")
    private String alarmContent;

    @Column(name = "alarm_title")
    private String alarmTitle;

    @Column(name = "alarm_level")
    private String alarmLevel;

    @Column(name = "create_time")
    private Instant createTime;

    @Column(name = "update_time")
    private Instant updateTime;

    @Column(name = "alarm_status")
    private String alarmStatus;

    public PowerAlarm() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAlarmName() {
        return alarmName;
    }

    public void setAlarmName(String alarmName) {
        this.alarmName = alarmName;
    }

    public String getAlarmContent() {
        return alarmContent;
    }

    public void setAlarmContent(String alarmContent) {
        this.alarmContent = alarmContent;
    }

    public String getAlarmTitle() {
        return alarmTitle;
    }

    public void setAlarmTitle(String alarmTitle) {
        this.alarmTitle = alarmTitle;
    }

    public String getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarmLevel(String alarmLevel) {
        this.alarmLevel = alarmLevel;
    }

    public Instant getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Instant createTime) {
        this.createTime = createTime;
    }

    public Instant getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Instant updateTime) {
        this.updateTime = updateTime;
    }

    public String getAlarmStatus() {
        return alarmStatus;
    }

    public void setAlarmStatus(String alarmStatus) {
        this.alarmStatus = alarmStatus;
    }

    public PowerAlarm(Integer id, String alarmName, String alarmContent, String alarmTitle, String alarmLevel, Instant createTime, Instant updateTime, String alarmStatus) {
        this.id = id;
        this.alarmName = alarmName;
        this.alarmContent = alarmContent;
        this.alarmTitle = alarmTitle;
        this.alarmLevel = alarmLevel;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.alarmStatus = alarmStatus;
    }

    @Override
    public String toString() {
        return "PowerAlarm{" +
                "id=" + id +
                ", alarmName='" + alarmName + '\'' +
                ", alarmContent='" + alarmContent + '\'' +
                ", alarmTitle='" + alarmTitle + '\'' +
                ", alarmLevel='" + alarmLevel + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", alarmStatus='" + alarmStatus + '\'' +
                '}';
    }
}