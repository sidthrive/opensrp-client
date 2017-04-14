package org.ei.opensrp.path.sync;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import org.ei.opensrp.domain.DownloadStatus;
import org.ei.opensrp.domain.FetchStatus;
import org.ei.opensrp.path.application.VaccinatorApplication;
import org.ei.opensrp.path.service.intent.PathReplicationIntentService;
import org.ei.opensrp.path.service.intent.PullUniqueIdsIntentService;
import org.ei.opensrp.path.service.intent.VaccineIntentService;
import org.ei.opensrp.path.service.intent.WeightIntentService;
import org.ei.opensrp.repository.EcRepository;
import org.ei.opensrp.service.ActionService;
import org.ei.opensrp.service.AllFormVersionSyncService;
import org.ei.opensrp.service.FormSubmissionSyncService;
import org.ei.opensrp.service.ImageUploadSyncService;
import org.ei.opensrp.sync.AdditionalSyncService;
import org.ei.opensrp.sync.UpdateActionsTask;
import org.ei.opensrp.view.BackgroundAction;
import org.ei.opensrp.view.LockingBackgroundTask;
import org.ei.opensrp.view.ProgressIndicator;

import static org.ei.opensrp.domain.FetchStatus.fetched;
import static org.ei.opensrp.domain.FetchStatus.nothingFetched;
import static org.ei.opensrp.util.Log.logInfo;

public class PathUpdateActionsTask extends UpdateActionsTask{

    public PathUpdateActionsTask(Context context, ActionService actionService, FormSubmissionSyncService formSubmissionSyncService, ProgressIndicator progressIndicator,
                                 AllFormVersionSyncService allFormVersionSyncService) {
        super(context,actionService,formSubmissionSyncService,progressIndicator,allFormVersionSyncService, (EcRepository) VaccinatorApplication.getInstance().getRepository());
        this.actionService = actionService;
        this.context = context;
        this.formSubmissionSyncService = formSubmissionSyncService;
        this.allFormVersionSyncService = allFormVersionSyncService;
        this.additionalSyncService = null;
        task = new LockingBackgroundTask(progressIndicator);
        this.httpAgent = org.ei.opensrp.Context.getInstance().getHttpAgent();
    }

    public void setAdditionalSyncService(AdditionalSyncService additionalSyncService) {
        this.additionalSyncService = additionalSyncService;
    }

    public void updateFromServer(final PathAfterFetchListener pathAfterFetchListener) {

        this.afterFetchListener = pathAfterFetchListener;
        if (org.ei.opensrp.Context.getInstance().IsUserLoggedOut()) {
            logInfo("Not updating from server as user is not logged in.");
            return;
        }

        task.doActionInBackground(new BackgroundAction<FetchStatus>() {

            public FetchStatus actionToDoInBackgroundThread() {

                FetchStatus fetchStatusForForms = sync(PathClientProcessor.getInstance(context));
                FetchStatus fetchStatusForActions = actionService.fetchNewActions();
                pathAfterFetchListener.partialFetch(fetchStatusForActions);

                startPullUniqueIdsIntentService(context);

                startVaccineIntentService(context);
                startWeightIntentService(context);

                startReplicationIntentService(context);

                startImageUploadIntentService(context);


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
                pathAfterFetchListener.afterFetch(result);
            }
        });
    }



    private void startReplicationIntentService(Context context) {
        Intent serviceIntent = new Intent(context, PathReplicationIntentService.class);
        context.startService(serviceIntent);
    }

    private void startImageUploadIntentService(Context context) {
        Intent intent = new Intent(context, ImageUploadSyncService.class);
        context.startService(intent);
    }

    private void startPullUniqueIdsIntentService(Context context) {
        Intent intent = new Intent(context, PullUniqueIdsIntentService.class);
        context.startService(intent);
    }

    private void startWeightIntentService(Context context) {
        Intent intent = new Intent(context, WeightIntentService.class);
        context.startService(intent);
    }

    private void startVaccineIntentService(Context context) {
        Intent intent = new Intent(context, VaccineIntentService.class);
        context.startService(intent);
    }
}