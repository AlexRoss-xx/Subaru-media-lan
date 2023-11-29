/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2;

import com.hzbhd.alexross.subarulan2.adapters.ProgressBindingAdapter;
import com.hzbhd.alexross.subarulan2.adapters.StyleBindingAdapter;

public class SoundSettingsBindComponent implements androidx.databinding.DataBindingComponent {

    public ProgressBindingAdapter getProgressBindingAdapter() {
        return new ProgressBindingAdapter();
    }

    public StyleBindingAdapter getStyleBindingAdapter() {
        return new StyleBindingAdapter();
    }
}
