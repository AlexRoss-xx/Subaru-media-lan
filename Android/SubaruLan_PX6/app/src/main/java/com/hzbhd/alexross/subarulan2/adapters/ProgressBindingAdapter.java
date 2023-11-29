/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2.adapters;

import androidx.databinding.BindingAdapter;
import androidx.core.content.ContextCompat;

import com.hzbhd.alexross.subarulan2.R;
import com.hzbhd.alexross.subarulan2.controls.seekBar.HorizontalSeekbarWithIntervals;
import com.hzbhd.alexross.subarulan2.controls.seekBar.VerticalSeekbarWithIntervals;

public class ProgressBindingAdapter {

    @BindingAdapter("android:progressValue")
    public void setProgressValue(VerticalSeekbarWithIntervals view, int progress) {
        view.setProgress(progress);
    }

    @BindingAdapter({"bind:modestyle", "bind:mode"})
    public void setStyle(VerticalSeekbarWithIntervals view, int id, int pid) {
        if (id == pid) {
            view.GetSeekBar().barColor= ContextCompat.getColor(view.getContext(), R.color.orange);
            view.GetSeekBar().invalidate();
        }
        else{
            view.GetSeekBar().barColor= ContextCompat.getColor(view.getContext(),R.color.background_main);
            view.GetSeekBar().invalidate();
        }
    }
    @BindingAdapter("android:progressValue")
    public void setProgressValue(HorizontalSeekbarWithIntervals view, int progress) {
        view.setProgress(progress);
    }

    @BindingAdapter({"bind:modestyle", "bind:mode"})
    public void setStyle(HorizontalSeekbarWithIntervals view, int id, int pid) {
        if (id == pid) {
            view.GetSeekBar().barColor= ContextCompat.getColor(view.getContext(),R.color.orange);
            view.GetSeekBar().invalidate();
        }
        else{
            view.GetSeekBar().barColor= ContextCompat.getColor(view.getContext(),R.color.background_main);
            view.GetSeekBar().invalidate();
        }
    }
}
