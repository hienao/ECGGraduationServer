package cn.edu.tjut.ecg.ecgserver.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.edu.tjut.ecg.ecgserver.R;
import cn.edu.tjut.ecg.ecgserver.utils.GetWaveDataFromFile;
import cn.edu.tjut.ecg.ecgserver.utils.MyJson;
import cn.edu.tjut.ecg.ecgserver.utils.PreferenceUtils;
import cn.edu.tjut.ecg.ecgserver.utils.WaveUtils;
import cn.edu.tjut.ecg.ecgserver.view.MyMarkerView;

public class ViewWaveActivity extends Activity implements OnChartGestureListener, OnChartValueSelectedListener {

    @Bind(R.id.chart)
    LineChart mChart;
    private float[] dataFiltered=null;
    private float[] rPeak = null;
    private PreferenceUtils preferenceUtils;
    private long time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_wave);
        ButterKnife.bind(this);
        //设置页面全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        preferenceUtils=new PreferenceUtils();
        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawGridBackground(false);
        // no description text
        mChart.setDescription("");

        // enable touch gestures
        mChart.setTouchEnabled(true);
        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);

        // set the marker to the chart
        mChart.setMarkerView(mv);
        /**获取intent传来的json波形信息并解析**/
        Intent intent=getIntent();
        String wavedatagson= intent.getStringExtra("wavedatagson");
        time=intent.getLongExtra("wavedatatime",0);
        dataFiltered= MyJson.json2FloatArray(wavedatagson);
        float orginWaveDataMax= WaveUtils.getWaveDataMax(dataFiltered);
        float orginWaveDataMin=WaveUtils.getWaveDataMin(dataFiltered);
        if ((orginWaveDataMax-orginWaveDataMin)<200)
            dataFiltered = WaveUtils.waveDataEdited(dataFiltered);
        GetWaveDataFromFile getWaveDataFromFile=new GetWaveDataFromFile(this);
        rPeak=getWaveDataFromFile.getDataRPeak(dataFiltered);
        /**获取intent传来的json波形信息并解析**/
        // x-axis limit line
        LimitLine llXAxis = new LimitLine(10f, "Index 10");
        llXAxis.setLineWidth(4f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);

        XAxis xAxis = mChart.getXAxis();


        mChart.getAxisRight().setEnabled(false);


        // add data
        setData(dataFiltered);


        mChart.animateX(2500, Easing.EasingOption.EaseInOutQuart);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(Legend.LegendForm.LINE);

        // // dont forget to refresh the drawing
        // mChart.invalidate();
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {

    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {

    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }
    public void setData(float[] dataFiltered) {
        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < dataFiltered.length; i++) {
            xVals.add((i) + "");
        }

        ArrayList<Entry> yVals = new ArrayList<Entry>();
        ArrayList<Entry> rVals = new ArrayList<Entry>();
        for (int i = 0; i < dataFiltered.length; i++) {
            float val = dataFiltered[i];
            for (int j = 0; j < rPeak.length; j++) {
                if (i == (int) rPeak[j]) {
                    rVals.add(new Entry(val, i));
                    j = rPeak.length + 1;
                }

            }
            yVals.add(new Entry(val, i));
        }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yVals, "心电数据");
        LineDataSet set2 = new LineDataSet(rVals, "R点数据");
        // set1.setFillAlpha(110);
        // set1.setFillColor(Color.RED);
        set1.setColor(Color.RED);
        set1.setCircleColor(Color.BLACK);
        set1.setLineWidth(1f);
        set1.setCircleRadius(3f);
        set1.setDrawCircleHole(false);
        set1.setValueTextSize(9f);
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_red);
        set1.setFillDrawable(drawable);
        set1.setDrawFilled(false);
        set1.setDrawValues(!set1.isDrawValuesEnabled());
        set1.setDrawCircles(false);

        set2.setColor(Color.TRANSPARENT);
        set2.setCircleColor(Color.GREEN);
        set2.setLineWidth(1f);
        set2.setCircleRadius(3f);
        set2.setDrawCircleHole(false);
        set2.setValueTextSize(9f);
        Drawable drawable1 = ContextCompat.getDrawable(this, R.drawable.fade_red);
        set2.setFillDrawable(drawable1);
        set2.setDrawValues(!set1.isDrawValuesEnabled());
        set2.setDrawFilled(false);
        set2.setDrawCircles(true);

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1); // add the datasets
        dataSets.add(set2);
        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

        // set data
        mChart.setData(data);
    }
}
