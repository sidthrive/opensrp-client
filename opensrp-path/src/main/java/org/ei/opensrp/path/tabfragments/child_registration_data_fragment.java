package org.ei.opensrp.path.tabfragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import org.ei.opensrp.path.R;
import org.ei.opensrp.path.viewComponents.WidgetFactory;


public class child_registration_data_fragment extends Fragment {

    public child_registration_data_fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentview = inflater.inflate(R.layout.child_registration_data_fragment, container, false);
        TableLayout layout = (TableLayout)fragmentview.findViewById(R.id.rowholder);
        WidgetFactory wd = new WidgetFactory();

        layout.addView(wd.createTableRow(inflater,container,"Catchment Area","Linda"));
        layout.addView(wd.createTableRow(inflater,container,"ZEIR ID","5425428"));
        layout.addView(wd.createTableRow(inflater,container,"Child Register Card Number",""));
        layout.addView(wd.createTableRow(inflater,container,"Birth Certificate Number",""));
        layout.addView(wd.createTableRow(inflater,container,"First Name","Joyce"));
        layout.addView(wd.createTableRow(inflater,container,"Last Name","Mwansa"));
        layout.addView(wd.createTableRow(inflater,container,"Sex","Female"));
        layout.addView(wd.createTableRow(inflater,container,"DOB",""));
        layout.addView(wd.createTableRow(inflater,container,"Date First Seen",""));
        layout.addView(wd.createTableRow(inflater,container,"Birth Weight",""));
        layout.addView(wd.createTableRow(inflater,container,"Mother/Guardian Name",""));
        layout.addView(wd.createTableRow(inflater,container,"Mother/Guardian NRC",""));
//        layout.addView(createTableRow(inflater,container,"Catchment Area","Linda"));
//        layout.addView(createTableRow(inflater,container,"Catchment Area","Linda"));
//        layout.addView(createTableRow(inflater,container,"Catchment Area","Linda"));
//        layout.addView(createTableRow(inflater,container,"Catchment Area","Linda"));
//        layout.addView(createTableRow(inflater,container,"Catchment Area","Linda"));
//        layout.addView(createTableRow(inflater,container,"Catchment Area","Linda"));
//        layout.addView(createTableRow(inflater,container,"Catchment Area","Linda"));
//        layout.addView(createTableRow(inflater,container,"Catchment Area","Linda"));
//        layout.addView(createTableRow(inflater,container,"Catchment Area","Linda"));




        // Inflate the layout for this fragment
        return fragmentview;
    }
    public View createTableRow(LayoutInflater inflater, ViewGroup container, String labelString, String valueString){
        View rows = inflater.inflate(R.layout.tablerows, container, false);
        TextView label = (TextView)rows.findViewById(R.id.label);
        TextView value = (TextView)rows.findViewById(R.id.value);

        label.setText(labelString);
        value.setText(valueString);
        return rows;
    }

}
