package com.faldez.shachi.ui.more

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.faldez.shachi.R
import com.faldez.shachi.data.database.AppDatabase
import com.faldez.shachi.data.model.Backup
import com.faldez.shachi.data.repository.*
import com.faldez.shachi.service.BooruServiceImpl
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

class MoreSettingsFragment : PreferenceFragmentCompat() {
    private val viewModel: MoreViewModel by viewModels {
        val db = AppDatabase.build(requireContext())
        val serverRepository = ServerRepository(db)
        val blacklistTagRepository = BlacklistTagRepository(db)
        val savedSearchRepository = SavedSearchRepository(db)
        val favoriteRepository = FavoriteRepository(db)
        val searchHistoryRepository = SearchHistoryRepository(db)
        val booruService = BooruServiceImpl()
        val tagRepository = TagRepository(booruService, db)
        MoreViewModelFactory(serverRepository,
            blacklistTagRepository,
            savedSearchRepository,
            favoriteRepository,
            searchHistoryRepository,
            tagRepository, this)
    }

    var dialog: Dialog? = null

    private val backupPathPicker =
        registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
            if (uri != null) {
                viewModel.accept(UiAction.Backup(uri))
            } else {
                Toast.makeText(requireContext(), "Backup cancelled", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    private val restorePathPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            Log.d("MoreSettingsFragment", "uri=$uri")
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
            backupDialog()
            true
        }

        findPreference<Preference>("restore")?.setOnPreferenceClickListener {
            restorePathPicker.launch(arrayOf("application/json"))

            lifecycleScope.launch {
                viewModel.restoreState.collectLatest {
                    Log.d("MoreSettingsFragment", "restoreState=$it")
                    when (it) {
                        RestoreState.Idle -> {}
                        RestoreState.Start -> {
                            dialog =
                                MaterialAlertDialogBuilder(requireContext()).setTitle("Restoring")
                                    .setView(R.layout.loading_dialog).setCancelable(false).show()

                            activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        }
                        is RestoreState.Loaded -> viewModel.restore(it.data)
                        RestoreState.Success -> {
                            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                            dialog?.dismiss()
                            dialog = null
                            Toast.makeText(requireContext(),
                                "Restore success",
                                Toast.LENGTH_SHORT).show()
                        }
                        is RestoreState.Failed -> {
                            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                            dialog?.dismiss()
                            dialog = null
                            Toast.makeText(requireContext(),
                                "Restore error: ${it.reason}",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            true
        }
    }

    private fun restore(uri: Uri) {
        viewModel.restoreState.value = RestoreState.Start

        val gson = Gson()
        val data = requireContext().contentResolver.openInputStream(uri)?.use { input ->
            gson.fromJson(input.reader(), Backup::class.java)
        } ?: return

        viewModel.restoreState.value = RestoreState.Loaded(data)
    }

    private fun backupDialog() {
        backupPathPicker.launch("shachi_backup_${
            ZonedDateTime.now().toEpochSecond()
        }.json")

        lifecycleScope.launch {
            viewModel.backupFlow.collectLatest {
                val dialog = MaterialAlertDialogBuilder(requireContext()).setTitle("Backing up")
                    .setView(R.layout.loading_dialog).show()
                activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                val gson = Gson()
                requireContext().contentResolver.openOutputStream(it.uri)?.use { output ->
                    val json = gson.toJson(it.data)
                    output.write(json.toByteArray())

                    Log.d("MoreSettingsFragment/backup", "written to ${it.uri}")

                    activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    dialog.dismiss()

                    Toast.makeText(requireContext(),
                        "Backup success",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}