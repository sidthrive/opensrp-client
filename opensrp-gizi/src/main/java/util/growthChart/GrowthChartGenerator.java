package util.growthChart;

import android.graphics.Color;

import com.jjoe64.graphview.GraphView;
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

    private double [][]graphLine;
    private String xValue;
    private String yValue;

    public GrowthChartGenerator(GraphView graph,String gender,String xValue,String yValue){
        this.graph = graph;
        this.xValue = xValue;
        this.yValue = yValue;
        graphLine = gender.toLowerCase().contains("em") ? GraphConstant.girlsChart : GraphConstant.boyChart;
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


}
