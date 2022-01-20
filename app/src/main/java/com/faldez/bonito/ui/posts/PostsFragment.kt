package com.faldez.bonito.ui.posts

import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import com.faldez.bonito.data.GelbooruRepository
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.faldez.bonito.MainActivity
import com.faldez.bonito.R
import com.faldez.bonito.adapter.PostsAdapter
import com.faldez.bonito.databinding.PostsFragmentBinding
import com.faldez.bonito.model.Post
import com.faldez.bonito.service.GelbooruService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.shape.MaterialShapeDrawable
import com.lapism.search.widget.MaterialSearchView
import com.lapism.search.widget.NavigationIconCompat
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PostsFragment : Fragment() {

    companion object {
        public const val TAG = "PostsFragment"
    }

    private lateinit var viewModel: PostsViewModel

    private lateinit var binding: PostsFragmentBinding

    private val gelbooruService = GelbooruService.getInstance("https://safebooru.org")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        Log.d(TAG, "onCreate " + savedInstanceState.toString())
        viewModel =
            ViewModelProvider(this,
                PostsViewModelFactory(GelbooruRepository(gelbooruService), this)).get(
                PostsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        Log.d(TAG, "onCreateView " + savedInstanceState.toString())
        super.onCreateView(inflater, container, savedInstanceState)
        binding = PostsFragmentBinding.inflate(inflater, container, false)

        val view = binding.root

        binding.bindState(
            uiState = viewModel.state,
            pagingData = viewModel.pagingDataFlow,
            uiActions = viewModel.accept
        )

        (activity as MainActivity).setSupportActionBar(binding.materialSearchBar.getToolbar())
        binding.appBarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())

        val layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS

        binding.postsRecyclerView.layoutManager = layoutManager

        binding.materialSearchBar.apply {
            navigationIconCompat = NavigationIconCompat.SEARCH
            setHint("Tags")
            setStrokeWidth(0)
            setBackgroundColor(resources.getColor(R.color.background))
            setOnClickListener {
                binding.materialSearchView.requestFocus()
                binding.materialSearchView.setTextQuery(viewModel.state.value.tags, false)
            }
            setNavigationOnClickListener {
                binding.materialSearchView.requestFocus()
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
                    val visibility = if (hasFocus) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                    (activity as MainActivity).findViewById<BottomNavigationView>(R.id.bottomNavigationView).visibility =
                        visibility
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "onSaveInstanceState " + outState.toString())
    }

    override fun onResume() {
        super.onResume()
//        binding.topAppBar.title = "Posts"
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.posts_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
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
        binding.materialSearchView.setOnQueryTextListener(object :
            MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: CharSequence) {

            }

            override fun onQueryTextSubmit(query: CharSequence) {
                onTagsChanged(UiAction.Search(query.toString()))
                binding.materialSearchView.clearFocus()
            }
        })

        lifecycleScope.launch {
            uiState.map {
                it.tags
            }.distinctUntilChanged().collect(binding.materialSearchBar::setText)
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