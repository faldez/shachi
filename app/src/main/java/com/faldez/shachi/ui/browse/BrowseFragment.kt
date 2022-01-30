package com.faldez.shachi.ui.browse

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.faldez.shachi.R
import com.faldez.shachi.databinding.BrowseFragmentBinding
import com.faldez.shachi.MainActivity
import com.faldez.shachi.data.FavoriteRepository
import com.faldez.shachi.data.PostRepository
import com.faldez.shachi.data.SavedSearchRepository
import com.faldez.shachi.data.ServerRepository
import com.faldez.shachi.database.AppDatabase
import com.faldez.shachi.model.Post
import com.faldez.shachi.model.Server
import com.faldez.shachi.model.Tag
import com.faldez.shachi.service.BooruService
import com.faldez.shachi.ui.base.BaseBrowseViewModel
import com.faldez.shachi.ui.base.UiAction
import com.faldez.shachi.ui.base.UiState
import com.google.android.material.shape.MaterialShapeDrawable
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BrowseFragment : Fragment() {
    companion object {
        const val TAG = "BrowseFragment"
    }

    private lateinit var binding: BrowseFragmentBinding

    private lateinit var viewModel: BaseBrowseViewModel

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

        prepareAppBar()

        val server = arguments?.get("server") as Server?
        val tags = arguments?.get("tags") as String?
        Log.d("SearchPostFragment", "$server $tags")

        val currentDestinationId = findNavController().currentDestination?.id
        Log.d(TAG, "$currentDestinationId ${R.id.browseServerFragment}")
        when (currentDestinationId) {
            R.id.browseServerFragment -> {
                val vm: BrowseViewModel by navGraphViewModels(R.id.nav_graph) {
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

                viewModel = vm
            }
            R.id.savedSearchBrowseServerFragment -> {
                val vm: SavedSearchBrowseViewModel by navGraphViewModels(R.id.nav_graph) {
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

                viewModel = vm
            }
        }

        viewModel.accept(UiAction.GetSelectedOrSelectServer(server))
        tags?.let {
            viewModel.accept(UiAction.Search(server?.url,
                it.split(" ").map { Tag.fromName(it) }))
        }

        val view = binding.root

        binding.bindState(
            uiState = viewModel.state,
            pagingData = viewModel.pagingDataFlow,
            uiActions = viewModel.accept
        )

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

    override fun onDestroy() {
        super.onDestroy()
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
                findNavController().navigate(R.id.action_global_to_searchsimple, bundle)
                return true
            }
            R.id.save_search_button -> {
                if (viewModel.state.value.tags.isNotEmpty()) {
                    viewModel.saveSearch()
                    Toast.makeText(requireContext(), "Saved", Toast.LENGTH_LONG).show()
                }
                return true
            }
            R.id.manage_server_button -> {
                findNavController().navigate(R.id.action_global_to_servers)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun prepareAppBar() {
        (activity as MainActivity).setSupportActionBar(binding.searchPostTopAppBar)
        binding.appBarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())
        val supportActionBar = (activity as MainActivity).supportActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun BrowseFragmentBinding.bindState(
        uiState: StateFlow<UiState>,
        pagingData: Flow<PagingData<Post>>,
        uiActions: (UiAction) -> Unit,
    ) {
        val postAdapter = BrowserAdapter(
            onClick = { position ->
                val bundle = bundleOf("position" to position)
                val action = when (findNavController().currentDestination?.id) {
                    R.id.browseServerFragment -> R.id.action_browse_new_to_postslide
                    R.id.savedSearchBrowseServerFragment -> R.id.action_saved_search_browse_to_postslide
                    else -> null
                }

                action?.let {
                    findNavController().navigate(it, bundle)
                }
            }
        )

        postAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
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
                if (dy != 0) {
                    Log.d("BrowseFragment", "getPosition ${postsAdapter.getPosition()}")
                    onScrollChanged(UiAction.Scroll(uiState.value.server?.url,
                        currentTags = uiState.value.tags))
                }
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
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                pagingData.collectLatest(postsAdapter::submitData)
            }
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

            }
        }
    }
}