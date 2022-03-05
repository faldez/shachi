package com.faldez.shachi.ui.settings

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.faldez.shachi.MainActivity
import com.faldez.shachi.R
import com.faldez.shachi.data.preference.ShachiPreference

class RootSettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {


    private val downloadPathPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            if (uri != null) {
                requireContext().contentResolver.takePersistableUriPermission(uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                PreferenceManager.getDefaultSharedPreferences(requireContext()).edit()?.apply {
                    putString(ShachiPreference.KEY_DOWNLOAD_PATH, uri.toString())
                    commit()
                }
            }
        }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        findPreference<Preference>(ShachiPreference.KEY_DOWNLOAD_PATH)?.setOnPreferenceClickListener {
            downloadPathPicker.launch(MediaStore.Images.Media.getContentUri("external"))
            true

        }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)

        findPreference<Preference>(ShachiPreference.KEY_DOWNLOAD_PATH)?.apply {
            summary =
                sharedPreferences?.getString(ShachiPreference.KEY_DOWNLOAD_PATH, null)
                    ?.let { Uri.parse(it) }?.lastPathSegment?.substringAfter(":")
                    ?: resources.getString(R.string.not_set_message)
        }
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            ShachiPreference.KEY_THEME -> (activity as MainActivity).setTheme()
            ShachiPreference.KEY_SEND_CRASH_REPORTS -> (activity as MainActivity).setSendCrashReports()
        }
    }
}