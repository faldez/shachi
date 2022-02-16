package com.faldez.shachi.ui.saved

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.faldez.shachi.R
import com.faldez.shachi.database.AppDatabase
import com.faldez.shachi.databinding.SavedFragmentBinding
import com.faldez.shachi.repository.FavoriteRepository
import com.faldez.shachi.repository.PostRepository
import com.faldez.shachi.repository.SavedSearchRepository
import com.faldez.shachi.service.BooruService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.MaterialShapeDrawable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class SavedFragment : Fragment() {
    private val viewModel: SavedViewModel by navGraphViewModels(R.id.saved) {
        val db = AppDatabase.build(requireContext())
        val service = BooruService()
        SavedViewModelFactory(SavedSearchRepository(db),
            PostRepository(service), FavoriteRepository(db))
    }
    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
    }
    private lateinit var binding: SavedFragmentBinding
    private lateinit var adapter: SavedSearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        Log.d("SavedFragment", "onCreateView")
        binding = SavedFragmentBinding.inflate(inflater, container, false)

        val quality = preferences.getString("preview_quality", null) ?: "preview"
        val questionableFilter =
            preferences.getString("filter_questionable_content", null) ?: "disable"
        val explicitFilter = preferences.getString("filter_explicit_content", null) ?: "disable"

        adapter = SavedSearchAdapter(
            onBrowse = {
                val bundle = bundleOf("title" to it.savedSearch.savedSearchTitle,
                    "server" to it.server,
                    "tags" to it.savedSearch.tags)
                findNavController()
                    .navigate(R.id.action_saved_to_browse,
                        bundle)
            },
            onClick = { savedSearchServer, position ->
                val bundle =
                    bundleOf("saved_search_id" to savedSearchServer.savedSearch.savedSearchId,
                        "position" to position,
                        "server" to savedSearchServer.server,
                        "tags" to savedSearchServer.savedSearch.tags)
                findNavController()
                    .navigate(R.id.action_saved_to_postslide,
                        bundle)
            },
            onDelete = { savedSearch ->
                MaterialAlertDialogBuilder(requireContext()).setTitle("Delete " + savedSearch.savedSearch.savedSearchTitle)
                    .setMessage("Are you Sure?")
                    .setPositiveButton("Yes"
                    ) { _, _ -> viewModel.delete(savedSearch.savedSearch) }
                    .setNegativeButton("No", null).show()
            },
            quality = quality,
            questionableFilter = questionableFilter,
            explicitFilter = explicitFilter,
        )

        binding.savedSearchRecyclerView.adapter = adapter

        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.savedSearchRecyclerView.layoutManager = layoutManager

        val divider = DividerItemDecoration(binding.savedSearchRecyclerView.context,
            layoutManager.orientation)
        binding.savedSearchRecyclerView.addItemDecoration(divider)

        binding.savedSwipeRefreshLayout.setOnRefreshListener {
            viewModel.accept(UiAction.GetSavedSearch)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.state.collectLatest { state ->
                    adapter.submitData(state)
                    binding.savedSwipeRefreshLayout.isRefreshing = false
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.savedAppbarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())
        binding.savedTopappbar.menu.clear()
    }
}