/*
 *  2023.
 * Alexey Rasskazov
 */

package com.hzbhd.alexross.subarulan2.models

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable

class RSEModel (stateModel: StateModel) : BaseObservable() {
     var soundSettings: SoundSettingsModel = stateModel.soundSettings
}