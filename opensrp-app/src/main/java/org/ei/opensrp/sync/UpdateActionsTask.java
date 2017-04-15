package org.ei.opensrp.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.ei.opensrp.AllConstants;
import org.ei.opensrp.domain.DownloadStatus;
import org.ei.opensrp.domain.FetchStatus;
import org.ei.opensrp.domain.Response;
import org.ei.opensrp.repository.AllSharedPreferences;
import org.ei.opensrp.repository.EcRepository;
import org.ei.opensrp.service.ActionService;
import org.ei.opensrp.service.AllFormVersionSyncService;
import org.ei.opensrp.service.FormSubmissionSyncService;
import org.ei.opensrp.service.HTTPAgent;
import org.ei.opensrp.view.BackgroundAction;
import org.ei.opensrp.view.LockingBackgroundTask;
import org.ei.opensrp.view.ProgressIndicator;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Map;

import static org.ei.opensrp.domain.FetchStatus.fetched;
import static org.ei.opensrp.domain.FetchStatus.fetchedFailed;
import static org.ei.opensrp.domain.FetchStatus.nothingFetched;
import static org.ei.opensrp.util.Log.logInfo;

public class UpdateActionsTask {
    protected LockingBackgroundTask task;
    protected ActionService actionService;
    protected Context context;
    protected FormSubmissionSyncService formSubmissionSyncService;
    protected AllFormVersionSyncService allFormVersionSyncService;
    protected AdditionalSyncService additionalSyncService;
    protected static final String EVENTS_SYNC_PATH = "/rest/event/add";
    protected HTTPAgent httpAgent;
    protected EcRepository db;
    protected AfterFetchListener afterFetchListener;

    public UpdateActionsTask(Context context, ActionService actionService, FormSubmissionSyncService formSubmissionSyncService, ProgressIndicator progressIndicator,
                             AllFormVersionSyncService allFormVersionSyncService, EcRepository _db) {
        this.actionService = actionService;
        this.context = context;
        this.formSubmissionSyncService = formSubmissionSyncService;
        this.allFormVersionSyncService = allFormVersionSyncService;
        this.additionalSyncService = null;
        task = new LockingBackgroundTask(progressIndicator);
        db = _db;
    }

    public void setAdditionalSyncService(AdditionalSyncService additionalSyncService) {
        this.additionalSyncService = additionalSyncService;
    }

    public void updateFromServer(final AfterFetchListener _afterFetchListener) {
        afterFetchListener = _afterFetchListener;
        if (org.ei.opensrp.Context.getInstance().IsUserLoggedOut()) {
            logInfo("Not updating from server as user is not logged in.");
            return;
        }

        task.doActionInBackground(new BackgroundAction<FetchStatus>() {
            public FetchStatus actionToDoInBackgroundThread() {

                FetchStatus fetchStatusForForms = sync(ClientProcessor.getInstance(context));
                FetchStatus fetchStatusForActions = actionService.fetchNewActions();
                FetchStatus fetchStatusAdditional = additionalSyncService == null ? nothingFetched : additionalSyncService.sync();

                if (org.ei.opensrp.Context.getInstance().configuration().shouldSyncForm()) {

                    allFormVersionSyncService.verifyFormsInFolder();
                    FetchStatus fetchVersionStatus = allFormVersionSyncService.pullFormDefinitionFromServer();
                    DownloadStatus downloadStatus = allFormVersionSyncService.downloadAllPendingFormFromServer();

                    if (downloadStatus == DownloadStatus.downloaded) {
                        allFormVersionSyncService.unzipAllDownloadedFormFile();
                    }

                    if (fetchVersionStatus == fetched || downloadStatus == DownloadStatus.downloaded) {
                        return fetched;
                    }
                }


                if (fetchStatusForActions == fetched || fetchStatusForForms == fetched || fetchStatusAdditional == fetched)
                    return fetched;

                return fetchStatusForForms;
            }

            public void postExecuteInUIThread(FetchStatus result) {
                if (result != null && context != null && result != nothingFetched) {
                    Toast.makeText(context, result.displayValue(), Toast.LENGTH_SHORT).show();
                }
                afterFetchListener.afterFetch(result);
            }
        });
    }

    /**
     * Processor is passed here to allow custom implementations being used
     * @param processor
     * @return
     */
    protected FetchStatus sync(ClientProcessor processor) {
        try {
            int totalCount = 0;
            pushToServer();
            ECSyncUpdater ecUpdater = ECSyncUpdater.getInstance(context, db);

            // Retrieve database host from preferences
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
            ecUpdater.updateLastCheckTimeStamp(Calendar.getInstance().getTimeInMillis());
            while (true) {
                long startSyncTimeStamp = ecUpdater.getLastSyncTimeStamp();

                int eCount = ecUpdater.fetchAllClientsAndEvents(AllConstants.SyncFilters.FILTER_PROVIDER, allSharedPreferences.fetchRegisteredANM());
                totalCount += eCount;

                if (eCount == 0 || eCount < 0) {
                    break;
                }

                long lastSyncTimeStamp = ecUpdater.getLastSyncTimeStamp();
                processor.processClient(ecUpdater.allEvents(startSyncTimeStamp, lastSyncTimeStamp));
                Log.i(getClass().getName(), "!!!!! Sync count:  " + eCount);
                afterFetchListener.partialFetch(fetched);
            }

            if (totalCount == 0) {
                return nothingFetched;
            } else if (totalCount < 0) {
                return fetchedFailed;
            } else {
                return fetched;
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "", e);
            return fetchedFailed;
        }

    }
    protected FetchStatus syncByLocation(ClientProcessor processor) {
        try {
            int totalCount = 0;
            pushToServer();
            ECSyncUpdater ecUpdater = ECSyncUpdater.getInstance(context, db);

            // Retrieve database host from preferences
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
            ecUpdater.updateLastCheckTimeStamp(Calendar.getInstance().getTimeInMillis());
            while (true) {
                long startSyncTimeStamp = ecUpdater.getLastSyncTimeStamp();
                String locationId="Gerantung";//allSharedPreferences.getPreference(AllConstants.SyncFilters.FILTER_LOCATION_ID);

                int eCount = ecUpdater.fetchAllClientsAndEvents(AllConstants.SyncFilters.FILTER_LOCATION_ID, locationId);
                totalCount += eCount;

                if (eCount == 0 || eCount < 0) {
                    break;
                }

                long lastSyncTimeStamp = ecUpdater.getLastSyncTimeStamp();

                processor.processClient(ecUpdater.allEvents(startSyncTimeStamp, lastSyncTimeStamp));
                Log.i(getClass().getName(), "!!!!! Sync count:  " + eCount);
                afterFetchListener.partialFetch(fetched);
            }

            if (totalCount == 0) {
                return nothingFetched;
            } else if (totalCount < 0) {
                return fetchedFailed;
            } else {
                return fetched;
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "", e);
            return fetchedFailed;
        }

    }
    protected void pushToServer() {
        boolean keepSyncing = true;
        int limit = 50;
        try {
            // db.markAllAsUnSynced();

            while (keepSyncing) {
                Map<String, Object> pendingEvents = null;
                pendingEvents = db.getUnSyncedEvents(limit);

                if (pendingEvents.isEmpty()) {
                    return;
                }

                String baseUrl = org.ei.opensrp.Context.getInstance().configuration().dristhiBaseURL();
                if (baseUrl.endsWith("/")) {
                    baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/"));
                }
                // create request body
                JSONObject request = new JSONObject();
                if (pendingEvents.containsKey("clients")) {
                    request.put("clients", pendingEvents.get("clients"));
                }
                if (pendingEvents.containsKey("events")) {
                    request.put("events", pendingEvents.get("events"));
                }
                String jsonPayload = request.toString();
                Response<String> response = httpAgent.post(
                        MessageFormat.format("{0}/{1}",
                                baseUrl,
                                EVENTS_SYNC_PATH),
                        jsonPayload);
                if (response.isFailure()) {
                    Log.e(getClass().getName(), "Events sync failed.");
                    return;
                }
                db.markEventsAsSynced(pendingEvents);
                Log.i(getClass().getName(), "Events synced successfully.");
            }
        } catch (JSONException e) {
            Log.e(getClass().getName(), e.getMessage());
        } catch (ParseException e) {
            Log.e(getClass().getName(), e.getMessage());
        } catch (UnsupportedEncodingException e) {
            Log.e(getClass().getName(), e.getMessage());
        }
    }
}