package org.ei.opensrp.path.tabfragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.domain.Vaccine;
import org.ei.opensrp.path.R;
import org.ei.opensrp.path.activity.ChildDetailTabbedActivity;
import org.ei.opensrp.path.domain.VaccineWrapper;
import org.ei.opensrp.path.listener.VaccinationActionListener;
import org.ei.opensrp.path.view.VaccineGroup;
import org.ei.opensrp.path.viewComponents.WidgetFactory;
import org.ei.opensrp.repository.VaccineRepository;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class child_under_five_fragment extends Fragment implements VaccinationActionListener {

    private LayoutInflater inflater;
    private ViewGroup container;
    private LinearLayout fragmentcontainer;
    private List<Vaccine> vaccineList;
    private CommonPersonObjectClient childDetails;
    private Map<String,String> detailmaps;
    private ArrayList<VaccineGroup> vaccineGroups;
    private static final String VACCINES_FILE = "vaccines.json";


    public child_under_five_fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;
        this.container = container;
        if (this.getArguments() != null) {
            Serializable serializable = getArguments().getSerializable(ChildDetailTabbedActivity.EXTRA_CHILD_DETAILS);
            if (serializable != null && serializable instanceof CommonPersonObjectClient) {
                childDetails = (CommonPersonObjectClient) serializable;
            }
        }
        View fragmenttwo = inflater.inflate(R.layout.child_under_five_fragment, container, false);
         fragmentcontainer = (LinearLayout)fragmenttwo.findViewById(R.id.container);
        LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        HashMap<String,String> weightmap = new HashMap<String, String>();
        weightmap.put("9 m","8.4");
        weightmap.put("8 m","7.5 Kg");
        weightmap.put("7 m","6.7 Kg");
        weightmap.put("6 m","5.6 Kg");
        weightmap.put("5 m","5.0 Kg");
        WidgetFactory wd = new WidgetFactory();

        ArrayList<String> vaccines = new ArrayList<String>();
        vaccines.add("BCG");
        vaccines.add("OPV1");
        vaccines.add("OPV2");
        vaccines.add("OPV3");
        vaccines.add("PCV1");
        vaccines.add("PCV2");
        vaccines.add("PCV3");
        vaccines.add("Penta 1");
        vaccines.add("Penta 2");
        vaccines.add("Penta 3");
        vaccines.add("Penta 4");
        vaccines.add("Penta 5");
        vaccines.add("Measles 1");
        vaccines.add("Measles 2");

        VaccineRepository vaccineRepository = ((ChildDetailTabbedActivity)getActivity()).getOpenSRPContext().vaccineRepository();
        vaccineList = vaccineRepository.findByEntityId(childDetails.entityId());


        fragmentcontainer.addView(wd.createWeightWidget(inflater,container,weightmap));
        fragmentcontainer.addView(wd.createImmunizationWidget(inflater,container,vaccineList,false));


        // Inflate the layout for this fragment
        return fragmenttwo;
    }
    public void loadview(boolean editmode){
//        View fragmenttwo = inflater.inflate(R.layout.child_under_five_fragment, container, false);
//        LinearLayout fragmentcontainer = (LinearLayout)fragmenttwo.findViewById(R.id.container);
        fragmentcontainer.removeAllViews();
        HashMap<String,String> weightmap = new HashMap<String, String>();
        weightmap.put("9 m","8.4");
        weightmap.put("8 m","7.5 Kg");
        weightmap.put("7 m","6.7 Kg");
        weightmap.put("6 m","5.6 Kg");
        weightmap.put("5 m","5.0 Kg");
        WidgetFactory wd = new WidgetFactory();

        ArrayList<String> vaccines = new ArrayList<String>();
        vaccines.add("BCG");
        vaccines.add("OPV1");
        vaccines.add("OPV2");
        vaccines.add("OPV3");
        vaccines.add("PCV1");
        vaccines.add("PCV2");
        vaccines.add("PCV3");
        vaccines.add("Penta 1");
        vaccines.add("Penta 2");
        vaccines.add("Penta 3");
        vaccines.add("Penta 4");
        vaccines.add("Penta 5");
        vaccines.add("Measles 1");
        vaccines.add("Measles 2");
        fragmentcontainer.addView(wd.createWeightWidget(inflater,container,weightmap));
//        updateVaccinationViews(fragmentcontainer);
        fragmentcontainer.addView(wd.createImmunizationWidget(inflater,container,vaccineList,true));



    }
    private void updateVaccinationViews(ViewGroup v) {
        if (vaccineGroups == null) {
            vaccineGroups = new ArrayList<>();
            LinearLayout vaccineGroupCanvasLL = new LinearLayout(getActivity());
            vaccineGroupCanvasLL.setOrientation(LinearLayout.VERTICAL);
            v.addView(vaccineGroupCanvasLL,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            String supportedVaccinesString = readAssetContents(VACCINES_FILE);
            try {
                JSONArray supportedVaccines = new JSONArray(supportedVaccinesString);
                for (int i = 0; i < supportedVaccines.length(); i++) {
                    VaccineGroup curGroup = new VaccineGroup(getActivity());
                    curGroup.setData(supportedVaccines.getJSONObject(i), childDetails, vaccineList);
                    curGroup.setOnRecordAllClickListener(new VaccineGroup.OnRecordAllClickListener() {
                        @Override
                        public void onClick(VaccineGroup vaccineGroup, ArrayList<VaccineWrapper> dueVaccines) {
//                            addVaccinationDialogFragment(dueVaccines, vaccineGroup);
                        }
                    });
                    curGroup.setOnVaccineClickedListener(new VaccineGroup.OnVaccineClickedListener() {
                        @Override
                        public void onClick(VaccineGroup vaccineGroup, VaccineWrapper vaccine) {
//                            addVaccinationDialogFragment(Arrays.asList(vaccine), vaccineGroup);
                        }
                    });
                    curGroup.setOnVaccineUndoClickListener(new VaccineGroup.OnVaccineUndoClickListener() {
                        @Override
                        public void onUndoClick(VaccineGroup vaccineGroup, VaccineWrapper vaccine) {
//                            addVaccineUndoDialogFragment(vaccineGroup, vaccine);
                        }
                    });
                    vaccineGroupCanvasLL.addView(curGroup);
                    vaccineGroups.add(curGroup);
                }
            } catch (JSONException e) {
//                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
    }
    private String readAssetContents(String path) {
        String fileContents = null;
        try {
            InputStream is = getActivity().getAssets().open(path);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            fileContents = new String(buffer, "UTF-8");
        } catch (IOException ex) {
//            android.util.Log.e(TAG, ex.toString(), ex);
        }

        return fileContents;
    }


    @Override
    public void onVaccinateToday(List<VaccineWrapper> tags, View view) {

    }

    @Override
    public void onVaccinateEarlier(List<VaccineWrapper> tags, View view) {

    }

    @Override
    public void onUndoVaccination(VaccineWrapper tag, View view) {

    }
}
