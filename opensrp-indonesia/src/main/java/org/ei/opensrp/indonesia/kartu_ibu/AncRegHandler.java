package org.ei.opensrp.indonesia.kartu_ibu;

import org.ei.opensrp.Context;
import org.ei.opensrp.commonregistry.AllCommonsRepository;
import org.ei.opensrp.commonregistry.CommonPersonObject;
import org.ei.opensrp.domain.form.FormSubmission;
import org.ei.opensrp.service.formSubmissionHandler.FormSubmissionHandler;
import org.ei.opensrp.util.Log;

import java.util.HashMap;

/**
 * Created by Iq on 06/06/17.
 */
public class AncRegHandler implements FormSubmissionHandler {

    static String bindobject = "kartu_ibu";
    protected Context context;
    private AllCommonsRepository allCommonsRepository;

    public AncRegHandler() {

    }

    @Override
    public void handle(FormSubmission submission) {

        String entityID = submission.getFieldValue("motherId");


        AllCommonsRepository kiRepository = org.ei.opensrp.Context.getInstance().allCommonsRepositoryobjects("ibu");

        CommonPersonObject kiobject = kiRepository.findByCaseID(entityID);

     //   AllCommonsRepository iburep = org.ei.opensrp.Context.getInstance().allCommonsRepositoryobjects("kartu_ibu");
     //   final CommonPersonObject ibuparent = iburep.findByCaseID(kiobject.getColumnmaps().get("kartuIbuId"));

        String KiId = kiobject.getColumnmaps().get("kartuIbuId");
        Log.logInfo("Anc Reg"+KiId);
        HashMap<String, String> merge = new HashMap<String, String>();
        merge.put("MotherId", entityID);
        org.ei.opensrp.Context.getInstance().allCommonsRepositoryobjects(bindobject).mergeDetails(KiId, merge);

    }
}