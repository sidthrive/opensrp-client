package org.ei.opensrp.domain;

import java.util.Date;

/**
 * Created by keyman on 3/1/17.
 */
public class Weight {
    Long id;
    String baseEntityId;
    Float kg;
    Date date;
    String anmId;
    String syncStatus;
    Date updatedAt;

    public Weight() {
    }

    public Weight(Long id, String baseEntityId, Float kg, Date date, String anmId, String syncStatus, Date updatedAt) {
        this.id = id;
        this.baseEntityId = baseEntityId;
        this.kg = kg;
        this.date = date;
        this.anmId = anmId;
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

    public Float getKg() {
        return kg;
    }

    public void setKg(Float kg) {
        this.kg = kg;
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

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }
}
