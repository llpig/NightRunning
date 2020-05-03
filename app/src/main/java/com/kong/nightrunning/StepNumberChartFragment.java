package com.kong.nightrunning;

import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

public class StepNumberChartFragment extends Fragment {

    private CombinedChart stepNumberChart;
    public CombinedChartManager manager;
    private Tool tool;
    private static String BARNAMESTEP = "步数(步数)", LINENAMECAL = "卡路里(百卡)", LINENAMEMILEAGE = "距离(米)";
    private int barColorStep = Color.rgb(128, 64, 255);
    private int lineColorCAl = Color.rgb(255, 128, 64);
    private int lineColorMileage = Color.rgb(64, 255, 128);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_step_number_chart, container, false);
        tool = new Tool();
        stepNumberChart = view.findViewById(R.id.stepNumberChart);
        manager = new CombinedChartManager(stepNumberChart);
        List<String> xAxisValues = manager.getXAxisValues(manager.getBarYAxisValues().size());
        manager.showCombinedChart(xAxisValues);
        return view;
    }

    class CombinedChartManager {

        private CombinedChart mCombinedChart;
        private YAxis leftAxis;
        private YAxis rightAxis;
        private XAxis xAxis;

        public CombinedChartManager(CombinedChart combinedChart) {
            this.mCombinedChart = combinedChart;
            leftAxis = mCombinedChart.getAxisLeft();
            rightAxis = mCombinedChart.getAxisRight();
            xAxis = mCombinedChart.getXAxis();
        }

        /**
         * 初始化Chart
         */
        private void initChart() {
            //不显示描述内容
            mCombinedChart.getDescription().setEnabled(false);

            mCombinedChart.setDrawOrder(new CombinedChart.DrawOrder[]{
                    CombinedChart.DrawOrder.BAR,
                    CombinedChart.DrawOrder.LINE
            });

            mCombinedChart.setBackgroundColor(Color.WHITE);
            mCombinedChart.setDrawGridBackground(false);
            mCombinedChart.setDrawBarShadow(false);
            mCombinedChart.setHighlightFullBarEnabled(false);
            //显示边界
            mCombinedChart.setDrawBorders(false);
            //图例说明
            Legend legend = mCombinedChart.getLegend();
            legend.setWordWrapEnabled(true);

            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            legend.setDrawInside(false);
            //Y轴设置
            leftAxis.setDrawGridLines(false);
            leftAxis.setAxisMinimum(0f);
            leftAxis.setDrawLabels(false);
            leftAxis.setEnabled(false);

            rightAxis.setDrawGridLines(false);
            rightAxis.setDrawLabels(false);
            rightAxis.setAxisMinimum(0f);
            rightAxis.setEnabled(false);

            mCombinedChart.animateX(0); // 立即执行的动画,x轴
        }

        /**
         * 设置X轴坐标值
         *
         * @param xAxisValues x轴坐标集合
         */
        public void setXAxis(final List<String> xAxisValues) {

            //设置X轴在底部
            final XAxis xAxis = mCombinedChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);

            xAxis.setLabelCount(xAxisValues.size() - 1, false);
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return xAxisValues.get((int) value);
                }
            });
            mCombinedChart.invalidate();
        }

        /**
         * 得到折线图(多条)
         */
        private LineData getLineData(List<List<Float>> lineChartYList, List<String> lineNameList, List<Integer> lineColorList) {
            LineData lineData = new LineData();
            int index = 0;
            for (List<Float> lineChartY : lineChartYList) {

                ArrayList<Entry> yValue = new ArrayList<>();
                String lineName = lineNameList.get(index);
                int lineColor = lineColorList.get(index++);
                for (int i = 0; i < lineChartY.size(); i++) {
                    yValue.add(new Entry(i, lineChartY.get(i)));
                }
                LineDataSet dataSet = new LineDataSet(yValue, lineName);

                dataSet.setColor(lineColor);
                //设置圆的半径
                dataSet.setCircleRadius(5f);
                dataSet.setCircleColor(lineColor);
                dataSet.setValueTextColor(lineColor);
                dataSet.setLineWidth(3f);

                //显示值
                dataSet.setDrawValues(true);
                dataSet.setValueTextSize(15f);
                dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                lineData.addDataSet(dataSet);
            }
            return lineData;
        }


        /**
         * 得到柱状图
         *
         * @param barChartY Y轴值
         * @param barName   柱状图名字
         * @param barColor  柱状图颜色
         * @return
         */

        private BarData getBarData(List<Float> barChartY, String barName, int barColor) {
            BarData barData = new BarData();
            ArrayList<BarEntry> yValues = new ArrayList<>();
            for (int i = 0; i < barChartY.size(); i++) {
                yValues.add(new BarEntry(i, barChartY.get(i)));
            }

            BarDataSet barDataSet = new BarDataSet(yValues, barName);
            barDataSet.setColor(barColor);
            barDataSet.setValueTextSize(15f);
            barDataSet.setValueTextColor(barColor);
            barDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            barData.addDataSet(barDataSet);

            //以下是为了解决 柱状图 左右两边只显示了一半的问题 根据实际情况 而定
            xAxis.setAxisMinimum(-0.5f);
            xAxis.setAxisMaximum((float) (barChartY.size() - 0.5));
            return barData;
        }

        /**
         * 显示混合图(柱状图+折线图)
         *
         * @param xAxisValues X轴坐标
         */

        public void showCombinedChart(List<String> xAxisValues) {
            initChart();
            setXAxis(xAxisValues);
            mCombinedChart.setData(getCombinedData());
            mCombinedChart.invalidate();
        }

        public List<String> getXAxisValues(int num) {
            List<String> xAxisData = new ArrayList<String>();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd");
            Calendar calendar = null;
            for (int i = num - 1; i >= 0; --i) {
                calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, -i);
                xAxisData.add(simpleDateFormat.format(calendar.getTime()));
            }
            return xAxisData;
        }

        public List<Float> getBarYAxisValues() {
            NightRunningDatabase helper = MainActivity.getDatabaseHelper();
            List<Float> barYAxisValues = helper.selectRecentTimeStepNumber(helper.getReadableDatabase(), MainActivity.USERNAME, "date('now','localtime','-6 days')");
            if (barYAxisValues.size() != 0) {
                barYAxisValues.remove(barYAxisValues.size() - 1);
            }
            barYAxisValues.add(new Float(NightRunningSensorEventListener.getTodayAddStepNumber()));
            return barYAxisValues;
        }

        public List<Float> getLineYAxisCalValues(List<Float> barYAxisValues) {
            List<Float> lineYAXisValues = new ArrayList<Float>();
            for (int i = 0; i < barYAxisValues.size(); ++i) {
                double calories = tool.getCalories(barYAxisValues.get(i), MainActivity.USERWEIGHT, MainActivity.USERHEIGHT, MainActivity.USERAGE,
                        MainActivity.USERSEX);
                lineYAXisValues.add(new Float(calories));
            }
            return lineYAXisValues;
        }

        public List<Float> getLineYAxisMileageValues(List<Float> barYAxisValues) {
            List<Float> lineYAXisValues = new ArrayList<Float>();
            for (int i = 0; i < barYAxisValues.size(); ++i) {
                double mileage = tool.getMileage(barYAxisValues.get(i), MainActivity.USERHEIGHT, MainActivity.USERAGE, MainActivity.USERSEX);
                lineYAXisValues.add(new Float(mileage));
            }
            return lineYAXisValues;
        }

        public void drawCombinedChart() {
            mCombinedChart.setData(getCombinedData());
            mCombinedChart.invalidate();
        }

        private CombinedData getCombinedData() {
            CombinedData combinedData = new CombinedData();
            List<Float> barYAxisValues = getBarYAxisValues();
            List<List<Float>> lineYAxisValuesList = new ArrayList<>();
            lineYAxisValuesList.add(getLineYAxisCalValues(barYAxisValues));
            lineYAxisValuesList.add(getLineYAxisMileageValues(barYAxisValues));
            List<String> lineNameList = new ArrayList<>();
            lineNameList.add(LINENAMECAL);
            lineNameList.add(LINENAMEMILEAGE);
            List<Integer> lineColorList = new ArrayList<>();
            lineColorList.add(lineColorCAl);
            lineColorList.add(lineColorMileage);
            combinedData.setData(getBarData(barYAxisValues, BARNAMESTEP, barColorStep));
            combinedData.setData(getLineData(lineYAxisValuesList, lineNameList, lineColorList));
            return combinedData;
        }

    }

}
