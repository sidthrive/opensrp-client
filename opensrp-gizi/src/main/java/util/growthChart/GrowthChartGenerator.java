package util.growthChart;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class GrowthChartGenerator {

    GraphView graph;

    LineGraphSeries<DataPoint> series1 = new LineGraphSeries<DataPoint>();
    LineGraphSeries<DataPoint> series2 = new LineGraphSeries<DataPoint>();
    LineGraphSeries<DataPoint> series3 = new LineGraphSeries<DataPoint>();
    LineGraphSeries<DataPoint> series4 = new LineGraphSeries<DataPoint>();
    LineGraphSeries<DataPoint> series5 = new LineGraphSeries<DataPoint>();
    LineGraphSeries<DataPoint> series6 = new LineGraphSeries<DataPoint>();
    LineGraphSeries<DataPoint> series7 = new LineGraphSeries<DataPoint>();
    LineGraphSeries<DataPoint> seriesMain = new LineGraphSeries<DataPoint>();

    private String xValue="0,1,2,3,4,5,6,7,8,9,10,11,12,13";
    private String yValue="3.3,4.4,5.4,6.4,7.0,7.7,8.5,9.1,9.6,10.1,10.8,11.5,12.1,12.6";

    public GrowthChartGenerator(GraphView graph,String xValue,String yValue){
        this.graph = graph;
        this.xValue = xValue;
        this.yValue = yValue;

        System.out.println();

        buildGraphTemplate();
    }

    private void buildGraphTemplate() {
        int red = Color.rgb(255,0,0);
        int yellow = Color.rgb(255,255,0);
        int green = Color.rgb(0,255,0);

        initSeries(series1, Color.argb(255,215,215,215), red, 5);
        initSeries(series2, Color.argb(192, 255, 255, 0), yellow, 5);
        initSeries(series3, Color.argb(128, 128, 255, 128), green, 5);
        initSeries(series4, Color.argb(30, 0, 135, 0), green, 5);
        initSeries(series5, Color.argb(30, 0, 40, 0), green, 5);
        initSeries(series6, Color.argb(128, 0, 255, 0), green, 5);
        initSeries(series7, Color.argb(128, 255, 255, 0), yellow, 5);

        initSeries(seriesMain,Color.argb(0,0,0,0),Color.BLUE,3,"weight",true);

        for(int i=0;i<graphLine.length;i++){
            series1.appendData(new DataPoint(i,graphLine[i][0]),false,70);
            series2.appendData(new DataPoint(i,graphLine[i][1]),false,70);
            series3.appendData(new DataPoint(i,graphLine[i][2]),false,70);
            series4.appendData(new DataPoint(i,graphLine[i][3]),false,70);
            series5.appendData(new DataPoint(i,graphLine[i][4]),false,70);
            series6.appendData(new DataPoint(i,graphLine[i][5]),false,70);
            series7.appendData(new DataPoint(i,graphLine[i][6]),false,70);
        }

        graph.setBackgroundColor(Color.rgb(215, 215, 215));

        graph.getViewport().setScrollable(true);
        graph.getViewport().setScalable(true);


        graph.addSeries(series7);
        graph.addSeries(series6);
        graph.addSeries(series5);
        graph.addSeries(series4);
        graph.addSeries(series3);
        graph.addSeries(series2);
        graph.addSeries(series1);

        graph.addSeries(createDataSeries(xValue.split(","),yValue.split(",")));

    }

    private void initSeries(LineGraphSeries<DataPoint> series, int backGround, int color, int thick,String title, boolean putStroke){
        series.setTitle(title);
        this.initSeries(series,backGround,color,thick);
        if(putStroke){
            series.setDrawDataPoints(true);
            series.setDataPointsRadius(5);
        }
    }

    private void initSeries(LineGraphSeries<DataPoint> series, int backGround, int color, int thick){
        series.setDrawBackground(true);
        series.setBackgroundColor(backGround);
        series.setColor(color);
        series.setThickness(thick);
    }


    private LineGraphSeries<DataPoint>createDataSeries(String []age,String []weight){
        LineGraphSeries<DataPoint>series=new LineGraphSeries<>();
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(7);
        for(int i=0;i<age.length;i++){
            series.appendData(new DataPoint(Double.parseDouble(age[i]), Double.parseDouble(weight[i])), false, 70);
        }
        return series;
    }

    private LineGraphSeries<DataPoint>[]createDataSeries(String dateOfBirth, String date,String weight){
        int counter = 0;
        int[]dateInt = calculateAgesFrom(dateOfBirth, date.split(","));
        String []weightDouble = weight.split(",");
        LineGraphSeries<DataPoint>[]series = new LineGraphSeries[countAgeSeries(dateInt)];
        for(int i=1;i<dateInt.length-1;i++){
            if(dateInt[i]-dateInt[i-1]>1)
                counter++;
            series[counter].appendData(new DataPoint(dateInt[i],Double.parseDouble(weightDouble[i])),false,70);
        }
        return series;
    }

    private int[]calculateAgesFrom(String dateOfBirth,String []data){
        int[]result=new int[data.length];
        for (int i=0;i<data.length;i++) {
            result[i] = getMonthAge(dateOfBirth,data[i]);
        }
        return result;
    }

    private int getMonthAge(String start,String end){
        if(start.length()<7 || end.length()<7)
            return 0;
        return ((Integer.parseInt(end.substring(0,4)) - Integer.parseInt(start.substring(0,4)))*12 +
                (Integer.parseInt(end.substring(5,7)) - Integer.parseInt(start.substring(5,7))));
    }

    private int countAgeSeries(int[]data){
        int counter=data[0]==0? 0:1;
        for(int i=0;i<data.length-1;i++){
            if(data[i+1]-data[i]>1)
                counter++;
        }
        return counter;
    }

    public final double [][]graphLine={
            {2.1,2.5,2.9,3.3,3.9,4.4,5},
            {2.9,3.4,3.9,4.5,5.1,5.8,6.6},
            {3.8,4.3,4.9,5.6,6.3,7.1,8},
            {4.4,5,5.7,6.4,7.2,8,9},
            {4.9,5.6,6.2,7,7.8,8.7,9.7},
            {5.3,6,6.7,7.5,8.4,9.3,10.4},
            {5.7,6.4,7.1,7.9,8.8,9.8,10.9},
            {5.9,6.7,7.4,8.3,9.2,10.3,11.4},
            {6.2,6.9,7.7,8.6,9.6,10.7,11.9},
            {6.4,7.1,8,8.9,9.9,11,12.3},
            {6.6,7.4,8.2,9.2,10.2,11.4,12.7},
            {6.8,7.6,8.4,9.4,10.5,11.7,13},
            {6.9,7.7,8.6,9.6,10.8,12,13.3},
            {7.1,7.9,8.8,9.9,11,12.3,13.7},
            {7.2,8.1,9,10.1,11.3,12.6,14},
            {7.4,8.3,9.2,10.3,11.5,12.8,14.3},
            {7.5,8.4,9.4,10.5,11.7,13.1,14.6},
            {7.7,8.6,9.6,10.7,12,13.4,14.9},
            {7.8,8.8,9.8,10.9,12.2,13.7,15.3},
            {8,8.9,10,11.1,12.5,13.9,15.6},
            {8.1,9.1,10.1,11.3,12.7,14.2,15.9},
            {8.2,9.2,10.3,11.5,12.9,14.5,16.2},
            {8.4,9.4,10.5,11.8,13.2,14.7,16.5},
            {8.5,9.5,10.7,12,13.4,15,16.8},
            {8.6,9.7,10.8,12.2,13.6,15.3,17.1},
            {8.8,9.8,11,12.4,13.9,15.5,17.5},
            {8.9,10,11.2,12.5,14.1,15.8,17.8},
            {9,10.1,11.3,12.7,14.3,16.1,18.1},
            {9.1,10.2,11.5,12.9,14.5,16.3,18.4},
            {9.2,10.4,11.7,13.1,14.8,16.6,18.7},
            {9.4,10.5,11.8,13.3,15,16.9,19},
            {9.5,10.7,12,13.5,15.2,17.1,19.3},
            {9.6,10.8,12.1,13.7,15.4,17.4,19.6},
            {9.7,10.9,12.3,13.8,15.6,17.6,19.9},
            {9.8,11,12.4,14,15.8,17.8,20.2},
            {9.9,11.2,12.6,14.2,16,18.1,20.4},
            {10,11.3,12.7,14.3,16.2,18.3,20.7},
            {10.1,11.4,12.9,14.5,16.4,18.6,21},
            {10.2,11.5,13,14.7,16.6,18.8,21.3},
            {10.3,11.6,13.1,14.8,16.8,19,21.6},
            {10.4,11.8,13.3,15,17,19.3,21.9},
            {10.5,11.9,13.4,15.2,17.2,19.5,22.1},
            {10.6,12,13.6,15.3,17.4,19.7,22.4},
            {10.7,12.1,13.7,15.5,17.6,20,22.7},
            {10.8,12.2,13.8,15.7,17.8,20.2,23},
            {10.9,12.4,14,15.8,18,20.5,23.3},
            {11,12.5,14.1,16,18.2,20.7,23.6},
            {11.1,12.6,14.3,16.2,18.4,20.9,23.9},
            {11.2,12.7,14.4,16.3,18.6,21.2,24.2},
            {11.3,12.8,14.5,16.5,18.8,21.4,24.5},
            {11.4,12.9,14.7,16.7,19,21.7,24.8},
            {11.5,13.1,14.8,16.8,19.2,21.9,25.1},
            {11.6,13.2,15,17,19.4,22.2,25.4},
            {11.7,13.3,15.1,17.2,19.6,22.4,25.7},
            {11.8,13.4,15.2,17.3,19.8,22.7,26},
            {11.9,13.5,15.4,17.5,20,22.9,26.3},
            {12,13.6,15.5,17.7,20.2,23.2,26.6},
            {12.1,13.7,15.6,17.8,20.4,23.4,26.9},
            {12.2,13.8,15.8,18,20.6,23.7,27.2},
            {12.3,14,15.9,18.2,20.8,23.9,27.6},
            {12.4,14.1,16,18.3,21,24.2,27.9}

    };
}
