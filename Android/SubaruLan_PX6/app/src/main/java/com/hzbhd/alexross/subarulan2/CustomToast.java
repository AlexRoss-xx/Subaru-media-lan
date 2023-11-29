/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2;

import android.app.Activity;
import android.content.Context;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

public class CustomToast extends Toast {
    public CustomToast(Context context) {
        super(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.toast_info, null);
        ViewDataBinding volumeBind = DataBindingUtil.bind(view.findViewById(R.id.volume), new SoundSettingsBindComponent());
        volumeBind.setVariable(BR.soundModel, MyApplication.Companion.getStModel().getSoundSettings());

        ViewDataBinding trip = DataBindingUtil.bind(view.findViewById(R.id.tripinfo_layout), new SoundSettingsBindComponent());
        trip.setVariable(BR.canModel, MyApplication.Companion.getStModel().getCanModel());

        ViewDataBinding stateMode = DataBindingUtil.bind(view.findViewById(R.id.toast_info_mode__layout), new SoundSettingsBindComponent());
        stateMode.setVariable(BR.stateModel, MyApplication.Companion.getStModel());

        setGravity(Gravity.FILL_HORIZONTAL | Gravity.FILL_VERTICAL, 0, 0);
        setView(view);
        setDuration(Toast.LENGTH_SHORT);
    }

    public CustomToast(Context context, Activity activity, String message, boolean flag) {
        super(context);

        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.toast_info,null);
        ViewDataBinding volumeBind = DataBindingUtil.bind(view.findViewById(R.id.volume), new SoundSettingsBindComponent());
        volumeBind.setVariable(BR.soundModel, MyApplication.Companion.getStModel().getSoundSettings());

        setGravity(Gravity.FILL_HORIZONTAL | Gravity.FILL_VERTICAL, 0, 0);
        setView(view);
        setDuration(Toast.LENGTH_LONG);
    }

}
