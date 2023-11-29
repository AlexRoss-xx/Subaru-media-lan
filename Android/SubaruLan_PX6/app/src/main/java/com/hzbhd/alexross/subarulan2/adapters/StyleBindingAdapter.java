/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2.adapters;

import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;
import androidx.paging.DataSource;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.hzbhd.alexross.subarulan2.R;
import com.hzbhd.alexross.subarulan2.models.FuelConsumption;
import com.hzbhd.alexross.subarulan2.models.fcData;

import java.util.ArrayList;
import java.util.List;

public class StyleBindingAdapter {


    @BindingAdapter("style")
    public void setStyle(TextView view, int station) {
        if (view.getText().equals("" + station))
            view.setTextAppearance(view.getContext(), R.style.station_selected);
        else
            view.setTextAppearance(view.getContext(), R.style.stations_number);
    }

    @BindingAdapter("style")
    public void setStyle(TextView view, boolean isSet) {
        if (!isSet)
            view.setTextAppearance(view.getContext(), R.style.station_selected);
        else
            view.setTextAppearance(view.getContext(), R.style.stations_number);
    }

    @BindingAdapter({"bind:text_style"})
    public void setRangeStyle(TextView view, int range) {
        if (range < 100 && range >= 50)
            view.setTextAppearance(view.getContext(), R.style.orangeColor);
        else if (range < 50)
            view.setTextAppearance(view.getContext(), R.style.redColor);
        else
            view.setTextAppearance(view.getContext(), R.style.mainTextColor);
    }

    @BindingAdapter({"bind:text_style_eng_temp", "bind:text_style_eng_temp_warning"})
    public void set_Engine_Temp_Style(TextView view, int temp, int hitemp) {
        if (temp >= hitemp)
            view.setTextAppearance(view.getContext(), R.style.redColor);
        else
            view.setTextAppearance(view.getContext(), R.style.mainTextColor);
    }

    @BindingAdapter({"bind:cd_style", "bind:cd_active"})
    public void setCD_Style(TextView view, boolean cd_style, boolean cd_active) {
        if (!cd_style)
            view.setTextAppearance(view.getContext(), R.style.emptycd);
        else
            view.setTextAppearance(view.getContext(), R.style.loadedcd);

        if (cd_active)
            view.setTextAppearance(view.getContext(), R.style.playingcd);
    }


    @BindingAdapter({"bind:modestyle", "bind:mode"})
    public void setStyle(ImageView view, int id, int pid) {
        if (id == pid) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.INVISIBLE);
        }
    }

    @BindingAdapter({"bind:setChartData", "bind:chartType"})
    public void setChartData(LineChart mChart, List<fcData> records, int chartType) {
        ArrayList<Entry> values = new ArrayList<>();

        int index = 0;
        float sum_fc = 0;

        for (fcData data : records) {
            float fuel_consumption = data.getFuel_consumption();
            values.add(new Entry(index++, fuel_consumption));
            sum_fc += fuel_consumption;
        }

        float avg = sum_fc / index;
        ArrayList<Entry> values2 = new ArrayList<>();
        values2.add(new Entry(index > 1 ? 1 : 0, avg));
        values2.add(new Entry(index > 2 ? index - 1 : index, avg));
        values2.add(new Entry(index, avg));
        LineDataSet set2;

        LineDataSet set1;
        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) mChart.getData().getDataSetByIndex(0);
            set1.setValues(values);

            set2 = (LineDataSet) mChart.getData().getDataSetByIndex(1);
            set2.setValues(values2);

            if(chartType==0)
                set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            else  if(chartType==1)
                set1.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
            else
                set1.setMode(LineDataSet.Mode.STEPPED);

            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
            mChart.invalidate();
        } else {
            mChart.setTouchEnabled(false);
            set1 = new LineDataSet(values, "Sample Data");
            set2 = new LineDataSet(values2, "Sample Data");
            set2.setDrawIcons(false);
            set2.setLineWidth(1f);
            set2.setColor(ContextCompat.getColor(mChart.getContext(), R.color.orange_light));
            set2.setCircleRadius(1f);
            set2.setDrawCircleHole(false);
            set2.setValueTextSize(28f);
            set2.setHighlightEnabled(false);
            set2.setValueTextColor(Color.RED);

            ArrayList<Integer> ls = new ArrayList<Integer>();
            ls.add(Color.TRANSPARENT);
            ls.add(Color.RED);
            ls.add(Color.TRANSPARENT);

            set2.setValueTextColors(ls);

            set1.setDrawIcons(false);
            set1.setColor(Color.DKGRAY);
            set1.setCircleColor(Color.DKGRAY);

            if(chartType==0)
                set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            else   if(chartType==1)
                set1.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
            else
                set1.setMode(LineDataSet.Mode.STEPPED);

            set1.setLineWidth(1f);
            set1.setCircleRadius(1f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setValueTextColor(Color.WHITE);
            set1.setDrawValues(false);
            set1.setDrawFilled(true);

            Drawable drw = ContextCompat.getDrawable(mChart.getContext(), R.drawable.gradient);
            set1.setDrawFilled(true);
            set1.setFillDrawable(drw);
            set1.setHighlightEnabled(false);
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();

            dataSets.add(set1);
            dataSets.add(set2);

            LineData data = new LineData(dataSets);
            mChart.setData(data);
            mChart.getLegend().setEnabled(false);
            mChart.getAxisLeft().setTextColor(Color.WHITE);
            mChart.getAxisRight().setTextColor(Color.CYAN);
            mChart.setScaleEnabled(false);
            mChart.getXAxis().setGridColor(ContextCompat.getColor(mChart.getContext(), R.color.md_grey_800));
            mChart.getAxisRight().setGridColor(ContextCompat.getColor(mChart.getContext(), R.color.md_grey_800));

            YAxis leftAxis = mChart.getAxisLeft();
            leftAxis.setDrawAxisLine(false);
            leftAxis.setGranularityEnabled(true);
            leftAxis.setGranularity(1f);
            leftAxis.setAxisMinimum(0f);
            leftAxis.setTextSize(12f);
            leftAxis.setDrawLabels(false);

            XAxis xAxis = mChart.getXAxis();
            xAxis.setLabelCount(8, false);

            xAxis.setGranularityEnabled(true);
            xAxis.setGranularity(1f);
            mChart.setMaxVisibleValueCount(10000);
            mChart.getAxisRight().setDrawAxisLine(false);
            mChart.getAxisRight().setGranularity(1f);
            mChart.getAxisRight().setAxisMinimum(0f);
            mChart.getAxisRight().setTextSize(12f);
        }
    }
}

