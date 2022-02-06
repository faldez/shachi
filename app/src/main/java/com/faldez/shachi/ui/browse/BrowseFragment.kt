package com.faldez.shachi.ui.browse

import android.app.Dialog
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.*
import android.widget.EditText
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
import com.faldez.shachi.MainActivity
import com.faldez.shachi.R
import com.faldez.shachi.repository.FavoriteRepository
import com.faldez.shachi.repository.PostRepository
import com.faldez.shachi.repository.SavedSearchRepository
import com.faldez.shachi.repository.ServerRepository
import com.faldez.shachi.database.AppDatabase
import com.faldez.shachi.databinding.BrowseFragmentBinding
import com.faldez.shachi.model.Post
import com.faldez.shachi.model.Server
import com.faldez.shachi.model.Tag
import com.faldez.shachi.service.BooruService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.MaterialShapeDrawable
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BrowseFragment : Fragment() {
    companion object {
        const val TAG = "BrowseFragment"
    }

    private lateinit var binding: BrowseFragmentBinding

    private val viewModel: BrowseViewModel by navGraphViewModels(R.id.nav_graph) {
        val db = AppDatabase.build(requireContext())
        val favoriteRepository = FavoriteRepository(db)
        BrowseViewModelFactory(
            PostRepository(BooruService()),
            ServerRepository(db),
            favoriteRepository,
            SavedSearchRepository(db),
            this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = BrowseFragmentBinding.inflate(inflater, container, false)

        prepareAppBar()
        arguments?.getString("title")?.let {
            binding.searchPostTopAppBar.title = it
            val supportActionBar = (activity as MainActivity).supportActionBar
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        val server = arguments?.get("server") as Server?
        val tags =
            (arguments?.get("tags") as String?)?.split(" ")?.map { name -> Tag.fromName(name) }
        Log.d("BrowseFragment", "$server $tags")

        binding.bindState(
            uiState = viewModel.state,
            pagingData = viewModel.pagingDataFlow,
            uiActions = viewModel.accept,
            server, tags
        )

        return binding.root
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
            }
            R.id.save_search_button -> {
                if (viewModel.state.value.tags.isEmpty()) {
                    Toast.makeText(requireContext(),
                        "Can't save search if selected tags is empty",
                        Toast.LENGTH_LONG).show()
                    return true
                }

                val dialog =
                    MaterialAlertDialogBuilder(requireContext()).setView(R.layout.saved_search_title_dialog_fragment)
                        .setTitle(resources.getString(R.string.title))
                        .setMessage(resources.getString(R.string.saved_search_description_title_text))
                        .setPositiveButton(resources.getText(R.string.save)) { dialog, which ->
                            val title =
                                (dialog as Dialog).findViewById<EditText>(R.id.savedSearchTitleInput).text?.toString()

                            if (viewModel.state.value.tags.isNotEmpty()) {
                                viewModel.saveSearch(title)
                                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_LONG).show()
                            }
                        }.show()
                dialog.findViewById<EditText>(R.id.savedSearchTitleInput)?.text =
                    SpannableStringBuilder(viewModel.state.value.tags.first().name)
            }
            R.id.select_server_button -> {
                findNavController().navigate(R.id.action_global_to_serverdialog)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }


    private fun prepareAppBar() {
        (activity as MainActivity).setSupportActionBar(binding.searchPostTopAppBar)
        binding.appBarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())
    }

    private fun BrowseFragmentBinding.bindState(
        uiState: StateFlow<UiState>,
        pagingData: Flow<PagingData<Post>>,
        uiActions: (UiAction) -> Unit,
        server: Server?,
        tags: List<Tag>?,
    ) {
        val postAdapter = BrowseAdapter(
            onClick = { position ->
                val bundle = bundleOf("position" to position)
                findNavController().navigate(R.id.action_global_to_postslide, bundle)
            }
        )

        postAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        postsRecyclerView.adapter = postAdapter

        val layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS

        postsRecyclerView.layoutManager = layoutManager

        bindList(
            postsAdapter = postAdapter,
            uiState = uiState,
            pagingData = pagingData,
            onScrollChanged = uiActions,
            server,
            tags
        )
    }

    private fun BrowseFragmentBinding.bindList(
        postsAdapter: BrowseAdapter,
        uiState: StateFlow<UiState>,
        pagingData: Flow<PagingData<Post>>,
        onScrollChanged: (UiAction.Scroll) -> Unit,
        server: Server?,
        tags: List<Tag>?,
    ) {
        retryButton.setOnClickListener { postsAdapter.retry() }
        postsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy != 0) {
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
                pagingData.collect(postsAdapter::submitData)
            }
        }

        lifecycleScope.launch {
            shouldScrollToTop.collect { shouldScroll ->
                Log.d(TAG, "shouldScroll $shouldScroll")
                if (shouldScroll) postsRecyclerView.scrollToPosition(0)
            }
        }

        lifecycleScope.launch {
            postsAdapter.loadStateFlow.collectLatest { loadState ->
                val isListEmpty =
                    loadState.refresh is LoadState.NotLoading && postsAdapter.itemCount == 0
                postsRecyclerView.isVisible = !isListEmpty
                swipeRefreshLayout.isRefreshing = loadState.source.refresh is LoadState.Loading
                retryButton.isVisible =
                    loadState.source.refresh is LoadState.Error && viewModel.state.value.server != null

            }
        }

        viewModel.accept(UiAction.GetSelectedOrSelectServer(server))
        viewModel.accept(UiAction.Search(server?.url, tags ?: listOf()))
    }
}