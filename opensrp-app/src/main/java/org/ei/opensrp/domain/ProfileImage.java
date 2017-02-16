package org.ei.opensrp.domain;

import org.ei.opensrp.AllConstants;
import org.ei.opensrp.Context;

import static java.text.MessageFormat.format;

public class ProfileImage {
    private String imageid;
    private String anmId;
    private String entityID;
    private String contenttype;
    private String filepath;
    private String syncStatus;
    private String filecategory;
    private String filevector;

    public ProfileImage(String imageid, String anmId, String entityID, String contenttype, String filepath, String syncStatus, String filecategory, String filevector) {
        this.imageid = imageid;
        this.entityID = entityID;
        this.anmId = anmId;
        this.contenttype = contenttype;
        this.filepath = filepath;
        this.syncStatus = syncStatus;
        this.filecategory = filecategory;
        this.filevector = filevector;
    }
    public ProfileImage(){}

//    public ProfileImage(String imageid, String anmId, String entityID, String contenttype, int filepath, String syncStatus, String filecategory, String filevector) {
//        this.imageid = imageid;
//        this.entityID = entityID;
//        this.anmId = anmId;
//        this.contenttype = contenttype;
//        this.filepath = filepath;
//        this.syncStatus = syncStatus;
//        this.filecategory = filecategory;
//        this.filevector = filevector;
//    }

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

    public String getImageUrl() {
        String url = format("{0}/{1}/{2}",
                Context.getInstance().allSharedPreferences().fetchBaseURL(""),
                AllConstants.PROFILE_IMAGES_DOWNLOAD_PATH, entityID);
        return url;
    }


    public String getFilevector() {
        return filevector;
    }

    public void setFilevector(String filevector) {
        this.filevector = filevector;
    }
}
