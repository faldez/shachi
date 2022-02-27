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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.faldez.shachi.MainActivity
import com.faldez.shachi.R
import com.faldez.shachi.database.AppDatabase
import com.faldez.shachi.databinding.SavedFragmentBinding
import com.faldez.shachi.model.SavedSearchServer
import com.faldez.shachi.repository.FavoriteRepository
import com.faldez.shachi.repository.PostRepository
import com.faldez.shachi.repository.SavedSearchRepository
import com.faldez.shachi.service.BooruService
import com.faldez.shachi.widget.CustomDividerItemDecoration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class SavedFragment : Fragment() {
    private val viewModel: SavedViewModel by navGraphViewModels(R.id.saved) {
        val db = AppDatabase.build(requireContext())
        val service = BooruService()
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

        override fun onDelete(savedSearchServer: SavedSearchServer) {
            MaterialAlertDialogBuilder(requireContext()).setTitle("Delete " + savedSearchServer.savedSearch.savedSearchTitle)
                .setMessage("Are you Sure?")
                .setPositiveButton("Yes"
                ) { _, _ -> viewModel.delete(savedSearchServer.savedSearch) }
                .setNegativeButton("No", null).show()
        }

        override fun onEdit(savedSearchServer: SavedSearchServer) {
            val dialog =
                MaterialAlertDialogBuilder(requireContext()).setView(R.layout.saved_search_title_dialog_fragment)
                    .setTitle(resources.getString(R.string.update_title))
                    .setMessage(resources.getString(R.string.saved_search_update_description_title_text))
                    .setPositiveButton(resources.getText(R.string.save)) { dialog, which ->
                        (dialog as Dialog).findViewById<TextInputEditText>(R.id.savedSearchTitleInput).text?.toString()
                            ?.let { title ->
                                viewModel.saveSearch(savedSearchServer.savedSearch.copy(
                                    savedSearchTitle = title))
                                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_LONG)
                                    .show()
                            }
                    }.show()
            dialog.findViewById<EditText>(R.id.savedSearchTitleInput)?.text =
                SpannableStringBuilder(savedSearchServer.savedSearch.savedSearchTitle)
        }

        override fun onScroll(position: Int, scroll: Int) {
            Log.d("SavedFragment", "position=$position scroll=$scroll")
            viewModel.putScroll(position, scroll)

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

        if (!resources.getBoolean(R.bool.isTablet)) {
            binding.savedAppbarLayout.statusBarForeground =
                MaterialShapeDrawable.createWithElevationOverlay(requireContext())
        }

        val gridMode = preferences.getString("grid_mode", null) ?: "staggered"
        val quality = preferences.getString("preview_quality", null) ?: "preview"
        val questionableFilter =
            preferences.getString("filter_questionable_content", null) ?: "disable"
        val explicitFilter = preferences.getString("filter_explicit_content", null) ?: "disable"

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

        val hideBottomBarOnScroll = preferences.getBoolean("hide_bottom_bar_on_scroll", true)
        if (hideBottomBarOnScroll) {
            binding.savedSearchRecyclerView.addOnScrollListener(object :
                RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy < 0) {
                        (activity as MainActivity).showNavigation()
                    } else if (dy > 0) {
                        (activity as MainActivity).hideNavigation()
                    }
                }
            })
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.savedSearchRecyclerView.addItemDecoration(CustomDividerItemDecoration(
                requireContext(),
                layoutManager.orientation,
                if (hideBottomBarOnScroll) systemBars.bottom else 0))
            insets
        }

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