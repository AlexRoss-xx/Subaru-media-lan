/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2;

import android.content.Context;

import android.util.AttributeSet;

public class EditTextPreference extends android.preference.EditTextPreference {

    public EditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
    }

    @Override
    public CharSequence getSummary() {
        return     super.getSummary()+ this.getText();
    }

}
