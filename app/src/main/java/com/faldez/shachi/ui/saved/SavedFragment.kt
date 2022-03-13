package com.faldez.shachi.ui.saved

import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
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
import com.faldez.shachi.data.database.AppDatabase
import com.faldez.shachi.data.model.SavedSearchServer
import com.faldez.shachi.data.preference.*
import com.faldez.shachi.data.repository.FavoriteRepository
import com.faldez.shachi.data.repository.PostRepository
import com.faldez.shachi.data.repository.SavedSearchRepository
import com.faldez.shachi.databinding.SavedFragmentBinding
import com.faldez.shachi.service.BooruServiceImpl
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class SavedFragment : Fragment() {
    private val viewModel: SavedViewModel by navGraphViewModels(R.id.saved) {
        val db = AppDatabase.build(requireContext())
        val service = BooruServiceImpl()
        SavedViewModelFactory(SavedSearchRepository(db),
            PostRepository(service), FavoriteRepository(db), this)
    }
    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
    }
    private lateinit var binding: SavedFragmentBinding
    private lateinit var savedSearchAdapter: SavedSearchAdapter

    private val adapterListener = object : SavedSearchAdapterListener {
        override fun onBrowse(savedSearchServer: SavedSearchServer) {
            val bundle = bundleOf("title" to savedSearchServer.savedSearch.savedSearchTitle,
                "server" to savedSearchServer.server,
                "tags" to savedSearchServer.savedSearch.tags)
            findNavController()
                .navigate(R.id.action_saved_to_browse,
                    bundle)
        }

        override fun onClick(savedSearchServer: SavedSearchServer, position: Int) {
            val bundle =
                bundleOf("saved_search_id" to savedSearchServer.savedSearch.savedSearchId,
                    "position" to position,
                    "server" to savedSearchServer.server,
                    "tags" to savedSearchServer.savedSearch.tags)
            findNavController()
                .navigate(R.id.action_saved_to_postslide,
                    bundle)
        }

        override fun onRefresh(position: Int) {
            savedSearchAdapter.adapters[position]?.refresh()
        }

        override fun onDelete(position: Int) {
            val savedSearchServer = savedSearchAdapter.currentList[position].savedSearch
            MaterialAlertDialogBuilder(requireContext()).setTitle("Delete " + savedSearchServer.savedSearch.savedSearchTitle)
                .setMessage("Are you Sure?")
                .setPositiveButton("Yes"
                ) { _, _ -> viewModel.delete(savedSearchServer.savedSearch) }
                .setNegativeButton("No", null).show()
        }

        override fun onEdit(position: Int) {
            val savedSearchServer = savedSearchAdapter.currentList[position].savedSearch
            val dialog =
                MaterialAlertDialogBuilder(requireContext()).setView(R.layout.saved_search_title_dialog_fragment)
                    .setTitle(resources.getString(R.string.edit))
                    .setMessage(resources.getString(R.string.saved_search_update_description_title_text))
                    .setPositiveButton(resources.getText(R.string.save)) { dialog, which ->
                        val title =
                            (dialog as Dialog).findViewById<TextInputEditText>(R.id.savedSearchTitleInput).text?.toString()
                        val tags =
                            (dialog as Dialog).findViewById<TextInputEditText>(R.id.savedSearchTagsInput).text?.toString()
                        if (!title.isNullOrEmpty() && !tags.isNullOrEmpty()) {
                            viewModel.saveSearch(savedSearchServer.savedSearch.copy(
                                savedSearchTitle = title, tags = tags))
                            Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Toast.makeText(requireContext(),
                                "Can't save search if selected tags is empty",
                                Toast.LENGTH_SHORT).show()
                        }
                    }.show()
            dialog.findViewById<EditText>(R.id.savedSearchTitleInput)?.text =
                SpannableStringBuilder(savedSearchServer.savedSearch.savedSearchTitle)
            dialog.findViewById<EditText>(R.id.savedSearchTagsInput)?.text =
                SpannableStringBuilder(savedSearchServer.savedSearch.tags)
        }

        override fun onScroll(position: Int, scroll: Int) {
            Log.d("SavedFragment", "position=$position scroll=$scroll")
            viewModel.putScroll(position, scroll)
        }

        override fun onBind(
            adapter: SavedSearchPostAdapter,
            item: SavedSearchPost,
            questionableFilter: Filter,
            explicitFilter: Filter,
        ) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                    item.posts.collectLatest {
                        if (it != null) {
                            adapter.submitData(it)
                        }
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = SavedFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.savedAppbarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())

        val gridMode = preferences.getString(ShachiPreference.KEY_GRID_MODE, null)?.toGridMode()
            ?: GridMode.Staggered
        val quality = preferences.getString(ShachiPreference.KEY_PREVIEW_QUALITY, null)?.toQuality()
            ?: Quality.Preview
        val questionableFilter =
            preferences.getString(ShachiPreference.KEY_FILTER_QUESTIONABLE_CONTENT, null)
                ?.toFilter() ?: Filter.Disable
        val explicitFilter =
            preferences.getString(ShachiPreference.KEY_FILTER_EXPLICIT_CONTENT, null)?.toFilter()
                ?: Filter.Disable

        viewModel.accept(UiAction.SetFilters(questionableFilter, explicitFilter))

        savedSearchAdapter = SavedSearchAdapter(
            listener = adapterListener,
            gridMode = gridMode,
            quality = quality,
            questionableFilter = questionableFilter,
            explicitFilter = explicitFilter,
            scrollPositions = viewModel.scrollState.value
        )

        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.savedSearchRecyclerView.apply {
            swapAdapter(savedSearchAdapter, false)
            setLayoutManager(layoutManager)
            setHasFixedSize(true)
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            savedSearchAdapter.adapters.forEach { (_, adapter) ->
                adapter.refresh()
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }

        binding.savedSearchRecyclerView.addItemDecoration(DividerItemDecoration(
            requireContext(),
            layoutManager.orientation))

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.savedSearchFlow.collectLatest { state ->
                    savedSearchAdapter.submitList(state)
                    binding.savedSearchHelpText.isVisible = state.isEmpty()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.scrollState.collectLatest {
                    savedSearchAdapter.setScrollPositions(it)
                }
            }
        }
    }
}