package org.ei.opensrp.gizi.gizi;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;

import org.ei.opensrp.Context;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.gizi.R;

import util.ZScore.ZScoreSystemCalculation;
import util.growthChart.GraphConstant;
import util.growthChart.GrowthChartGenerator;

/**
 * Created by Null on 2016-12-06.
 */
public class GiziZScoreChartActivity extends Activity{

    public static CommonPersonObjectClient client;
    private ZScoreSystemCalculation calc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = Context.getInstance();
        calc = new ZScoreSystemCalculation();
        setContentView(R.layout.gizi_z_score_activity);

        String seriesAxis = "";
        String [] temp = buildDayAgeArray(client.getDetails().get("history_umur"), client.getDetails().get("history_umur_hari")).split(",");
        seriesAxis = temp[0].equals("") ? "" : ""+(Integer.parseInt(temp[0])/30);
        for(int i=1;i<temp.length;i++){
            seriesAxis = seriesAxis + "," + (Integer.parseInt(temp[i])/30);
        }

        String seriesData = this.createWFASeries();

        System.out.println("series axis = "+seriesAxis);
        System.out.println("series data = "+seriesData);

        detailActivity = (TextView)findViewById(R.id.chart_navbar_details);
        back = (ImageButton)findViewById(R.id.btn_back_to_home);
        lfaActivity = (TextView)findViewById(R.id.chart_navbar_growth_chart);

        zScoreGraph = (GraphView)findViewById(R.id.z_score_chart);

        new GrowthChartGenerator(zScoreGraph, GraphConstant.Z_SCORE_CHART,
                client.getDetails().get("tanggalLahir"),
                client.getDetails().get("jenisKelamin"),
                seriesAxis,seriesData
        );

        zScoreGraph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX)
                    return super.formatLabel(value, isValueX) + " Bulan";
                else
                    return super.formatLabel(value, isValueX) + "";
            }
        });

        detailActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                ChildDetailActivity.childclient = client;
                startActivity(new Intent(GiziZScoreChartActivity.this, ChildDetailActivity.class));
                overridePendingTransition(0, 0);
            }
        });

        lfaActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                GiziGrowthChartActivity.client = client;
                startActivity(new Intent(GiziZScoreChartActivity.this, GiziGrowthChartActivity.class));
                overridePendingTransition(0, 0);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(GiziZScoreChartActivity.this, GiziSmartRegisterActivity.class));
                overridePendingTransition(0, 0);
            }
        });
    }

    private String createWFASeries(){
        String []dayAge = buildDayAgeArray(client.getDetails().get("history_umur"),client.getDetails().get("history_umur_hari")).split(",");
        String []weight = client.getDetails().get("history_berat").split(",");
//
        boolean isMale = !client.getDetails().get("jenisKelamin").toLowerCase().contains("em");
        String wfa = "";
//        String hfa = "";
//        String wfh = "";

        int ageLength = dayAge.length;

        for(int i=0;i<ageLength;i++){
            if(i>0)
                wfa = wfa + ",";
            wfa = wfa + calc.countWFA(isMale,Integer.parseInt(dayAge[i]),Double.parseDouble(weight[i+1]));
        }

        return wfa;
    }

    private String buildDayAgeArray(String hu,String huh){
        if(hu==null)
            return "";
        System.out.println("hu = "+hu);
        System.out.println("huh = " + huh);
        String [] huhLength = huh==null ? new String[1] : huh.split(",");
        String [] huLength = hu.split(",");
        String result = "";

        if(huhLength.length<huLength.length) {
            // step 1.  initializing sum of data that recorded before the history_umur_hari.
            int[] age = new int[(huLength.length)-huhLength.length];
//            String[] temp = client.getDetails().get("history_umur").split(",");

            // step 2.  copying month age data
            for (int i = 0; i < age.length; i++) {
                age[i] = Integer.parseInt(huLength[i+1]);
            }

            // step 3.  fix the duplicate value on series
            if (age[0] == age[1])
                age[1]++;
            for (int i = 2; i < age.length - 1; i++) {
                if (age[i - 1] == age[i]) {
                    if (age[i - 1] - age[i - 2] == 2)
                        age[i - 1]--;
                    else
                        age[i]++;
                }
            }

            // step 4.  convert month age to daily unit and transform it into string
            result = Integer.toString(age[0] * 30);
            for(int i=1;i<age.length;i++){
                age[i]*=30;
                result = result + "," + Integer.toString(age[i]);
            }
        }
        if(huh!=null) {
            result = result.length() > 0 && !huhLength[0].equals("") ? result + "," + huhLength[1] : "";
            for (int i = 2; i < huhLength.length; i++) {
                result = result + "," + huhLength[i];
            }
        }
        return result;
    }

    private String [] splitHeightHistory(String htu, String htuh){
        String [] historyNew = htu.split(",");
        String [] historyOld = htuh.split(",");
        String tempOldHistory = "";
        tempOldHistory = historyOld.length > 1 ? historyOld[1].split(":")[0]:"";
        for(int i=2;i<historyOld.length;i++){
            tempOldHistory = tempOldHistory + "," + historyOld[i].split(":")[0];
        }

        String tempNewHistory = historyNew.length > 1 ? historyNew[1].split(":")[0]:"";
        for(int i=2;i<historyNew.length;i++){
            tempNewHistory = tempNewHistory + "," + historyNew[i].split(":")[0];
        }
//        tempOldHistory = tempOldHistory +
//                (!tempOldHistory.equals("") && historyNew.length>1
//                    ? "," + historyNew[1].split(":")[0]
//                    : historyNew.length>1
//                        ? historyNew[1].split(":")[0]
//                        : ""
//                );
//        for(int i=2;i<historyNew.length;i++){
//            tempOldHistory = tempOldHistory + "," + historyNew[i].split(":")[0];
//        }

        String tempHeight = "";
        for(int i=1;i<historyOld.length;i++){
            tempHeight = tempHeight + "," + historyOld[i].split(":")[1];
        }

        return new String[]{buildDayAgeArray(tempOldHistory,tempNewHistory),tempHeight};
    }

    private TextView detailActivity;
    private ImageButton back;
    private TextView lfaActivity;

    private GraphView zScoreGraph;

}
