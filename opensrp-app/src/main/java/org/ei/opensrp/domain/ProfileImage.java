package org.ei.opensrp.domain;

public class ProfileImage {
    private String imageid;
    private String anmId;
    private String entityID;
    private String contenttype;
    private  String filepath;
    private  String syncStatus;
    private String filecategory;
    // FR
    private String filevector;
    private String bfrstatus;

    public ProfileImage(String imageid, String anmId, String entityID, String contenttype, String filepath, String syncStatus, String filecategory, String filevector, String bfrStatus) {
        this.imageid = imageid;
        this.entityID = entityID;
        this.anmId = anmId;
        this.contenttype = contenttype;
        this.filepath = filepath;
        this.syncStatus = syncStatus;
        this.filecategory = filecategory;
        this.filevector = filevector;
        this.bfrstatus = bfrStatus;
    }

    public ProfileImage() {

    }

    public String getFilecategory() {
        return filecategory;
    }

    public void setFilecategory(String filecategory) {
        this.filecategory = filecategory;
    }

    public String getImageid() {
        return imageid;
    }

    public void setImageid(String imageid) {
        this.imageid = imageid;
    }

    public String getAnmId() {
        return anmId;
    }

    public void setAnmId(String anmId) {
        this.anmId = anmId;
    }

    public String getEntityID() {
        return entityID;
    }

    public void setEntityID(String entityID) {
        this.entityID = entityID;
    }

    public String getContenttype() {
        return contenttype;
    }

    public void setContenttype(String contenttype) {
        this.contenttype = contenttype;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

    // FR
    public String getFilevector() {
        return filevector;
    }

    public void setFilevector(String filevector) {
        this.filevector = filevector;
    }

    public byte[] getBfrstatus() {
        return new byte[0];
    }
}
