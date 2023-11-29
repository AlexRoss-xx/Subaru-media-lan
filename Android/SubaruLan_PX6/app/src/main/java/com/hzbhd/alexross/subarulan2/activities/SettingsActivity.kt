/*
 *  2023.
 * Alexey Rasskazov
 */

/*
 *  Created by Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2.activities

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.preference.RingtonePreference
import android.text.TextUtils
import android.view.MenuItem
import android.content.SharedPreferences
import com.hzbhd.alexross.subarulan2.*
import kotlin.math.floor


class SettingsActivity : AppCompatPreferenceActivity() {
    private var current: String = "";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
    }

    override fun onResume() {
        super.onResume()
        MyApplication.activityResumed()
    }

    override fun onPause() {
        super.onPause()
        MyApplication.activityPaused()
        // EventBus.getDefault().unregister(this);
        // this.unregisterReceiver(testReceiver);
    }

    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onIsMultiPane(): Boolean {
        return isXLargeTablet(this)
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onBuildHeaders(target: List<PreferenceActivity.Header>) {
        loadHeadersFromResource(R.xml.pref_headers, target)
    }

    override fun isValidFragment(fragmentName: String): Boolean {
        current = fragmentName;
        return PreferenceFragment::class.java.name == fragmentName
                || GeneralPreferenceFragment::class.java.name == fragmentName
                || ServicePreferenceFragment::class.java.name == fragmentName
                || DataSyncPreferenceFragment::class.java.name == fragmentName
                || NotificationPreferenceFragment::class.java.name == fragmentName
                || AboutPreferenceFragment::class.java.name == fragmentName
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {

            // activity.onBackPressed();
            if (id == android.R.id.home && current == "") {
                super.onBackPressed()
            }
        }
        current = ""
        return super.onOptionsItemSelected(item)
    }

    fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == "CAN_LOGGING") {
            // Set summary to be the user-description for the selected value
            MyApplication.canLogging = sharedPreferences.getBoolean(key, false)
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class GeneralPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_general)
            setHasOptionsMenu(true)

        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                activity.onBackPressed();
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class ServicePreferenceFragment : PreferenceFragment()    {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_service)
            setHasOptionsMenu(true)

            val h = floor(MyApplication.repository.vehicle().motorHours/60f).toInt()
            val m = floor(MyApplication.repository.vehicle().motorHours%60f).toInt()
            findPreference("motor_hrs").title = "Motor hours from last reset: " +h +" h: "+m+" min"

            findPreference("sample_key").setOnPreferenceClickListener {
                findPreference("motor_hrs").title = "Motor hours from last reset: " +0 +" h: "+0+" min"
                MyApplication.repository.clear()
                MyApplication.stModel.canModel.motorhours=0;
                MyApplication.stModel.canModel.motorhoursStr=""
                MyApplication.stModel.canModel.maintenance=false;
                MyApplication.stModel.canModel.notifyPropertyChanged(BR.records)
                false

            };
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                activity.onBackPressed();
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class NotificationPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_notification)
            setHasOptionsMenu(true)
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"))
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                //  startActivity(Intent(activity, SettingsActivity::class.java))
                activity.onBackPressed();
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class DataSyncPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_data_sync)
            setHasOptionsMenu(true)
            bindPreferenceSummaryToValue(findPreference("sync_frequency"))
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                // startActivity(Intent(activity, SettingsActivity::class.java))
                activity.onBackPressed();
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class AboutPreferenceFragment : PreferenceFragment() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_about)
            setHasOptionsMenu(true)

            findPreference("device_info").title = "ID: " + ApplicationConfig.deviceSerialNumber + "__"+ ApplicationConfig.device
            findPreference("registered_to").title = "Registered to: " + ApplicationConfig.registeredTo

            findPreference("mcu_version").title = "MCU version: " + ApplicationConfig.mcuVersion
            findPreference("apk_version").title = "APK version: " + BuildConfig.VERSION_CODE+ " - " +BuildConfig.VERSION_NAME
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                // startActivity(Intent(activity, SettingsActivity::class.java))
                activity.onBackPressed();
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    companion object {

        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private val sBindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->
            val stringValue = value.toString()

            if (preference is ListPreference) {
                val listPreference = preference
                val index = listPreference.findIndexOfValue(stringValue)

                preference.setSummary(
                        if (index >= 0)
                            listPreference.entries[index]
                        else
                            null)

            } else if (preference is RingtonePreference) {
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent)

                } else {
                    val ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue))

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null)
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        val name = ringtone.getTitle(preference.getContext())
                        preference.setSummary(name)
                    }
                }

            } else {
                preference.summary = stringValue
            }
            true
        }

        /**
         * Helper method to determine if the device has an extra-large screen. For
         * example, 10" tablets are extra-large.
         */
        private fun isXLargeTablet(context: Context): Boolean {
            return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE
        }

        /**
         * Binds a preference's summary to its value. More specifically, when the
         * preference's value is changed, its summary (line of text below the
         * preference title) is updated to reflect the value. The summary is also
         * immediately updated upon calling this method. The exact display format is
         * dependent on the type of preference.

         * @see .sBindPreferenceSummaryToValueListener
         */
        private fun bindPreferenceSummaryToValue(preference: Preference) {
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.context)
                            .getString(preference.key, ""))
        }
    }
}
