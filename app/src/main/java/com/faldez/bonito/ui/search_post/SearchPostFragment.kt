package com.faldez.bonito.ui.search_post

import com.faldez.bonito.data.PostRepository
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.os.bundleOf
import androidx.core.view.*
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
import com.faldez.bonito.data.ServerRepository
import com.faldez.bonito.database.AppDatabase
import com.faldez.bonito.databinding.SearchPostFragmentBinding
import com.faldez.bonito.model.Post
import com.faldez.bonito.service.BooruService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.shape.MaterialShapeDrawable
import com.lapism.search.widget.MaterialSearchView
import com.lapism.search.widget.NavigationIconCompat
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchPostFragment : Fragment() {
    companion object {
        const val TAG = "SearchPostFragment"
    }

    private lateinit var binding: SearchPostFragmentBinding


    private val viewModel: SearchPostViewModel by
    navGraphViewModels(R.id.nav_graph) {
        SearchPostViewModelFactory(PostRepository(BooruService()),
            ServerRepository(AppDatabase.build(requireContext())),
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
        Log.d(TAG, "onCreateView " + savedInstanceState.toString())
        super.onCreateView(inflater, container, savedInstanceState)
        binding = SearchPostFragmentBinding.inflate(inflater, container, false)

        val view = binding.root

        binding.bindState(
            uiState = viewModel.state,
            pagingData = viewModel.pagingDataFlow,
            uiActions = viewModel.accept
        )

        (activity as MainActivity).setSupportActionBar(binding.materialSearchBar.getToolbar())
        binding.appBarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())

        val bottomNavigationView =
            (activity as MainActivity).findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        if (bottomNavigationView.visibility == View.GONE) {
            bottomNavigationView.visibility = View.VISIBLE
        }

        val layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS

        binding.postsRecyclerView.layoutManager = layoutManager

        binding.materialSearchBar.apply {
            setHint("Tags")
            setStrokeWidth(0)
            setBackgroundColor(resources.getColor(R.color.background))
            setOnClickListener {
                binding.materialSearchView.requestFocus()
                binding.materialSearchView.setTextQuery(viewModel.state.value.tags, false)
            }
            setNavigationOnClickListener {

            }
        }

        binding.materialSearchView.apply {
            navigationIconCompat = NavigationIconCompat.ARROW
            setNavigationOnClickListener {
                binding.materialSearchView.clearFocus()
            }
            setHint("tags_1 tags_2")
            setOnFocusChangeListener(object : MaterialSearchView.OnFocusChangeListener {
                override fun onFocusChange(hasFocus: Boolean) {
                    val bottomNavigationView =
                        (activity as MainActivity).findViewById<BottomNavigationView>(R.id.bottomNavigationView)
                    val visibility = if (hasFocus) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }

                    bottomNavigationView.visibility = visibility
                    binding.materialSearchBar.visibility = visibility
                }
            })
        }

        ViewCompat.setOnApplyWindowInsetsListener(view) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply the insets as padding to the view. Here we're setting all of the
            // dimensions, but apply as appropriate to your layout. You could also
            // update the views margin if more appropriate.
            (view.layoutParams as ViewGroup.MarginLayoutParams).setMargins(insets.left,
                insets.top,
                insets.right,
                insets.bottom)

            // Return CONSUMED if we don't want the window insets to keep being passed
            // down to descendant views.
            WindowInsetsCompat.CONSUMED
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "onActivityCreated " + savedInstanceState.toString())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.posts_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.pin_tags_button -> {
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

    private fun SearchPostFragmentBinding.bindState(
        uiState: StateFlow<UiState>,
        pagingData: Flow<PagingData<Post>>,
        uiActions: (UiAction) -> Unit,
    ) {
        val postAdapter = SearchPostAdapter(
            onClick = { posts, position ->
                val bundle = bundleOf("posts" to posts, "position" to position)
                findNavController().navigate(R.id.action_searchpost_to_postslide, bundle)
                hideBottomNavigationView()
            }
        )
        postsRecyclerView.adapter = postAdapter
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

    private fun SearchPostFragmentBinding.bindSearch(
        uiState: StateFlow<UiState>,
        onTagsChanged: (UiAction.Search) -> Unit,
    ) {
        binding.materialSearchView.setOnQueryTextListener(object :
            MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: CharSequence) {

            }

            override fun onQueryTextSubmit(query: CharSequence) {
                onTagsChanged(UiAction.Search(uiState.value.server?.serverId, query.toString()))
                binding.materialSearchView.clearFocus()
            }
        })

        lifecycleScope.launch {
            uiState.map {
                it.tags
            }.distinctUntilChanged().collect(binding.materialSearchBar::setText)
        }
    }

    private fun SearchPostFragmentBinding.bindList(
        postsAdapter: SearchPostAdapter,
        uiState: StateFlow<UiState>,
        pagingData: Flow<PagingData<Post>>,
        onScrollChanged: (UiAction.Scroll) -> Unit,
    ) {
        retryButton.setOnClickListener { postsAdapter.retry() }
        postsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy != 0) onScrollChanged(UiAction.Scroll(uiState.value.server?.serverId, currentTags = uiState.value.tags))
            }
        })
        val notLoading = postsAdapter.loadStateFlow.distinctUntilChangedBy { it.source.refresh }
            .map { it.source.refresh is LoadState.NotLoading }
        val hasNotScrolledForCurrentSearch =
            uiState.map { it.hasNotScrolledForCurrentTag }.distinctUntilChanged()

        val shouldScrollToTop = combine(
            notLoading,
            hasNotScrolledForCurrentSearch,
            Boolean::and
        )

        lifecycleScope.launch {
            pagingData.collectLatest(postsAdapter::submitData)
        }

        lifecycleScope.launch {
            shouldScrollToTop.collect { shouldScroll ->
                if (shouldScroll) postsRecyclerView.scrollToPosition(0)
            }
        }

        lifecycleScope.launch {
            postsAdapter.loadStateFlow.collect { loadState ->
                val isListEmpty =
                    loadState.refresh is LoadState.NotLoading && postsAdapter.itemCount == 0
                postsRecyclerView.isVisible = !isListEmpty
                progressBar.isVisible = loadState.source.refresh is LoadState.Loading
                retryButton.isVisible = loadState.source.refresh is LoadState.Error
            }
        }
    }
}