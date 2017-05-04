package org.ei.opensrp.indonesia;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

import org.ei.opensrp.Context;
import org.ei.opensrp.commonregistry.CommonPersonObjectController;
import org.ei.opensrp.cursoradapter.SmartRegisterQueryBuilder;
import org.ei.opensrp.event.Listener;

import org.ei.opensrp.indonesia.anc.ChildMergeID;
import org.ei.opensrp.indonesia.kartu_ibu.ChildRegistrationHandler;
import org.ei.opensrp.indonesia.lib.FlurryFacade;
import org.ei.opensrp.indonesia.pnc.PncOAHandler;
import org.ei.opensrp.service.PendingFormSubmissionService;
import org.ei.opensrp.sync.SyncAfterFetchListener;
import org.ei.opensrp.sync.SyncProgressIndicator;
import org.ei.opensrp.sync.UpdateActionsTask;
import org.ei.opensrp.view.activity.SecuredActivity;
import org.ei.opensrp.view.contract.HomeContext;
import org.ei.opensrp.view.controller.NativeAfterANMDetailsFetchListener;
import org.ei.opensrp.view.controller.NativeUpdateANMDetailsTask;
import org.ei.opensrp.view.fragment.DisplayFormFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.widget.Toast.LENGTH_SHORT;
import static java.lang.String.valueOf;
import static org.ei.opensrp.event.Event.ACTION_HANDLED;
import static org.ei.opensrp.event.Event.FORM_SUBMITTED;
import static org.ei.opensrp.event.Event.SYNC_COMPLETED;
import static org.ei.opensrp.event.Event.SYNC_STARTED;
import static org.ei.opensrp.indonesia.AllConstantsINA.FormNames.ANAK_BAYI_REGISTRATION;

public class BidanHomeActivity extends SecuredActivity {
    SimpleDateFormat timer = new SimpleDateFormat("hh:mm:ss");
    private MenuItem updateMenuItem;
    private MenuItem remainingFormsToSyncMenuItem;
    private PendingFormSubmissionService pendingFormSubmissionService;

    private Listener<Boolean> onSyncStartListener = new Listener<Boolean>() {
        @Override
        public void onEvent(Boolean data) {
            if (updateMenuItem != null) {
                updateMenuItem.setActionView(R.layout.progress);
            }
        }
    };

    private Listener<Boolean> onSyncCompleteListener = new Listener<Boolean>() {
        @Override
        public void onEvent(Boolean data) {
            //#TODO: RemainingFormsToSyncCount cannot be updated from a back ground thread!!
            updateRemainingFormsToSyncCount();
            if (updateMenuItem != null) {
                updateMenuItem.setActionView(null);
            }
            updateRegisterCounts();
        }
    };

    private Listener<String> onFormSubmittedListener = new Listener<String>() {
        @Override
        public void onEvent(String instanceId) {
            updateRegisterCounts();
        }
    };

    private Listener<String> updateANMDetailsListener = new Listener<String>() {
        @Override
        public void onEvent(String data) {
            updateRegisterCounts();
        }
    };

    private TextView ecRegisterClientCountView;
    private TextView kartuIbuANCRegisterClientCountView;
    private TextView kartuIbuPNCRegisterClientCountView;
    private TextView anakRegisterClientCountView;
    private TextView kohortKbCountView;
    private TextView ParanaClientCount;
    public static CommonPersonObjectController kicontroller;
    public static CommonPersonObjectController anccontroller;
    public static CommonPersonObjectController kbcontroller;
    public static CommonPersonObjectController childcontroller;
    public static CommonPersonObjectController pnccontroller;
    public static int kicount;
    public static int paranacount;
    private int kbcount;
    private int anccount;
    private int pnccount;
    private int childcount;

    @Override
    protected void onCreation() {
        //home dashboard

        String HomeStart = timer.format(new Date());
        Map<String, String> Home = new HashMap<String, String>();
        Home.put("start", HomeStart);
        FlurryAgent.logEvent("home_dashboard",Home, true );

       // FlurryAgent.logEvent("home_dashboard");
        setContentView(R.layout.smart_registers_home_bidan);
        navigationController = new NavigationControllerINA(this,anmController);
        setupViews();
        initialize();
        DisplayFormFragment.formInputErrorMessage = getResources().getString(R.string.forminputerror);
        DisplayFormFragment.okMessage = getResources().getString(R.string.okforminputerror);
      //  context.formSubmissionRouter().getHandlerMap().put("census_enrollment_form", new ANChandler());
        context.formSubmissionRouter().getHandlerMap().put("kartu_pnc_dokumentasi_persalinan", new ChildMergeID());
        context.formSubmissionRouter().getHandlerMap().put("kartu_pnc_regitration_oa", new PncOAHandler());
        context.formSubmissionRouter().getHandlerMap().put(ANAK_BAYI_REGISTRATION, new ChildRegistrationHandler());

        System.out.println("unique id = " + LoginActivity.generator.uniqueIdController().getAllUniqueId().toString());
    }

    private void setupViews() {
        findViewById(R.id.btn_kartu_ibu_register).setOnClickListener(onRegisterStartListener);
        findViewById(R.id.btn_kartu_ibu_anc_register).setOnClickListener(onRegisterStartListener);
        findViewById(R.id.btn_kartu_ibu_pnc_register).setOnClickListener(onRegisterStartListener);
        findViewById(R.id.btn_anak_register).setOnClickListener(onRegisterStartListener);
        findViewById(R.id.btn_kohort_kb_register).setOnClickListener(onRegisterStartListener);
        findViewById(R.id.btn_parana_register).setOnClickListener(onRegisterStartListener);


        findViewById(R.id.btn_reporting).setOnClickListener(onButtonsClickListener);
        findViewById(R.id.btn_videos).setOnClickListener(onButtonsClickListener);

        ecRegisterClientCountView = (TextView) findViewById(R.id.txt_kartu_ibu_register_client_count);
        kartuIbuANCRegisterClientCountView = (TextView) findViewById(R.id.txt_kartu_ibu_anc_register_client_count);
        kartuIbuPNCRegisterClientCountView = (TextView) findViewById(R.id.txt_kartu_ibu_pnc_register_client_count);
        anakRegisterClientCountView = (TextView) findViewById(R.id.txt_anak_client_count);
        kohortKbCountView = (TextView) findViewById(R.id.txt_kohort_kb_register_count);
        ParanaClientCount = (TextView) findViewById(R.id.txt_parana_client_count);

        findViewById(R.id.btn_videos).setVisibility(View.GONE);

        //.setVisibility(View.INVISIBLE);
    }

    private void initialize() {
        pendingFormSubmissionService = context.pendingFormSubmissionService();
        SYNC_STARTED.addListener(onSyncStartListener);
        SYNC_COMPLETED.addListener(onSyncCompleteListener);
        FORM_SUBMITTED.addListener(onFormSubmittedListener);
        ACTION_HANDLED.addListener(updateANMDetailsListener);

        getSupportActionBar().setTitle("");
        getSupportActionBar().setIcon(getResources().getDrawable(org.ei.opensrp.indonesia.R.mipmap.logo));
        getSupportActionBar().setLogo(org.ei.opensrp.indonesia.R.mipmap.logo);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        LoginActivity.setLanguage();
        updateFromServer();
//        getActionBar().setBackgroundDrawable(getReso
// urces().getDrawable(R.color.action_bar_background));
    }

    @Override
    protected void onResumption() {

        LoginActivity.setLanguage();
        updateRegisterCounts();
        updateSyncIndicator();
        updateRemainingFormsToSyncCount();
    }

    private void updateRegisterCounts() {
        NativeUpdateANMDetailsTask task = new NativeUpdateANMDetailsTask(Context.getInstance().anmController());
        task.fetch(new NativeAfterANMDetailsFetchListener() {
            @Override
            public void afterFetch(HomeContext anmDetails) {
                updateRegisterCounts(anmDetails);
            }
        });
    }

    private void updateRegisterCounts(HomeContext homeContext) {
        SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder();
        Cursor kicountcursor = context.commonrepository("kartu_ibu").RawCustomQueryForAdapter(sqb.queryForCountOnRegisters("kartu_ibu", "kartu_ibu.isClosed NOT Null and kartu_ibu.isClosed != '' and kartu_ibu.isClosed != 'true'"));
        kicountcursor.moveToFirst();
        kicount= kicountcursor.getInt(0);
        kicountcursor.close();

        Cursor kbcountcursor = context.commonrepository("kartu_ibu").RawCustomQueryForAdapter(sqb.queryForCountOnRegisters("kartu_ibu", "kartu_ibu.isClosed NOT Null and kartu_ibu.isClosed != '' and kartu_ibu.isClosed != 'true' and details not LIKE '%\"jenisKontrasepsi\":\"\"%'"));
        kbcountcursor.moveToFirst();
        kbcount= kbcountcursor.getInt(0);
        kbcountcursor.close();


        Cursor anccountcursor = context.commonrepository("ibu").RawCustomQueryForAdapter(sqb.queryForCountOnRegisters("ibu", "ibu.isClosed !='true' and ibu.type ='anc'"));
        anccountcursor.moveToFirst();
        anccount= anccountcursor.getInt(0);
        anccountcursor.close();


        Cursor pnccountcursor = context.commonrepository("ibu").RawCustomQueryForAdapter(sqb.queryForCountOnRegisters("ibu", "ibu.isClosed != 'true' and ibu.type = 'pnc'"));
        pnccountcursor.moveToFirst();
        pnccount= pnccountcursor.getInt(0);
        pnccountcursor.close();


        Cursor childcountcursor = context.commonrepository("anak").RawCustomQueryForAdapter(sqb.queryForCountOnRegisters("anak", "anak.isClosed NOT Null and anak.isClosed != '' and anak.isClosed != 'true' "));
        childcountcursor.moveToFirst();
        childcount= childcountcursor.getInt(0);
        childcountcursor.close();


        Cursor prcountcursor = context.commonrepository("kartu_ibu").RawCustomQueryForAdapter(sqb.queryForCountOnRegisters("kartu_ibu", "kartu_ibu.isClosed NOT Null and kartu_ibu.isClosed != '' and kartu_ibu.isClosed != 'true'"));
        prcountcursor.moveToFirst();
        paranacount= prcountcursor.getInt(0);
        prcountcursor.close();

        ecRegisterClientCountView.setText(valueOf(kicount));
        kartuIbuANCRegisterClientCountView.setText(valueOf(anccount));
        kartuIbuPNCRegisterClientCountView.setText(valueOf(pnccount));
        anakRegisterClientCountView.setText(valueOf(childcount));
        kohortKbCountView.setText(valueOf(kbcount));
        ParanaClientCount.setText(valueOf(paranacount));
       // ParanaClientCount.setText(paranacount);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        updateMenuItem = menu.findItem(R.id.updateMenuItem);
        remainingFormsToSyncMenuItem = menu.findItem(R.id.remainingFormsToSyncMenuItem);

        updateSyncIndicator();
        updateRemainingFormsToSyncCount();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.updateMenuItem:
                updateFromServer();
                return true;
            case R.id.switchLanguageMenuItem:
                String newLanguagePreference = LoginActivity.switchLanguagePreference();
                LoginActivity.setLanguage();
                Toast.makeText(this, "Language preference set to " + newLanguagePreference + ". Please restart the application.", LENGTH_SHORT).show();
                this.recreate();
                return true;
            case R.id.help:
                //  startActivity(new Intent(this, tutorialCircleViewFlow.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateFromServer() {
        FlurryFacade.logEvent("clicked_update_from_server");
        UpdateActionsTask updateActionsTask = new UpdateActionsTask(
                this, context.actionService(), context.formSubmissionSyncService(), new SyncProgressIndicator(), context.allFormVersionSyncService());

//        updateActionsTask.setAdditionalSyncService((context).uniqueIdService());
        if(LoginActivity.generator.uniqueIdController().needToRefillUniqueId(LoginActivity.generator.UNIQUE_ID_LIMIT)){
            System.out.println("unique id need to be reloaded");
            LoginActivity.generator.requestUniqueId();
        }

        updateActionsTask.updateFromServer(new SyncAfterFetchListener());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String HomeEnd = timer.format(new Date());
        Map<String, String> Home = new HashMap<String, String>();
        Home.put("end", HomeEnd);
        FlurryAgent.logEvent("home_dashboard",Home, true);
        SYNC_STARTED.removeListener(onSyncStartListener);
        SYNC_COMPLETED.removeListener(onSyncCompleteListener);
        FORM_SUBMITTED.removeListener(onFormSubmittedListener);
        ACTION_HANDLED.removeListener(updateANMDetailsListener);
    }

    private void updateSyncIndicator() {
        if (updateMenuItem != null) {
            if (context.allSharedPreferences().fetchIsSyncInProgress()) {
                updateMenuItem.setActionView(R.layout.progress);
            } else
                updateMenuItem.setActionView(null);
        }
    }

    private void updateRemainingFormsToSyncCount() {
        if (remainingFormsToSyncMenuItem == null) {
            return;
        }

        long size = pendingFormSubmissionService.pendingFormSubmissionCount();
        if (size > 0) {
            remainingFormsToSyncMenuItem.setTitle(valueOf(size) + " " + getString(R.string.unsynced_forms_count_message));
            remainingFormsToSyncMenuItem.setVisible(true);
        } else {
            remainingFormsToSyncMenuItem.setVisible(false);
        }
    }

    private View.OnClickListener onRegisterStartListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_kartu_ibu_register:
                    navigationController.startECSmartRegistry();
                    break;

                case R.id.btn_kohort_kb_register:
                        navigationController.startFPSmartRegistry();
                        break;

               case R.id.btn_kartu_ibu_anc_register:
                    navigationController.startANCSmartRegistry();
                   break;

               case R.id.btn_anak_register:
                    navigationController.startChildSmartRegistry();
                   break;

               case R.id.btn_kartu_ibu_pnc_register:
                   navigationController.startPNCSmartRegistry();
                   break;
                case R.id.btn_parana_register:
                    navigationController.startVideos();
                    break;
            }
            String HomeEnd = timer.format(new Date());
            Map<String, String> Home = new HashMap<String, String>();
            Home.put("end", HomeEnd);
            FlurryAgent.logEvent("home_dashboard",Home, true);
         //   FlurryAgent.endTimedEvent("home_dashboard");
        }
    };

    private View.OnClickListener onButtonsClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_reporting:
                    navigationController.startReports();
                    break;

               /* case R.id.btn_videos:
                    navigationController.startVideos();
                    break;*/
            }
        }
    };
}