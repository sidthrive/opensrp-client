package org.ei.opensrp.path.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.vijay.jsonwizard.activities.JsonFormActivity;

import org.ei.opensrp.Context;
import org.ei.opensrp.domain.FetchStatus;
import org.ei.opensrp.path.R;
import org.ei.opensrp.path.sync.ECSyncUpdater;
import org.ei.opensrp.path.sync.PathUpdateActionsTask;
import org.ei.opensrp.path.toolbar.BaseToolbar;
import org.ei.opensrp.repository.AllSharedPreferences;
import org.ei.opensrp.repository.UniqueIdRepository;
import org.ei.opensrp.sync.AfterFetchListener;
import org.ei.opensrp.sync.SyncProgressIndicator;
import org.ei.opensrp.util.FormUtils;
import org.ei.opensrp.view.activity.DrishtiApplication;
import org.ei.opensrp.view.activity.SettingsActivity;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Seconds;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opensrp.api.constants.Gender;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import util.JsonFormUtils;

/**
 * Base activity class for all other PATH activity classes. Implements:
 * - A uniform navigation bar that is launched by swiping from the left
 * - Support for specifying which {@link BaseToolbar} to use
 * <p/>
 * This activity requires that the base view for any child activity be {@link DrawerLayout}
 * Make sure include the navigation view as the last element in the activity's root DrawerLayout
 * like this:
 * <p/>
 * <include layout="@layout/nav_view_base"/>
 * <p/>
 * Created by Jason Rogena - jrogena@ona.io on 16/02/2017.
 */
public abstract class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "BaseActivity";
    private BaseToolbar toolbar;
    private Menu menu;
    private static final int REQUEST_CODE_GET_JSON = 3432;
    private AfterFetchListener afterFetchListener;
    private boolean isSyncing;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
        toolbar = (BaseToolbar) findViewById(getToolbarId());
        setSupportActionBar(toolbar);
        if (getToolbarId() != R.id.child_detail_toolbar) {


        DrawerLayout drawer = (DrawerLayout) findViewById(getDrawerLayoutId());
        BaseActivityToggle toggle = new BaseActivityToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        toggleIsSyncing();
         }

        afterFetchListener = new AfterFetchListener() {
            @Override
            public void afterFetch(FetchStatus fetchStatus) {
                isSyncing = false;
                toggleIsSyncing();
            }
        };

        initializeProgressDialog();
    }

    private void toggleIsSyncing() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null && navigationView.getMenu() != null) {
            MenuItem syncMenuItem = navigationView.getMenu().findItem(R.id.nav_sync);
            if (syncMenuItem != null) {
                if (isSyncing) {
                    syncMenuItem.setTitle(R.string.syncing);
                } else {
                    String lastSync = getLastSyncTime();

                    if (!TextUtils.isEmpty(lastSync)) {
                        lastSync = " " + String.format(getString(R.string.last_sync), lastSync);
                    }
                    syncMenuItem.setTitle(String.format(getString(R.string.sync_), lastSync));
                }
            }
        }
    }

    private String getLastSyncTime() {
        String lastSync = "";
        long milliseconds = ECSyncUpdater.getInstance(this).getLastCheckTimeStamp();
        if (milliseconds > 0) {
            DateTime lastSyncTime = new DateTime(milliseconds);
            DateTime now = new DateTime(Calendar.getInstance());
            Minutes minutes = Minutes.minutesBetween(lastSyncTime, now);
            if (minutes.getMinutes() < 1) {
                Seconds seconds = Seconds.secondsBetween(lastSyncTime, now);
                lastSync = seconds.getSeconds() + "s";
            } else if (minutes.getMinutes() >= 1 && minutes.getMinutes() < 60) {
                lastSync = minutes.getMinutes() + "m";
            } else if (minutes.getMinutes() >= 60 && minutes.getMinutes() < 1440) {
                Hours hours = Hours.hoursBetween(lastSyncTime, now);
                lastSync = hours.getHours() + "h";
            } else {
                Days days = Days.daysBetween(lastSyncTime, now);
                lastSync = days.getDays() + "d";
            }
        }
        return lastSync;
    }

    @Override
    protected void onResume() {
        super.onResume();
        initViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(toolbar.getSupportedMenu(), menu);
        toolbar.prepareMenu();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(toolbar.onMenuItemSelected(item));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(getDrawerLayoutId());
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void initViews() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Button logoutButton = (Button) navigationView.findViewById(R.id.logout_b);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrishtiApplication application = (DrishtiApplication) getApplication();
                application.logoutCurrentUser();
                finish();
            }
        });

        ImageButton cancelButton = (ImageButton) navigationView.findViewById(R.id.cancel_b);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawer = (DrawerLayout) BaseActivity.this.findViewById(getDrawerLayoutId());
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
            }
        });

        TextView initialsTV = (TextView) navigationView.findViewById(R.id.initials_tv);
        String preferredName = getOpenSRPContext().allSharedPreferences().getANMPreferredName(
                getOpenSRPContext().allSharedPreferences().fetchRegisteredANM());
        if (!TextUtils.isEmpty(preferredName)) {
            String[] initialsArray = preferredName.split(" ");
            String initials = "";
            if (initialsArray.length > 0) {
                initials = initialsArray[0].substring(0, 1);
                if (initialsArray.length > 1) {
                    initials = initials + initialsArray[1].substring(0, 1);
                }
            }

            initialsTV.setText(initials.toUpperCase());
        }

        TextView nameTV = (TextView) navigationView.findViewById(R.id.name_tv);
        nameTV.setText(preferredName);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_register) {
            startChildRegistration();
        } else if (id == R.id.nav_record_vaccination_out_catchment) {

        }/* else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }*/ else if (id == R.id.nav_sync) {
            isSyncing = true;
            toggleIsSyncing();
            PathUpdateActionsTask pathUpdateActionsTask = new PathUpdateActionsTask(
                    this, getOpenSRPContext().actionService(),
                    getOpenSRPContext().formSubmissionSyncService(),
                    new SyncProgressIndicator(),
                    getOpenSRPContext().allFormVersionSyncService());
            pathUpdateActionsTask.updateFromServer(afterFetchListener);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(getDrawerLayoutId());
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Updates all gender affected views
     *
     * @param gender The gender to update the
     */
    protected int[] updateGenderViews(Gender gender) {
        int darkShade = R.color.gender_neutral_dark_green;
        int normalShade = R.color.gender_neutral_green;
        int lightSade = R.color.gender_neutral_light_green;

        if (gender.equals(Gender.FEMALE)) {
            darkShade = R.color.female_dark_pink;
            normalShade = R.color.female_pink;
            lightSade = R.color.female_light_pink;
        } else if (gender.equals(Gender.MALE)) {
            darkShade = R.color.male_dark_blue;
            normalShade = R.color.male_blue;
            lightSade = R.color.male_light_blue;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(darkShade));
        }
        toolbar.setBackground(new ColorDrawable(getResources().getColor(normalShade)));
        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);
        viewGroup.setBackground(new ColorDrawable(getResources().getColor(lightSade)));

        return new int[]{darkShade, normalShade, lightSade};
    }

    protected void startChildRegistration() {
        try {
            UniqueIdRepository uniqueIdRepo = org.ei.opensrp.Context.getInstance().uniqueIdRepository();
            String entityId = uniqueIdRepo.getNextUniqueId() != null ? uniqueIdRepo.getNextUniqueId().getOpenmrsId() : "";
            if (entityId.isEmpty()) {
                Toast.makeText(this, getString(R.string.no_openmrs_id), Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject form = FormUtils.getInstance(getApplicationContext()).getFormJson("child_enrollment");
            JsonFormUtils.addChildRegLocHierarchyQuestions(form, getOpenSRPContext());
            if (form != null) {
                Intent intent = new Intent(getApplicationContext(), JsonFormActivity.class);
                //inject zeir id into the form
                JSONObject stepOne = form.getJSONObject(JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(JsonFormUtils.ZEIR_ID)) {
                        jsonObject.remove(JsonFormUtils.VALUE);
                        jsonObject.put(JsonFormUtils.VALUE, entityId);
                        continue;
                    }
                }
                intent.putExtra("json", form.toString());
                startActivityForResult(intent, REQUEST_CODE_GET_JSON);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String jsonString = data.getStringExtra("json");

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);

            JsonFormUtils.save(this, jsonString, allSharedPreferences.fetchRegisteredANM(), "Child_Photo", "child", "mother");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected BaseToolbar getToolbar() {
        return toolbar;
    }

    /**
     * The layout resource file to user for this activity
     *
     * @return The resource id for the layout file to use
     */
    protected abstract int getContentView();

    /**
     * The id for the base {@link DrawerLayout} for the activity
     *
     * @return
     */
    protected abstract int getDrawerLayoutId();

    /**
     * The id for the toolbar used in this activity
     *
     * @return The id for the toolbar used
     */
    protected abstract int getToolbarId();

    public Context getOpenSRPContext() {
        return Context.getInstance().updateApplicationContext(this.getApplicationContext());
    }

    public Menu getMenu() {
        return menu;
    }

    /**
     * The activity to go back to
     *
     * @return
     */
    protected abstract Class onBackActivity();

    private class BaseActivityToggle extends ActionBarDrawerToggle {

        public BaseActivityToggle(Activity activity, DrawerLayout drawerLayout, @StringRes int openDrawerContentDescRes, @StringRes int closeDrawerContentDescRes) {
            super(activity, drawerLayout, openDrawerContentDescRes, closeDrawerContentDescRes);
        }

        public BaseActivityToggle(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar, @StringRes int openDrawerContentDescRes, @StringRes int closeDrawerContentDescRes) {
            super(activity, drawerLayout, toolbar, openDrawerContentDescRes, closeDrawerContentDescRes);
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
            toggleIsSyncing();
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            super.onDrawerClosed(drawerView);
        }
    }

    public void processInThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            new Thread(runnable).start();
        }
    }

    private void initializeProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(getString(R.string.saving_dialog_title));
        progressDialog.setMessage(getString(R.string.saving_dialog_message));
    }

    protected  void showProgressDialog(){
        if(progressDialog != null){
            progressDialog.show();
        }
    }

    protected  void hideProgressDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }
}
