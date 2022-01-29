package com.faldez.bonito.ui.browse

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.faldez.bonito.MainActivity
import com.faldez.bonito.R
import com.faldez.bonito.data.FavoriteRepository
import com.faldez.bonito.data.PostRepository
import com.faldez.bonito.data.SavedSearchRepository
import com.faldez.bonito.data.ServerRepository
import com.faldez.bonito.database.AppDatabase
import com.faldez.bonito.databinding.BrowseFragmentBinding
import com.faldez.bonito.model.Post
import com.faldez.bonito.model.Server
import com.faldez.bonito.model.Tag
import com.faldez.bonito.service.BooruService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.SnackbarContentLayout
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BrowseFragment : Fragment() {
    companion object {
        const val TAG = "SearchPostFragment"
    }

    private lateinit var binding: BrowseFragmentBinding

    private val viewModel: BrowseViewModel by
    navGraphViewModels(R.id.nav_graph) {
        val server = arguments?.get("server") as Server?
        val tags = arguments?.get("tags") as String?
        val db = AppDatabase.build(requireContext())
        val favoriteRepository = FavoriteRepository(db)
        BrowseViewModelFactory(
            server, tags,
            PostRepository(BooruService()),
            ServerRepository(db),
            favoriteRepository,
            SavedSearchRepository(db),
            this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        Log.d(TAG, "onCreate " + savedInstanceState.toString())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = BrowseFragmentBinding.inflate(inflater, container, false)

        val view = binding.root

        (activity as MainActivity).setSupportActionBar(binding.searchPostTopAppBar)

        binding.appBarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())

        binding.bindState(
            uiState = viewModel.state,
            pagingData = viewModel.pagingDataFlow,
            uiActions = viewModel.accept
        )

        val bottomNavigationView =
            (activity as MainActivity).findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        if (bottomNavigationView.visibility == View.GONE) {
            bottomNavigationView.visibility = View.VISIBLE
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<List<Tag>>("tags")
            ?.observe(viewLifecycleOwner) { result ->
                Log.d("SearchPostFragment", "$result")
                viewModel.accept(UiAction.Search(viewModel.state.value.server?.url,
                    result))
            }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.browse_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search_button -> {
                val bundle = bundleOf("server" to viewModel.state.value.server,
                    "tags" to viewModel.state.value.tags)
                findNavController().navigate(R.id.action_searchpost_to_searchsimple, bundle)
                hideBottomNavigationView()
                return true
            }
            R.id.save_search_button -> {
                if (viewModel.state.value.tags.isNotEmpty()) {
                    viewModel.saveSearch()
                    Snackbar.make(binding.root, "Saved", LENGTH_SHORT).show()
                }
                return true
            }
            R.id.manage_server_button -> {
                findNavController().navigate(R.id.action_searchpost_to_servers)
                hideBottomNavigationView()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun hideBottomNavigationView() {
        (activity as MainActivity).findViewById<BottomNavigationView>(R.id.bottomNavigationView).visibility =
            View.GONE
    }

    private fun BrowseFragmentBinding.bindState(
        uiState: StateFlow<UiState>,
        pagingData: Flow<PagingData<Post>>,
        uiActions: (UiAction) -> Unit,
    ) {
        val postAdapter = BrowserAdapter(
            onClick = { position ->
                val bundle = bundleOf("position" to position)
                findNavController().navigate(R.id.action_global_to_postslide, bundle)
                hideBottomNavigationView()
            }
        )
        postsRecyclerView.adapter = postAdapter

        val layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS

        postsRecyclerView.layoutManager = layoutManager

        bindSearch(
            uiState = uiState,
            onTagsChanged = uiActions
        )

        bindList(
            postsAdapter = postAdapter,
            uiState = uiState,
            pagingData = pagingData,
            onScrollChanged = uiActions
        )
    }

    private fun BrowseFragmentBinding.bindSearch(
        uiState: StateFlow<UiState>,
        onTagsChanged: (UiAction.Search) -> Unit,
    ) {
    }

    private fun BrowseFragmentBinding.bindList(
        postsAdapter: BrowserAdapter,
        uiState: StateFlow<UiState>,
        pagingData: Flow<PagingData<Post>>,
        onScrollChanged: (UiAction.Scroll) -> Unit,
    ) {
        retryButton.setOnClickListener { postsAdapter.retry() }
        postsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy != 0) onScrollChanged(UiAction.Scroll(uiState.value.server?.url,
                    currentTags = uiState.value.tags))
            }
        })

        swipeRefreshLayout.setOnRefreshListener {
            postsAdapter.refresh()
        }

        val notLoading = postsAdapter.loadStateFlow.distinctUntilChangedBy { it.source.refresh }
            .map { it.source.refresh is LoadState.NotLoading }
        val hasNotScrolledForCurrentSearch =
            uiState.map { it.hasNotScrolledForCurrentTag }.distinctUntilChanged()

        val shouldScrollToTop = combine(
            notLoading,
            hasNotScrolledForCurrentSearch,
            Boolean::and
        )

        lifecycleScope.launchWhenCreated {
            pagingData.collectLatest(postsAdapter::submitData)
        }

        lifecycleScope.launch {
            shouldScrollToTop.collect { shouldScroll ->
                Log.d(TAG, "shouldScroll $shouldScroll")
                if (shouldScroll) postsRecyclerView.scrollToPosition(0)
            }
        }

        lifecycleScope.launch {
            postsAdapter.loadStateFlow.collect { loadState ->
                val isListEmpty =
                    loadState.refresh is LoadState.NotLoading && postsAdapter.itemCount == 0
                postsRecyclerView.isVisible = !isListEmpty
                swipeRefreshLayout.isRefreshing = loadState.source.refresh is LoadState.Loading
                retryButton.isVisible =
                    loadState.source.refresh is LoadState.Error && viewModel.state.value.server != null
                binding.serverHelpText.isVisible = viewModel.state.value.server == null
            }
        }
    }
}