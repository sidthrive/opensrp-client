package org.ei.opensrp.service;

import android.content.Intent;

import ch.lambdaj.function.convert.Converter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.ei.opensrp.DristhiConfiguration;
import org.ei.opensrp.domain.FetchStatus;
import org.ei.opensrp.domain.Response;
import org.ei.opensrp.domain.form.FormSubmission;
import org.ei.opensrp.domain.form.FormSubmissionCouch;
import org.ei.opensrp.domain.form.FormSubmissionDTO;
import org.ei.opensrp.repository.AllSettings;
import org.ei.opensrp.repository.AllSharedPreferences;
import org.ei.opensrp.repository.FormDataRepository;
import org.ei.opensrp.util.FormSubmissionConverter;
import org.ei.opensrp.view.activity.DrishtiApplication;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.lambdaj.collection.LambdaCollections.with;
import static java.text.MessageFormat.format;
import static java.util.Collections.sort;
import static org.ei.opensrp.convertor.FormSubmissionConvertor.toDomain;
import static org.ei.opensrp.domain.FetchStatus.fetched;
import static org.ei.opensrp.domain.FetchStatus.fetchedFailed;
import static org.ei.opensrp.domain.FetchStatus.nothingFetched;
import static org.ei.opensrp.domain.FetchStatus.pushFailed;
import static org.ei.opensrp.domain.FetchStatus.pushAndFetchFailed;
import static org.ei.opensrp.util.Log.logError;
import static org.ei.opensrp.util.Log.logInfo;

public class FormSubmissionSyncService {
    public static final String FORM_SUBMISSIONS_PATH = "form-submissions";
    public static final String FORM_SUBMISSIONS_PATH_BY_LOC = "form-submissions-by-loc";
    private final HTTPAgent httpAgent;
    private final FormDataRepository formDataRepository;
    private AllSettings allSettings;
    private AllSharedPreferences allSharedPreferences;
    private FormSubmissionService formSubmissionService;
    private DristhiConfiguration configuration;
    private final int MAX_SIZE = 50;

    public FormSubmissionSyncService(FormSubmissionService formSubmissionService, HTTPAgent httpAgent,
                                     FormDataRepository formDataRepository, AllSettings allSettings,
                                     AllSharedPreferences allSharedPreferences, DristhiConfiguration configuration) {
        this.formSubmissionService = formSubmissionService;
        this.httpAgent = httpAgent;
        this.formDataRepository = formDataRepository;
        this.allSettings = allSettings;
        this.allSharedPreferences = allSharedPreferences;
        this.configuration = configuration;
    }

    public FetchStatus sync() {
        boolean pushStatus = pushToServer();
        Intent intent = new Intent(DrishtiApplication.getInstance().getApplicationContext(),ImageUploadSyncService.class);
        DrishtiApplication.getInstance().getApplicationContext().startService(intent);
        FetchStatus pullStatus = pullFromServerbyLoc();

        if((!pushStatus)&&pullStatus==fetchedFailed){
            return pushAndFetchFailed;
        }else if(pullStatus==fetchedFailed){
            return fetchedFailed;
        }else if((!pushStatus)&&(pullStatus==nothingFetched||pullStatus==fetched)){
            return pushFailed;
        }else if(pullStatus==nothingFetched){
            return nothingFetched;
        }else{
            return fetched;
        }
    }

    public boolean pushToServer() {
        boolean keepSyncing = true;
        while (keepSyncing) {
            List<FormSubmission> pendingFormSubmissions = formDataRepository.getPendingFormSubmissions(MAX_SIZE);
            if (pendingFormSubmissions.isEmpty()) {
                return true;
            }
            if(pendingFormSubmissions.size() < 50){
                keepSyncing = false;
            }
            List<FormSubmissionDTO> formSubmissionsDTO = new Gson().fromJson((String) mapToFormSubmissionDTO(pendingFormSubmissions), new TypeToken<List<FormSubmissionDTO>>() {
            }.getType());
            List<FormSubmissionCouch> formSubmissions = with(formSubmissionsDTO).convert(new Converter<FormSubmissionDTO, FormSubmissionCouch>() {
                @Override
                public FormSubmissionCouch convert(FormSubmissionDTO submission) {
                    return FormSubmissionConverter.toFormSubmission(submission).forSubmission(DateTime.now().getMillis());
                }
            });
            sort(formSubmissions, timeStampComparator());
            Map<String, List<FormSubmissionCouch>> docs = new HashMap<>();;
            docs.put("docs", formSubmissions);
            String submissionPayload = new Gson().toJson(docs);
            Response<String> response = httpAgent.postToCouch(
                    format("{0}/{1}",
                            "http://118.91.130.18:5983",
                            "opensrp-form/_bulk_docs"),
                    submissionPayload, "rootuser", "Satu23456");
            if (response.isFailure()) {
                logError(format("Form submissions sync failed. Submissions:  {0}", pendingFormSubmissions));
                return false;
            }
            formDataRepository.markFormSubmissionsAsSynced(pendingFormSubmissions);
            logInfo(format("Form submissions sync successfully. Submissions:  {0}", pendingFormSubmissions));
        }
        return true;
    }

    private Comparator<FormSubmissionCouch> timeStampComparator() {
        return new Comparator<FormSubmissionCouch>() {
            public int compare(FormSubmissionCouch firstSubmission, FormSubmissionCouch secondSubmission) {
                long firstTimestamp = firstSubmission.clientVersion();
                long secondTimestamp = secondSubmission.clientVersion();
                return firstTimestamp == secondTimestamp ? 0 : firstTimestamp < secondTimestamp ? -1 : 1;
            }
        };
    }

    public FetchStatus pullFromServer() {
        FetchStatus dataStatus = nothingFetched;
        String anmId = allSharedPreferences.fetchRegisteredANM();
        int downloadBatchSize = configuration.syncDownloadBatchSize();
        String baseURL = configuration.dristhiBaseURL();
        while (true) {
            String uri = format("{0}/{1}?anm-id={2}&timestamp={3}&batch-size={4}",
                    baseURL,
                    FORM_SUBMISSIONS_PATH,
                    anmId,
                    allSettings.fetchPreviousFormSyncIndex(),
                    downloadBatchSize);
            Response<String> response = httpAgent.fetch(uri);
            if (response.isFailure()) {
                logError(format("Form submis,sions pull failed."));
                return fetchedFailed;
            }
            List<FormSubmissionDTO> formSubmissions = new Gson().fromJson(response.payload(),
                    new TypeToken<List<FormSubmissionDTO>>() {
                    }.getType());
            if (formSubmissions.isEmpty()) {
                return dataStatus;
            } else {
                formSubmissionService.processSubmissions(toDomain(formSubmissions));
                dataStatus = fetched;
            }
        }
    }

    public FetchStatus pullFromServerbyLoc() {
        FetchStatus dataStatus = nothingFetched;
        String locationId = allSettings.fetchANMLocation();
        int downloadBatchSize = configuration.syncDownloadBatchSize();
        String baseURL = configuration.dristhiBaseURL();
        while (true) {
            String uri = format("{0}/{1}?locationId={2}&timestamp={3}&batch-size={4}",
                    baseURL,
                    FORM_SUBMISSIONS_PATH_BY_LOC,
                    locationId,
                    allSettings.fetchPreviousFormSyncIndex(),
                    downloadBatchSize);
            Response<String> response = httpAgent.fetch(uri);
            if (response.isFailure()) {
                logError(format("Form submis,sions pull failed."));
                return fetchedFailed;
            }
            List<FormSubmissionDTO> formSubmissions = new Gson().fromJson(response.payload(),
                    new TypeToken<List<FormSubmissionDTO>>() {
                    }.getType());
            if (formSubmissions.isEmpty()) {
                return dataStatus;
            } else {
                formSubmissionService.processSubmissions(toDomain(formSubmissions));
                dataStatus = fetched;
            }
        }
    }

    private String mapToFormSubmissionDTO(List<FormSubmission> pendingFormSubmissions) {
        List<FormSubmissionDTO> formSubmissions = new ArrayList<FormSubmissionDTO>();
        for (FormSubmission pendingFormSubmission : pendingFormSubmissions) {
            formSubmissions.add(new FormSubmissionDTO("FormSubmission",allSharedPreferences.fetchRegisteredANM(), pendingFormSubmission.instanceId(),
                    pendingFormSubmission.entityId(), pendingFormSubmission.formName(), allSettings.fetchANMLocation(), pendingFormSubmission.instance(), pendingFormSubmission.version(),
                    pendingFormSubmission.formDataDefinitionVersion()).withServerVersion(DateTime.now().getMillis()));
        }
        return new Gson().toJson(formSubmissions);
    }
}
