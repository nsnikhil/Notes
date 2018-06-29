package com.nrs.nsnik.notes.view.fragments


import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.nrs.nsnik.notes.R


class PrefFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.main_prefs)
    }

}
