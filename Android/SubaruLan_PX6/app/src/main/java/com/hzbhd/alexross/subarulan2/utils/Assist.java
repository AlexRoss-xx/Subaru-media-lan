
/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2.utils;
import android.app.Activity;
import android.content.Context;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.hzbhd.alexross.subarulan2.BR;
import com.hzbhd.alexross.subarulan2.MyApplication;
import com.hzbhd.alexross.subarulan2.R;
import com.hzbhd.alexross.subarulan2.SoundSettingsBindComponent;

public class Assist extends Toast {
    private static final String TAG = Assist.class.getSimpleName();
    public Assist(Context context) {
        super(context);
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.assist_info,null);

        ViewDataBinding trip = DataBindingUtil.bind(view, new SoundSettingsBindComponent());
        trip.setVariable(com.hzbhd.alexross.subarulan2.BR.canModel, MyApplication.Companion.getStModel().getCanModel());
        trip.setVariable(com.hzbhd.alexross.subarulan2.BR.stateModel, MyApplication.Companion.getStModel());

        ViewDataBinding tripInfo = DataBindingUtil.bind(view.findViewById(R.id.tripinfo_layout), new SoundSettingsBindComponent());
        tripInfo.setVariable(BR.canModel, MyApplication.Companion.getStModel().getCanModel());

        ViewDataBinding stateMode = DataBindingUtil.bind(view.findViewById(R.id.toast_info_mode__layout), new SoundSettingsBindComponent());
        stateMode.setVariable(BR.stateModel, MyApplication.Companion.getStModel());

        setGravity(Gravity.FILL_HORIZONTAL | Gravity.FILL_VERTICAL, 0, 0);

        setView(view);
        setDuration(Toast.LENGTH_LONG);
    }

    public Assist(Context context, Activity activity, String message,
                       boolean flag) {
        super(context);

        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.assist_info,null);

        setGravity(Gravity.FILL_HORIZONTAL | Gravity.FILL_VERTICAL, 0, 0);
        setView(view);
        setDuration(Toast.LENGTH_LONG);
    }

}