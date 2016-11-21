package org.ei.opensrp.sync;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.cloudant.sync.datastore.Datastore;
import com.cloudant.sync.notifications.ReplicationCompleted;
import com.cloudant.sync.notifications.ReplicationErrored;
import com.cloudant.sync.replication.PullFilter;
import com.cloudant.sync.replication.Replicator;
import com.cloudant.sync.replication.ReplicatorBuilder;
import com.cloudant.sync.event.Subscribe;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.ei.opensrp.AllConstants;
import org.ei.opensrp.repository.AllSharedPreferences;
import org.ei.opensrp.util.AssetHandler;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Handles cloundant replication/sync processes.
 * Created by koros on 3/16/16.
 */
public class CloudantSyncHandler {
    private static final String LOG_TAG = "CloudantSyncHandler";


    private Replicator mPushReplicator;
    private Replicator mPullReplicator;

    private final Context mContext;
    private final Handler mHandler;
    private CloudantSyncListener mListener;
    private CountDownLatch countDownLatch;

    private String dbURL;

    private static CloudantSyncHandler instance;

    public static CloudantSyncHandler getInstance(Context context) {
        if (instance == null) {
            instance = new CloudantSyncHandler(context);
        }
        return instance;
    }

    public CloudantSyncHandler(Context context) {
        this.mContext = context;
        // Allow us to switch code called by the ReplicationListener into
        // the main thread so the UI can update safely.
        this.mHandler = new Handler(Looper.getMainLooper());
        try {

            // Retrieve database host from preferences
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.mContext);
            AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
            String locationAnmids=allSharedPreferences.getPreference(AllConstants.SyncFilters.FILTER_LOCATION_ID);

            String port = AllConstants.CloudantSync.COUCHDB_PORT;
            String databaseName = AllConstants.CloudantSync.COUCH_DATABASE_NAME;
            dbURL = allSharedPreferences.fetchHost("").concat(":").concat(port).concat("/").concat(databaseName);

            // Replication Filter by provider
            String designDocumentId = this.replicationFilterSettings();
            PullFilter pullFilter = null;

            if (designDocumentId != null) {
                String filterDoc = designDocumentId.split("/")[1];
                HashMap<String, String> filterParams = new HashMap<String, String>();
                filterParams.put(AllConstants.SyncFilters.FILTER_LOCATION_ID,locationAnmids);
                pullFilter = new PullFilter(filterDoc.concat("/").concat(AllConstants.SyncFilters.FILTER_LOCATION_ID), filterParams);
            }

            this.reloadReplicationSettings(pullFilter);

        } catch (URISyntaxException e) {
            Log.e(LOG_TAG, "Unable to construct remote URI from configuration", e);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception While setting up datastore", e);
        }

    }

    //
    // GETTERS AND SETTERS
    //

    /**
     * Sets the listener for replication callbacks as a weak reference.
     *
     * @param listener {@link CloudantSyncListener} to receive callbacks.
     */
    public void setReplicationListener(CloudantSyncListener listener) {
        this.mListener = listener;
    }

    //
    // MANAGE REPLICATIONS
    //

    /**
     * <p>Stops running replications.</p>
     * <p/>
     * <p>The stop() methods stops the replications asynchronously, see the
     * replicator docs for more information.</p>
     */
    public void stopAllReplications() {
        if (this.mPullReplicator != null) {
            this.mPullReplicator.stop();
        }
        if (this.mPushReplicator != null) {
            this.mPushReplicator.stop();
        }
    }

    /**
     * <p>Starts the configured push replication.</p>
     */
    public void startPushReplication() {
        if (this.mPushReplicator != null) {
            this.mPushReplicator.start();
        } else {
            throw new RuntimeException("Push replication not set up correctly");
        }
    }

    /**
     * <p>Starts the configured pull replication.</p>
     */
    public void startPullReplication() {
        if (this.mPullReplicator != null) {
            this.mPullReplicator.start();
        } else {
            throw new RuntimeException("Push replication not set up correctly");
        }
    }

    /**
     * <p>Stops running replications and reloads the replication settings from
     * the app's preferences.</p>
     */
    public void reloadReplicationSettings(PullFilter pullFilter) throws URISyntaxException, Exception {
        this.stopAllReplications();

        // Set up the new replicator objects
        URI uri = this.createServerURI();

        CloudantDataHandler mCloudantDataHandler = CloudantDataHandler.getInstance(mContext);
        Datastore mDatastore = mCloudantDataHandler.getDatastore();

        if (pullFilter != null) {
            mPullReplicator = ReplicatorBuilder.pull().to(mDatastore).from(uri).filter(pullFilter).build();

        } else {
            mPullReplicator = ReplicatorBuilder.pull().to(mDatastore).from(uri).build();
        }
        mPushReplicator = ReplicatorBuilder.push().from(mDatastore).to(uri).build();

        mPushReplicator.getEventBus().register(this);
        mPullReplicator.getEventBus().register(this);

        Log.d(LOG_TAG, "Set up replicators for URI:" + uri.toString());
    }

    /**
     * <p>Returns the URI for the remote database, based on the app's
     * configuration.</p>
     *
     * @return the remote database's URI
     * @throws URISyntaxException if the settings give an invalid URI
     */
    private URI createServerURI() throws URISyntaxException {
        // We recommend always using HTTPS to talk to Cloudant.
        return new URI(dbURL);
    }

    //
    // REPLICATIONLISTENER IMPLEMENTATION
    //

    /**
     * Calls the SecuredActivity's replicationComplete method on the main thread,
     * as the complete() callback will probably come from a replicator worker
     * thread.
     */
    @Subscribe
    public void complete(final ReplicationCompleted rc) {
        // Call the logic to break down CE into case models
        try {
            if(rc.documentsReplicated > 0) {
                ClientProcessor.getInstance(mContext.getApplicationContext()).processClient();
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.toString(), e);
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // Fire callback if a replicationListener exists
                if (mListener != null) {
                    mListener.replicationComplete();
                }

                if (countDownLatch != null) {
                    countDownLatch.countDown();
                }

                // Fire this incase the replication was lauched from an intent service
                Intent localIntent = new Intent(AllConstants.CloudantSync.ACTION_REPLICATION_COMPLETED);
                // Puts the status into the Intent
                localIntent.putExtra(AllConstants.CloudantSync.DOCUMENTS_REPLICATED, rc.documentsReplicated);
                localIntent.putExtra(AllConstants.CloudantSync.BATCHES_REPLICATED, rc.batchesReplicated);
                // Broadcasts the Intent to receivers in this app.
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(localIntent);
            }
        });
    }

    /**
     * Calls the SecuredActivity's replicationComplete method on the main thread,
     * as the error() callback will probably come from a replicator worker
     * thread.
     */
    @Subscribe
    public void error(final ReplicationErrored re) {
        Log.e(LOG_TAG, "Replication error:", re.errorInfo.getException());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // Fire callback if a replicationListener exists
                if (mListener != null) {
                    mListener.replicationError();
                }

                if (countDownLatch != null) {
                    countDownLatch.countDown();
                }

                //Fire this incase the replication was lauched from an intent service
                Intent localIntent = new Intent(AllConstants.CloudantSync.ACTION_REPLICATION_ERROR);
                // Puts the status into the Intent
                localIntent.putExtra(AllConstants.CloudantSync.REPLICATION_ERROR, re.errorInfo.getException().getMessage());
                // Broadcasts the Intent to receivers in this app.
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(localIntent);
            }
        });
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    public String replicationFilterSettings() {
        try {
            String localJsonString = getFileContents("sync_filters.json");
            JsonObject localJson = new JsonParser().parse(localJsonString).getAsJsonObject();

            String designDocumentId = localJson.get("_id").getAsString();
            if(designDocumentId == null || designDocumentId.isEmpty() || !designDocumentId.contains("_design/")){
                return null;
            }

            JsonObject serverJson = getReplicationFiler(designDocumentId);
            if (serverJson == null) {
                return null;
            }

            JsonElement idElement = serverJson.get("_id");
            if (idElement != null && idElement.getAsString().equals(designDocumentId)) {
                //Compare filters and update if the filters are different
                JsonObject syncFiltersElement = localJson.get("filters").getAsJsonObject();
                JsonObject serverFiltersElement = serverJson.get("filters").getAsJsonObject();
                if (!syncFiltersElement.equals(serverFiltersElement)) {
                    String rev = serverJson.get("_rev").getAsString();
                    localJson.addProperty("_rev", rev);
                    setReplicationFilter(localJson, designDocumentId);

                }
                return designDocumentId;
            }

            // Define replication filter
            setReplicationFilter(localJson, designDocumentId);
            return designDocumentId;

        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception While getting sync filters json", e);
            return null;
        }


    }

    public JsonObject setReplicationFilter(JsonObject jsonObject, String designDocumentId) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

            String json = gson.toJson(jsonObject);

            HttpClient client = new DefaultHttpClient();
            HttpPut put = new HttpPut(dbURL.concat("/".concat(designDocumentId)));


            StringEntity stringEntity = new StringEntity(json, "utf-8");
            put.setEntity(stringEntity);
            put.setHeader("Content-type", "application/json; charset=utf-8");
            put.setHeader("Accept", "application/json");

            HttpResponse response = client.execute(put);
            HttpEntity entity = response.getEntity();
            InputStream in = entity.getContent();

            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(new InputStreamReader(in));

            return root.getAsJsonObject();


        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception While setting replication filter", e);
            return null;
        }
    }

    public JsonObject getReplicationFiler(String designDocumentId) {
        try {
            HttpClient httpclient = new DefaultHttpClient();

            HttpGet get = new HttpGet(dbURL.concat("/".concat(designDocumentId)));

            HttpResponse response = httpclient.execute(get);
            HttpEntity entity = response.getEntity();
            InputStream in = entity.getContent();

            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(new InputStreamReader(in));

            return root.getAsJsonObject();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception While getting replication filter", e);
            return null;
        }
    }

    private String getFileContents(String fileName) {
        return AssetHandler.readFileFromAssetsFolder(fileName, mContext);
    }
}