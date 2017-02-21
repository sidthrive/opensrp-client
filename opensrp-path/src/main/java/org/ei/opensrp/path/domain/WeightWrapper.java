package org.ei.opensrp.path.domain;

import org.ei.opensrp.domain.Alert;
import org.ei.opensrp.path.db.VaccineRepo.Vaccine;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by keyman on 16/11/2016.
 */
public class WeightWrapper {
    private String id = UUID.randomUUID().toString();
    private Photo photo;
    private String patientName;
    private String patientNumber;
    private String patientAge;
    private Float weight;

    private DateTime updatedVaccineDate;

    private boolean today;

    public String getId() {
        return id;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientNumber() {
        return patientNumber;
    }

    public void setPatientNumber(String patientNumber) {
        this.patientNumber = patientNumber;
    }

    public DateTime getUpdatedVaccineDate() {
        return updatedVaccineDate;
    }

    public void setUpdatedVaccineDate(DateTime updatedVaccineDate, boolean today) {
        this.today = today;
        this.updatedVaccineDate = updatedVaccineDate;
    }

    public boolean isToday() {
        return today;
    }

    public String getUpdatedVaccineDateAsString() {
        return updatedVaccineDate != null ? updatedVaccineDate.toString("yyyy-MM-dd") : "";
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }

    public Photo getPhoto() {
        return photo;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }

    public Float getWeight() {
        return weight;
    }

    public void setPatientAge(String patientAge) {
        this.patientAge = patientAge;
    }

    public String getPatientAge() {
        return patientAge;
    }
}
