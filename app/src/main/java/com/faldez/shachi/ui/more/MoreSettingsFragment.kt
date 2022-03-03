package com.faldez.shachi.ui.more

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.SparseBooleanArray
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.faldez.shachi.R
import com.faldez.shachi.database.AppDatabase
import com.faldez.shachi.model.*
import com.faldez.shachi.repository.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime

class MoreSettingsFragment : PreferenceFragmentCompat() {
    private var selectedItems = SparseBooleanArray()
    private val items =
        listOf("Server", "Blacklist tags", "Saved searches", "Favorites", "Search histories")

    private val backupPathPicker =
        registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
            if (uri != null) {
                requireContext().contentResolver.takePersistableUriPermission(uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            backup(uri, selectedItems)
                        }
                        Toast.makeText(requireContext(), "Backup success", Toast.LENGTH_SHORT)
                            .show()
                    } catch (e: IllegalStateException) {
                        Toast.makeText(requireContext(), "Backup failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                    requireContext().contentResolver.releasePersistableUriPermission(uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }
            } else {
                Toast.makeText(requireContext(), "Backup cancelled", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    private val restorePathPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            Log.d("MoreSettingsFragment", "uri=$uri")
            if (uri != null) {
                requireContext().contentResolver.takePersistableUriPermission(uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                restoreDialog(uri)
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
            true
        }

        findPreference<Preference>("privacy_policy")?.setOnPreferenceClickListener {
            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://raw.githubusercontent.com/faldez/shachi/master/PRIVACY.md"))
            startActivity(intent)
            true
        }
    }

    private suspend fun restore(uri: Uri, restoreItems: SparseBooleanArray) {
        val gson = Gson()
        val data = requireContext().contentResolver.openInputStream(uri)?.use { input ->
            gson.fromJson(input.reader(), Backup::class.java)
        }

        if (data != null) {
            val db = AppDatabase.build(requireContext())
            if (restoreItems[0]) restoreServers(db, data)
            if (restoreItems[1]) restoreBlacklistTags(db, data)
            if (restoreItems[2]) restoreSavedSearch(db, data)
            if (restoreItems[3]) restoreFavorites(db, data)
            if (restoreItems[4]) restoreSearchHistories(db, data)
        }
    }

    private suspend fun restoreServers(db: AppDatabase, data: Backup) {
        val serverRepository = ServerRepository(db)

        data.servers?.forEach { server ->
            serverRepository.insert(Server(
                serverId = server.serverId,
                type = server.type,
                title = server.title,
                url = server.url,
                username = server.username,
                password = server.password
            ))
        }
    }

    private suspend fun restoreBlacklistTags(db: AppDatabase, data: Backup) {
        val blacklistTagRepository = BlacklistTagRepository(db)

        data.blacklistedTags?.forEach {
            blacklistTagRepository.insertBlacklistedTag(it)
        }

        data.blacklistedTagsCrossRef?.let { blacklistTagRepository.insertBlacklistedTag(it) }
    }

    private suspend fun restoreFavorites(db: AppDatabase, data: Backup) {
        val favoriteRepository = FavoriteRepository(db)
        data.favorites?.forEach {
            favoriteRepository.insert(it)
        }
    }

    private suspend fun restoreSearchHistories(db: AppDatabase, data: Backup) {
        val searchHistoryRepository = SearchHistoryRepository(db)
        data.searchHistories?.forEach {
            searchHistoryRepository.insert(it)
        }
    }

    private suspend fun restoreSavedSearch(db: AppDatabase, data: Backup) {
        val savedSearchRepository = SavedSearchRepository(db)
        data.savedSearches?.forEach {
            savedSearchRepository.insert(it)
        }
    }

    private suspend fun backup(uri: Uri, backupItems: SparseBooleanArray) {
        val db = AppDatabase.build(requireContext())

        val blacklistedTags = getBlacklistedTags(db)
        val blacklistedTagsCrossRef = blacklistedTags?.flatMap { blacklistedTag ->
            blacklistedTag.servers.map { server ->
                ServerBlacklistedTagCrossRef(serverId = server.serverId,
                    blacklistedTagId = blacklistedTag.blacklistedTag.blacklistedTagId)
            }
        }

        val data = Backup(
            servers = if (backupItems[0]) getServers(db) else null,
            blacklistedTags = if (backupItems[1]) blacklistedTags?.map { it.blacklistedTag } else null,
            blacklistedTagsCrossRef = if (backupItems[1]) blacklistedTagsCrossRef else null,
            savedSearches = if (backupItems[2]) getSavedSearches(db) else null,
            favorites = if (backupItems[3]) getFavorites(db) else null,
            searchHistories = if (backupItems[4]) getSearchHistories(db) else null,
        )

        val gson = Gson()
        requireContext().contentResolver.openOutputStream(uri)?.use { output ->
            val json = gson.toJson(data)
            output.write(json.toByteArray())

            Log.d("MoreSettingsFragment/backup", "written to $uri")
        }
    }

    private suspend fun getServers(db: AppDatabase): List<Server>? {
        val serverRepository = ServerRepository(db)
        return serverRepository.getAllServers()
    }

    private suspend fun getBlacklistedTags(db: AppDatabase): List<BlacklistedTagWithServer>? {
        val blacklistTagRepository = BlacklistTagRepository(db)
        return blacklistTagRepository.getAll()
    }

    private suspend fun getSavedSearches(db: AppDatabase): List<SavedSearch>? {
        val savedSearchRepository = SavedSearchRepository(db)
        return savedSearchRepository.getAll()
    }

    private suspend fun getFavorites(db: AppDatabase): List<Post>? {
        val favoriteRepository = FavoriteRepository(db)
        return favoriteRepository.getAll()
    }

    private suspend fun getSearchHistories(db: AppDatabase): List<SearchHistory>? {
        val searchHistoryRepository = SearchHistoryRepository(db)
        return searchHistoryRepository.getAll()
    }

    private fun backupDialog() {
        selectItemsDialog("Select items to backup", "Backup", callback = {
            backupPathPicker.launch("shachi_backup_${
                ZonedDateTime.now().toEpochSecond()
            }.json")
        })
    }

    private fun restoreDialog(uri: Uri) {
        Log.d("MoreSettingsFragment", "restoreDialog")
        selectItemsDialog("Select items to restore", "Restore", callback = {
            lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        restore(uri, selectedItems)
                    }
                    Toast.makeText(requireContext(), "Restore success", Toast.LENGTH_SHORT)
                        .show()
                } catch (e: IllegalStateException) {
                    Toast.makeText(requireContext(), "Restore failed", Toast.LENGTH_SHORT)
                        .show()
                }
                requireContext().contentResolver.releasePersistableUriPermission(uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
        })
    }

    private fun selectItemsDialog(title: String, action: String, callback: () -> Unit) {
        Log.d("MoreSettingsFragment", "selectItemsDialog")
        selectedItems = SparseBooleanArray()
        MaterialAlertDialogBuilder(requireContext()).setTitle(title)
            .setMultiChoiceItems(items.toTypedArray(), null) { _, which, isChecked ->
                selectedItems.put(which, isChecked)
            }.setPositiveButton(action) { _, _ ->
                callback()
            }.show()
    }
}