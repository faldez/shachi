package com.faldez.shachi.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.faldez.shachi.R
import com.faldez.shachi.data.preference.ShachiPreference

class AboutSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.about_preferences, rootKey)
        findPreference<Preference>(ShachiPreference.KEY_APP_VERSION)?.apply {
            summary =
                "v" + requireContext().packageManager.getPackageInfo(requireContext().packageName,
                    0).versionName
        }

        findPreference<Preference>(ShachiPreference.KEY_GITHUB)?.setOnPreferenceClickListener {
            val intent =
                Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/faldez/shachi"))
            startActivity(intent)
            true
        }

        findPreference<Preference>(ShachiPreference.KEY_PRIVACY_POLICY)?.setOnPreferenceClickListener {
            val intent =
                Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://raw.githubusercontent.com/faldez/shachi/master/PRIVACY.md"))
            startActivity(intent)
            true
        }
    }
}