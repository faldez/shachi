package com.faldez.shachi.ui.more

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.faldez.shachi.R
import com.faldez.shachi.data.database.AppDatabase
import com.faldez.shachi.data.model.*
import com.faldez.shachi.data.repository.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime

class MoreSettingsFragment : PreferenceFragmentCompat() {
    private val backupPathPicker =
        registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
            if (uri != null) {
                requireContext().contentResolver.takePersistableUriPermission(uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
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
    }

    private suspend fun restore(uri: Uri) {
        val gson = Gson()
        val data = requireContext().contentResolver.openInputStream(uri)?.use { input ->
            gson.fromJson(input.reader(), Backup::class.java)
        } ?: return


        val db = AppDatabase.build(requireContext())

        // restore server
        val serverRepository = ServerRepository(db)
        val serverIdMap: MutableMap<Int, Int> = mutableMapOf()
        data.servers?.forEach { server ->
            serverIdMap[server.serverId] =
                serverRepository.getServerByUrl(server.url)?.serverId ?: serverRepository.insert(
                    server.copy(serverId = 0)).toInt()
        }

        //restore blacklist tag
        val blacklistTagRepository = BlacklistTagRepository(db)
        data.blacklistedTags?.forEach {
            blacklistTagRepository.insertBlacklistedTag(it)
        }

        // restore blacklist crossref
        data.blacklistedTagsCrossRef?.let {
            val list =
                it.mapNotNull { ref ->
                    serverIdMap[ref.serverId]?.let { serverId ->
                        ref.copy(serverId = serverId)
                    }
                }
            blacklistTagRepository.insertBlacklistedTag(list)
        }

        // restore saved search
        val savedSearchRepository = SavedSearchRepository(db)
        data.savedSearches?.forEach {
            serverIdMap[it.serverId]?.let { serverId ->
                savedSearchRepository.insert(tags = it.tags,
                    title = it.savedSearchTitle,
                    serverId = serverId)
            }
        }

        // restore favorites
        val favoriteRepository = FavoriteRepository(db)
        data.favorites?.forEach {
            serverIdMap[it.serverId]?.let { serverId ->
                favoriteRepository.insert(it.copy(serverId = serverId))
            }
        }

        // restore search histories
        val searchHistoryRepository = SearchHistoryRepository(db)
        data.searchHistories?.forEach {
            serverIdMap[it.serverId]?.let { serverId ->
                searchHistoryRepository.insert(it.copy(serverId = serverId))
            }
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
            searchHistories = getSearchHistories(db),
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
        backupPathPicker.launch("shachi_backup_${
            ZonedDateTime.now().toEpochSecond()
        }.json")
    }

    private fun restoreDialog(uri: Uri) {
        Log.d("MoreSettingsFragment", "restoreDialog")
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
            requireContext().contentResolver.releasePersistableUriPermission(uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
    }
}