package com.faldez.shachi.ui.more

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.faldez.shachi.R
import com.faldez.shachi.service.BackupService
import com.faldez.shachi.service.RestoreService
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

class MoreSettingsFragment : PreferenceFragmentCompat() {

    companion object {
        const val TAG = "MoreSettingsFragment"
    }

    var dialog: Dialog? = null

    private val backupPathPicker =
        registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
            if (uri != null) {
                Log.d(TAG, "backup uri=$uri")
                val intent = Intent(requireContext(), BackupService::class.java).apply {
                    putExtras(bundleOf("file_uri" to uri))
                }

                requireContext().startService(intent)
            } else {
                Toast.makeText(requireContext(), "Backup cancelled", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    private val restorePathPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            Log.d(TAG, "uri=$uri")
            if (uri != null) {
                restore(uri)
            } else {
                Toast.makeText(requireContext(), "Restore cancelled", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.more_preferences, rootKey)

        findPreference<Preference>("backup")?.setOnPreferenceClickListener {
            backupPathPicker.launch("shachi_backup_${
                ZonedDateTime.now().toEpochSecond()
            }.proto.gz")

            true
        }

        findPreference<Preference>("restore")?.setOnPreferenceClickListener {
            restorePathPicker.launch(arrayOf("application/gzip"))

            true
        }
    }

    private fun restore(uri: Uri) {
        val intent = Intent(requireContext(), RestoreService::class.java).apply {
            putExtras(bundleOf("file_uri" to uri))
        }

        requireContext().startService(intent)
    }
}