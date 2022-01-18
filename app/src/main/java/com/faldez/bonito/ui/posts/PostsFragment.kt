package com.faldez.bonito.ui.posts

import com.faldez.bonito.data.GelbooruRepository
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.faldez.bonito.MainActivity
import com.faldez.bonito.R
import com.faldez.bonito.adapter.PostsAdapter
import com.faldez.bonito.databinding.PostsFragmentBinding
import com.faldez.bonito.model.Post
import com.faldez.bonito.service.GelbooruService
import com.google.android.material.shape.MaterialShapeDrawable
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PostsFragment : Fragment() {

    companion object {
        fun newInstance() = PostsFragment()
    }

    private lateinit var viewModel: PostsViewModel

    private lateinit var binding: PostsFragmentBinding

    private val gelbooruService = GelbooruService.getInstance("https://safebooru.org")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        viewModel =
            ViewModelProvider(this,
                PostsViewModelFactory(GelbooruRepository(gelbooruService), this)).get(
                PostsViewModel::class.java)
        binding = PostsFragmentBinding.inflate(inflater, container, false)
        binding.bindState(
            uiState = viewModel.state,
            pagingData = viewModel.pagingDataFlow,
            uiActions = viewModel.accept
        )

        (activity as MainActivity).setSupportActionBar(binding.topAppBar)
        binding.appBarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        binding.postsRecyclerView.layoutManager = layoutManager
        binding.postsRecyclerView.setHasFixedSize(true)

    }

    override fun onResume() {
        super.onResume()
        binding.topAppBar.title = "Posts"
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.posts_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun PostsFragmentBinding.bindState(
        uiState: StateFlow<UiState>,
        pagingData: Flow<PagingData<Post>>,
        uiActions: (UiAction) -> Unit,
    ) {
        val postAdapter = PostsAdapter()
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

    private fun PostsFragmentBinding.bindSearch(
        uiState: StateFlow<UiState>,
        onTagsChanged: (UiAction.Search) -> Unit,
    ) {
        lifecycleScope.launch {
            uiState.map {
                it.tags
            }.distinctUntilChanged().collect()
        }
    }

    private fun PostsFragmentBinding.bindList(
        postsAdapter: PostsAdapter,
        uiState: StateFlow<UiState>,
        pagingData: Flow<PagingData<Post>>,
        onScrollChanged: (UiAction.Scroll) -> Unit,
    ) {
        retryButton.setOnClickListener { postsAdapter.retry() }
        postsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy != 0) onScrollChanged(UiAction.Scroll(currentTags = uiState.value.tags))
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