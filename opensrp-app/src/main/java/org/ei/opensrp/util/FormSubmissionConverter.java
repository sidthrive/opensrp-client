package org.ei.opensrp.util;

/**
 * Created by Dani on 26/08/2017.
 */
import com.google.gson.Gson;
import org.ei.opensrp.domain.form.FormSubmissionDTO;
import org.ei.opensrp.domain.form.FormInstanceCouch;
import org.ei.opensrp.domain.form.FormSubmissionCouch;

import static java.lang.Long.parseLong;
import static java.lang.String.valueOf;

public class FormSubmissionConverter {

    public static FormSubmissionCouch toFormSubmission(FormSubmissionDTO FormSubmissionDTO) {
        try {
            FormInstanceCouch FormInstanceCouch = new Gson().fromJson(FormSubmissionDTO.instance(), FormInstanceCouch.class);
            return new FormSubmissionCouch("FormSubmission", FormSubmissionDTO.anmId(), FormSubmissionDTO.instanceId(), FormSubmissionDTO.formName(), FormSubmissionDTO.entityId(),
                    FormSubmissionDTO.formDataDefinitionVersion(), parseLong(FormSubmissionDTO.clientVersion()), FormInstanceCouch);
        } catch (Exception e) {
            throw e;
        }
    }

    public static FormSubmissionCouch toFormSubmissionWithVersion(FormSubmissionDTO FormSubmissionDTO) {
        return new FormSubmissionCouch("FormSubmission", FormSubmissionDTO.anmId(), FormSubmissionDTO.instanceId(), FormSubmissionDTO.formName(),
                FormSubmissionDTO.entityId(), parseLong(FormSubmissionDTO.clientVersion()), FormSubmissionDTO.formDataDefinitionVersion(), new Gson().fromJson(FormSubmissionDTO.instance(), FormInstanceCouch.class),
                parseLong(FormSubmissionDTO.serverVersion()));
    }
}