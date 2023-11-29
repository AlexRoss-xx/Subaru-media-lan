/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;

import java.util.List;

public class AppSelectorPreference extends ListPreference {
    CharSequence[] entries ;
    CharSequence[] entryValues ;

    public AppSelectorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        PackageManager pm = context.getPackageManager();
        final List<ApplicationInfo> installedApplications = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        //  List<PackageInfo> appListInfo = pm.getInstalledPackages(0);
         entries = new CharSequence[installedApplications.size()];
          entryValues = new CharSequence[installedApplications.size()];

        try {
            int i = 0;
            for (ApplicationInfo p : installedApplications) {
                if (!p.packageName.contentEquals("com.hzbhd.alexross.subarulan2")) {
                    entries[i] = pm.getApplicationLabel(p);
                    entryValues[i] = p.packageName.toString();
                    Log.d("AppSelectorPreference", "Label: " + entries[i]);
                    Log.d("AppSelectorPreference", "PName: " + entryValues[i]);
                    i++;
                }
            }
        } catch (Exception e) {
            Log.e("AppSelectorPreference", "ER> Put descriptive error message here");
            e.printStackTrace();
        }

        setEntries(entries);
        setEntryValues(entryValues);
    }

    public int findIndexOfValue(String value) {
        if(value == null)
            return 0;
        int i = 0;
        for (CharSequence p : entryValues) {
            if (p.toString().contentEquals(value)) {
                return i;
            }
            i++;
        }

        return 0;
    }
}
