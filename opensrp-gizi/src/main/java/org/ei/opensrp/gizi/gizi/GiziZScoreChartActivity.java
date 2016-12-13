package org.ei.opensrp.gizi.gizi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

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

        // configure nav bar option
        detailActivity = (TextView)findViewById(R.id.chart_navbar_details);
        back = (ImageButton)findViewById(R.id.btn_back_to_home);
        lfaActivity = (TextView)findViewById(R.id.chart_navbar_growth_chart);
        initializeActionNavBar();

        //

        String [] data = initializeZScoreSeries();
//        String seriesAxis = this.createWFAAxis();
//        String seriesData = this.createWFASeries();
        String seriesAxis = data[0];
        String seriesData = data[1];
        System.out.println("series axis = "+seriesAxis);
        System.out.println("series data = "+seriesData);



        zScoreGraph = (GraphView)findViewById(R.id.z_score_chart);

        new GrowthChartGenerator(zScoreGraph, GraphConstant.Z_SCORE_CHART,
                client.getDetails().get("tanggalLahir"),
                client.getDetails().get("jenisKelamin"),
                seriesAxis,seriesData
        );


    }

    private String[]initializeZScoreSeries(){
        String axis1 = createWFAAxis();
        String data1 = createWFASeries();
        String axis2="",data2="";

        String tempAxis2 = createHFAAxis();
        if(!tempAxis2.equals("")) {
            axis2 = tempAxis2.split(",").length > 0 ? Integer.toString(Integer.parseInt(tempAxis2.split(",")[0]) / 30) : "";
            for (int i = 1; i < tempAxis2.split(",").length; i++) {
                axis2 = axis2 + "," + Integer.toString(Integer.parseInt(tempAxis2.split(",")[i]) / 30);
            }
            data2 = createHFASeries();
        }
        String axis3 = createWFHAxis();
        String data3 = createWFHSeries();
        return new String[]{axis1+"@"+axis2+"@"+axis3,data1+"@"+data2+"@"+data3};
    }

    public void initializeActionNavBar(){
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

    private String createWFAAxis(){
        String seriesAxis = "";
        String [] temp = buildDayAgeArray(client.getDetails().get("history_umur"), client.getDetails().get("history_umur_hari")).split(",");
        seriesAxis = temp[0].equals("") ? "" : ""+(Integer.parseInt(temp[0])/30);
        for(int i=1;i<temp.length;i++){
            seriesAxis = seriesAxis + "," + (Integer.parseInt(temp[i])/30);
        }
        return seriesAxis;
    }

    private String createWFASeries(){
        if(client.getDetails().get("history_berat")==null)
            return "";
        String []dayAge = buildDayAgeArray(client.getDetails().get("history_umur"),client.getDetails().get("history_umur_hari")).split(",");
        String[] weight = client.getDetails().get("history_berat").split(",");
        boolean isMale = !client.getDetails().get("jenisKelamin").toLowerCase().contains("em");
        String wfa = "";
        int ageLength = dayAge.length;
        for(int i=0;i<ageLength;i++){
            if(i>0)
                wfa = wfa + ",";
            wfa = wfa + calc.countWFA(isMale,Integer.parseInt(dayAge[i]),Double.parseDouble(weight[i+1]));
        }
        return wfa;
    }

    private String createHFAAxis(){
        if (client.getDetails().get("history_tinggi")==null)
            return "";
        String []historyUmur = client.getDetails().get("history_tinggi").split(",");
        String []historyUmurHari = client.getDetails().get("history_tinggi_umur_hari").split(",");

        String tempUmur = historyUmur.length>1? historyUmur[0].split(":")[0]:"";
        String tempUmurHari = historyUmurHari.length>1?historyUmurHari[0].split(":")[0]:"";

        for(int i=1;i<historyUmur.length;i++){
            tempUmur = tempUmur + "," + historyUmur[i].split(":")[0];
            if(historyUmurHari.length>i)
                tempUmurHari = tempUmurHari+ ","+historyUmurHari[i].split(":")[0];
        }
        return buildDayAgeArray(tempUmur,tempUmurHari);
    }

    private String createHFASeries(){
        String []historyUmur = createHFAAxis().split(",");
        if(historyUmur.length<1 || historyUmur[0].equals(""))
            return "";
        String []temp = client.getDetails().get("history_tinggi").split(",");
        boolean isMale = !client.getDetails().get("jenisKelamin").toLowerCase().contains("em");


        String result = "";
        for(int i=0;i<historyUmur.length;i++){
            if(i>0)
                result = result+",";
            result = result + Double.toString(calc.countHFA(isMale,Integer.parseInt(historyUmur[i]),Double.parseDouble(temp[i+1].split(":")[1])));
        }
        //System.out.println("hfa result = "+result);
        return result;
    }

    private String createWFHAxis(){
        String axis = createHFAAxis();
        if(axis.equals(""))
            return "";
        String result = "";

        for(int i=0;i<axis.split(",").length;i++){
            result = result + "," +Integer.toString(Integer.parseInt(axis.split(",")[i])/30);
        }
        return result.substring(1,result.length());
    }

    private String createWFHSeries(){
        String result = "";
        String uT = createWFHAxis();
        String u = client.getDetails().get("history_umur");
        System.out.println("u = "+u);
        String b = client.getDetails().get("history_berat");
        System.out.println("b = "+b);
        String t= client.getDetails().get("history_tinggi");
        System.out.println("t = "+t);
        if(u==null || uT.equals("") || t==null)
            return "";
        String[]umurTinggi = uT.split(",");
        String[]umur = u.split(",");
        String[]berat = b.split(",");
        String[]tinggi = t.split(",");

        boolean isMale = !client.getDetails().get("jenisKelamin").toLowerCase().contains("em");
        int j=1;
        for(int i=0;i<umurTinggi.length;i++){
            for(;j<umur.length;j++){
                if(umurTinggi[i].equals(umur[j])) {
                    result = result + "," + Double.toString(Integer.parseInt(umurTinggi[i])<24
                            ? calc.countWFL(isMale,Double.parseDouble(berat[j]),Double.parseDouble(tinggi[i].split(":")[1]))
                            : calc.countWFH(isMale, Double.parseDouble(berat[j]), Double.parseDouble(tinggi[i].split(":")[1])));
                    break;
                }
            }
        }

        return result.length()>1 ? result.substring(1,result.length()):"";
    }

    private String buildDayAgeArray(String hu,String huh){
        if(hu==null)
            return "";
        //System.out.println("hu = "+hu);
        //System.out.println("huh = " + huh);
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
            result = result.length() > 0 && !huhLength[0].equals("") && huhLength.length > 1? result + "," + huhLength[1] : "";
            for (int i = 2; i < huhLength.length; i++) {
                result = result + "," + huhLength[i];
            }
        }
        //System.out.println("result = "+result);
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
