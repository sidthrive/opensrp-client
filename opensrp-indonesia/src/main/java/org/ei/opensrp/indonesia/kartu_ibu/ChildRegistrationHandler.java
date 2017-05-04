package org.ei.opensrp.indonesia.kartu_ibu;

import org.ei.opensrp.Context;
import org.ei.opensrp.commonregistry.AllCommonsRepository;
import org.ei.opensrp.commonregistry.CommonPersonObject;
import org.ei.opensrp.domain.form.FormSubmission;
import org.ei.opensrp.service.formSubmissionHandler.FormSubmissionHandler;

import java.util.HashMap;


/**
 * Created by Iq on 15/09/16.
 */
public class ChildRegistrationHandler implements FormSubmissionHandler{
    static String bindobject = "kartu_ibu";
    protected Context context;
    private AllCommonsRepository allCommonsRepository;
    public ChildRegistrationHandler() {

    }

    @Override
    public void handle(FormSubmission submission) {

        String entityID = submission.getFieldValue("childId");

        AllCommonsRepository childRepository = Context.getInstance().allCommonsRepositoryobjects("anak");
        CommonPersonObject childobject = childRepository.findByCaseID(entityID);

        AllCommonsRepository iburep = Context.getInstance().allCommonsRepositoryobjects("ibu");
        final CommonPersonObject ibuparent = iburep.findByCaseID(childobject.getColumnmaps().get("ibuCaseId"));

        HashMap<String, String> merge = new HashMap<String, String>();
        merge.put("childId", entityID);
        Context.getInstance().allCommonsRepositoryobjects(bindobject).mergeDetails(ibuparent.getColumnmaps().get("kartuIbuId"),merge);

    }
}