package org.ei.opensrp.path.domain;

import com.vijay.jsonwizard.widgets.LabelFactory;

import org.ei.opensrp.domain.Alert;
import org.ei.opensrp.path.db.VaccineRepo.Vaccine;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by keyman on 16/11/2016.
 */
public class VaccineWrapper {
    private String id = UUID.randomUUID().toString();
    private Photo photo;
    ;
    private String status;
    private List<Vaccine> vaccines;
    private DateTime vaccineDate;
    private Alert alert;
    private String previousVaccineId;
    private boolean compact;

    private String color;
    private String formattedVaccineDate;
    private String existingAge;

    private String patientName;
    private String patientNumber;

    private DateTime updatedVaccineDate;

    private boolean today;

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Vaccine> vaccines() {
        if (vaccines == null) {
            vaccines = new ArrayList<>();
        }
        return vaccines;
    }

    public void addVaccine(Vaccine vaccine) {
        this.vaccines().add(vaccine);
    }

    public DateTime getVaccineDate() {
        return vaccineDate;
    }

    public void setVaccineDate(DateTime vaccineDate) {
        this.vaccineDate = vaccineDate;
    }

    public Alert getAlert() {
        return alert;
    }

    public void setAlert(Alert alert) {
        this.alert = alert;
    }

    public String getPreviousVaccineId() {
        return previousVaccineId;
    }

    public void setPreviousVaccine(String previousVaccineId) {
        this.previousVaccineId = previousVaccineId;
    }

    public boolean isCompact() {
        return compact;
    }

    public void setCompact(boolean compact) {
        this.compact = compact;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getFormattedVaccineDate() {
        return formattedVaccineDate;
    }

    public void setFormattedVaccineDate(String formattedVaccineDate) {
        this.formattedVaccineDate = formattedVaccineDate;
    }

    public String getExistingAge() {
        return existingAge;
    }

    public void setExistingAge(String existingAge) {
        this.existingAge = existingAge;
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

    public String getVaccineDateAsString() {
        return vaccineDate != null ? vaccineDate.toString("yyyy-MM-dd") : "";
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

}
