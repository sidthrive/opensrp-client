package org.ei.opensrp.path.tabfragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.ei.opensrp.path.R;
import org.ei.opensrp.path.viewComponents.WidgetFactory;

import java.util.ArrayList;
import java.util.HashMap;


public class child_under_five_fragment extends Fragment {

    private LayoutInflater inflater;
    private ViewGroup container;
    private LinearLayout fragmentcontainer;

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
        fragmentcontainer.addView(wd.createWeightWidget(inflater,container,weightmap));
        fragmentcontainer.addView(wd.createImmunizationWidget(inflater,container,vaccines,false));


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
        fragmentcontainer.addView(wd.createImmunizationWidget(inflater,container,vaccines,true));
    }

}
