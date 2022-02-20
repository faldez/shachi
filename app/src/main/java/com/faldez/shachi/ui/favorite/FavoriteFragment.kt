package com.faldez.shachi.ui.favorite

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
import androidx.paging.filter
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.faldez.shachi.MainActivity
import com.faldez.shachi.R
import com.faldez.shachi.database.AppDatabase
import com.faldez.shachi.databinding.FavoriteFragmentBinding
import com.faldez.shachi.model.Rating
import com.faldez.shachi.repository.FavoriteRepository
import com.google.android.material.shape.MaterialShapeDrawable
import kotlinx.coroutines.launch

class FavoriteFragment : Fragment() {
    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
    }

    private lateinit var binding: FavoriteFragmentBinding
    private val viewModel: FavoriteViewModel by navGraphViewModels(R.id.nav_graph) {
        val db = AppDatabase.build(requireContext())
        val favoriteRepository = FavoriteRepository(db)
        FavoriteViewModelFactory(favoriteRepository, this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FavoriteFragmentBinding.inflate(inflater, container, false)

        val tags = arguments?.get("tags") as String? ?: ""
        if (tags != viewModel.state.value.tags) {
            viewModel.accept(UiAction.SearchFavorite(tags))
        }

        binding.bind()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.searchFloatingButton.setOnClickListener {
            val bundle = bundleOf("tags" to viewModel.state.value.tags)
            Log.d("FavoriteFragment/onViewCreated", "tags ${viewModel.state.value.tags}")
            findNavController().navigate(R.id.action_browse_to_search, bundle)
        }
        prepareAppBar()
    }

    private fun prepareAppBar() {
        binding.favoriteAppbarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())
        binding.favoriteTopappbar.menu.clear()
    }

    private fun FavoriteFragmentBinding.bind(
    ) {
        val gridCount = preferences.getString("grid_column", null)?.toInt() ?: 3
        val gridMode = preferences.getString("grid_mode", null) ?: "staggered"
        val quality = preferences.getString("preview_quality", null) ?: "preview"
        val questionableFilter =
            preferences.getString("filter_questionable_content", null) ?: "disable"
        val explicitFilter = preferences.getString("filter_explicit_content", null) ?: "disable"

        val favoriteAdapter = FavoriteAdapter(
            gridMode,
            quality,
            hideQuestionable = questionableFilter == "hide",
            hideExplicit = explicitFilter == "hide",
            onClick = { position ->
                val bundle = bundleOf("position" to position)
                findNavController().navigate(R.id.action_favorite_to_favoritepostslide, bundle)
            }
        )

        favoriteAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        favoriteRecyclerView.adapter = favoriteAdapter

        favoriteRecyclerView.layoutManager = if (gridMode == "staggered") {
            val layoutManager =
                StaggeredGridLayoutManager(gridCount, StaggeredGridLayoutManager.VERTICAL)
            layoutManager.gapStrategy =
                StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
            layoutManager
        } else {
            GridLayoutManager(requireContext(), gridCount)
        }

        favoriteRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy != 0) {
                    Log.d("BrowseFragment", "getPosition ${favoriteAdapter.getPosition()}")
                    viewModel.accept(UiAction.Scroll(currentTags = viewModel.state.value.tags))
                }
            }
        })

        val hideBottomBarOnScroll = preferences.getBoolean("hide_bottom_bar_on_scroll", true)
        if (hideBottomBarOnScroll) {
            favoriteRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy < 0) {
                        (activity as MainActivity).showNavigation(callback = { binding.searchFloatingButton.show() })
                    } else if (dy > 0) {
                        (activity as MainActivity).hideNavigation(callback = { binding.searchFloatingButton.hide() })
                    }
                }
            })
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pagingDataFlow.collect {
                    favoriteAdapter.submitData(it.filter { post ->
                        when (post.rating) {
                            Rating.Questionable -> questionableFilter != "mute"
                            Rating.Explicit -> explicitFilter != "mute"
                            Rating.Safe -> true
                        }
                    })
                }
            }
        }
    }
}