package org.ei.opensrp.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by keyman on 3/1/17.
 */
public class Vaccine {
    Long id;
    String baseEntityId;
    String name;
    Integer calculation;
    Date date;
    String anmId;
    String locationId;
    String syncStatus;
    Long updatedAt;

    public Vaccine() {
    }

    public Vaccine(Long id, String baseEntityId, String name, Integer calculation, Date date, String anmId, String locationId, String syncStatus, Long updatedAt) {
        this.id = id;
        this.baseEntityId = baseEntityId;
        this.name = name;
        this.calculation = calculation;
        this.date = date;
        this.anmId = anmId;
        this.locationId = locationId;
        this.syncStatus = syncStatus;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBaseEntityId() {
        return baseEntityId;
    }

    public void setBaseEntityId(String baseEntityId) {
        this.baseEntityId = baseEntityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCalculation() {
        return calculation;
    }

    public void setCalculation(Integer calculation) {
        this.calculation = calculation;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getAnmId() {
        return anmId;
    }

    public void setAnmId(String anmId) {
        this.anmId = anmId;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }
}
