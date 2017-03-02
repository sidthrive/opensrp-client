package org.ei.opensrp.path.viewComponents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import org.ei.opensrp.path.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



/**
 * Created by raihan on 2/26/17.
 */
public class WidgetFactory {

    public View createTableRow(LayoutInflater inflater, ViewGroup container, String labelString, String valueString){
        View rows = inflater.inflate(R.layout.tablerows, container, false);
        TextView label = (TextView)rows.findViewById(R.id.label);
        TextView value = (TextView)rows.findViewById(R.id.value);

        label.setText(labelString);
        value.setText(valueString);
        return rows;
    }
    public View createWeightWidget(LayoutInflater inflater, ViewGroup container, HashMap<String,String> last_five_weight_map){
        View weightwidget = inflater.inflate(R.layout.weightwidget, container, false);
        LinearLayout tableLayout = (LinearLayout) weightwidget.findViewById(R.id.weightvalues);
        for (Map.Entry<String, String> entry : last_five_weight_map.entrySet())
        {
            tableLayout.addView(createTableRow(inflater,tableLayout,""+entry.getKey(),""+entry.getValue()));
        }

        return weightwidget;
    }
    public View createImmunizationWidget(LayoutInflater inflater, ViewGroup container, ArrayList<String> vaccines,boolean editmode){

        View immunization_widget = inflater.inflate(R.layout.immunization_widget, container, false);
        TableLayout immunizationholder = (TableLayout)immunization_widget.findViewById(R.id.immunizationholder);
        for(int i = 0 ;i<vaccines.size();i++){
            immunizationholder.addView(createImmunizationRow(inflater,container,vaccines.get(i),editmode));
        }

        return immunization_widget;
    }
    public View createImmunizationRow(LayoutInflater inflater, ViewGroup container, String vaccines,boolean editmode){

        View vaccineRow = inflater.inflate(R.layout.vaccinate_row_view, container, false);
//        TableLayout tableLayout = (TableLayout)vaccineRow.findViewById(R.id.weightvalues);
        TextView vaccinename = (TextView)vaccineRow.findViewById(R.id.vaccine);
        Button undobutton = (Button)vaccineRow.findViewById(R.id.undo);
        if(editmode) {
            undobutton.setVisibility(View.VISIBLE);
        }else{
            undobutton.setVisibility(View.GONE);
        }
        vaccinename.setText(vaccines);

        return vaccineRow;
    }
}
