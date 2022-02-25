package com.faldez.shachi.ui.more

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.faldez.shachi.R
import com.faldez.shachi.database.AppDatabase
import com.faldez.shachi.model.*
import com.faldez.shachi.repository.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime

class MoreSettingsFragment : PreferenceFragmentCompat() {

    private val backupPathPicker =
        registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
            if (uri != null) {
                lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            backup(uri)
                        }
                        Toast.makeText(requireContext(), "Backup success", Toast.LENGTH_SHORT)
                            .show()
                    } catch (e: IllegalStateException) {
                        Toast.makeText(requireContext(), "Backup failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Backup cancelled", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    private val restorePathPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            restore(uri)
                        }
                        Toast.makeText(requireContext(), "Restore success", Toast.LENGTH_SHORT)
                            .show()
                    } catch (e: IllegalStateException) {
                        Toast.makeText(requireContext(), "Restore failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
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
            }.json")
            true
        }

        findPreference<Preference>("restore")?.setOnPreferenceClickListener {
            restorePathPicker.launch(arrayOf("*/*"))
            true
        }
    }

    private suspend fun restore(uri: Uri) {
        val gson = Gson()
        val data = requireContext().contentResolver.openInputStream(uri)?.use { input ->
            gson.fromJson(input.reader(), Backup::class.java)
        }

        if (data != null) {
            val db = AppDatabase.build(requireContext())
            restoreServers(db, data)
            restoreBlacklistTags(db, data)
            restoreFavorites(db, data)
            restoreSearchHistories(db, data)
            restoreSavedSearch(db, data)
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

    private suspend fun backup(uri: Uri) {
        val db = AppDatabase.build(requireContext())

        val blacklistedTags = getBlacklistedTags(db)
        val blacklistedTagsCrossRef = blacklistedTags?.flatMap { blacklistedTag ->
            blacklistedTag.servers.map { server ->
                ServerBlacklistedTagCrossRef(serverId = server.serverId,
                    blacklistedTagId = blacklistedTag.blacklistedTag.blacklistedTagId)
            }
        }
        val data = Backup(
            servers = getServers(db),
            blacklistedTags = blacklistedTags?.map { it.blacklistedTag },
            blacklistedTagsCrossRef = blacklistedTagsCrossRef,
            savedSearches = getSavedSearches(db),
            favorites = getFavorites(db),
            searchHistories = getSearchHistories(db)
        )

        val gson = Gson()
        requireContext().contentResolver.openOutputStream(uri)?.use { output ->
            val json = gson.toJson(data)
            output.write(json.toByteArray())

            Log.d("MoreSettingsFragment/backup", "writted to $uri")
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
}