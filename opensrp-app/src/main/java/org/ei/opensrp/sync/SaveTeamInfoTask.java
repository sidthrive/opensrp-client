package org.ei.opensrp.sync;

/**
 * Created by Dani on 06/10/2017.
 */

import org.ei.opensrp.repository.AllSettings;
import org.ei.opensrp.util.Log;
import org.ei.opensrp.view.BackgroundAction;
import org.ei.opensrp.view.LockingBackgroundTask;
import org.ei.opensrp.view.ProgressIndicator;

public class SaveTeamInfoTask {
    private LockingBackgroundTask lockingBackgroundTask;
    private AllSettings allSettings;

    public SaveTeamInfoTask(AllSettings allSettings) {
        this.allSettings = allSettings;
        lockingBackgroundTask = new LockingBackgroundTask(new ProgressIndicator() {
            @Override
            public void setVisible() {
            }

            @Override
            public void setInvisible() {
                Log.logInfo("Successfully saved Team information");
            }
        });
    }

    public void save(final String userInfo) {
        lockingBackgroundTask.doActionInBackground(new BackgroundAction<Object>() {
            @Override
            public Object actionToDoInBackgroundThread() {
                allSettings.saveTeamInformation(userInfo);
                return userInfo;
            }

            @Override
            public void postExecuteInUIThread(Object result) {

            }
        });
    }
}
