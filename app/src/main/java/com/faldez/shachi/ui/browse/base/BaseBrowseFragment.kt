package com.faldez.shachi.ui.browse.base

import android.app.Dialog
import android.content.SharedPreferences
import android.content.res.Configuration
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
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.filter
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.faldez.shachi.MainActivity
import com.faldez.shachi.R
import com.faldez.shachi.databinding.BrowseFragmentBinding
import com.faldez.shachi.model.Post
import com.faldez.shachi.model.Rating
import com.faldez.shachi.model.ServerView
import com.faldez.shachi.ui.browse.BrowseAdapter
import com.faldez.shachi.ui.browse.BrowseViewModel
import com.faldez.shachi.ui.browse.UiAction
import com.faldez.shachi.ui.browse.UiState
import com.faldez.shachi.ui.search.SearchFragment
import com.faldez.shachi.ui.server_dialog.ServerDialogFragment
import com.faldez.shachi.widget.EmptyFooterDecoration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class BaseBrowseFragment : Fragment() {
    companion object {
        const val TAG = "BaseBrowseFragment"
    }

    private lateinit var binding: BrowseFragmentBinding

    abstract val viewModel: BrowseViewModel

    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = BrowseFragmentBinding.inflate(inflater, container, false)

        val server = arguments?.get("server") as ServerView?
        val tags = arguments?.get("tags") as String? ?: ""

        Log.d("BrowseFragment/onCreateView", "server: $server tags: $tags")

        if (server != null) {
            viewModel.selectServer(server)
        }
        viewModel.accept(UiAction.Search(tags))

        binding.bindState(
            uiState = viewModel.state,
            pagingData = viewModel.pagingDataFlow,
            uiActions = viewModel.accept,
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (resources.getBoolean(R.bool.isTablet)) {
            (activity as MainActivity).binding.sideNavigationRail?.headerView?.setOnClickListener {
                navigateToSearch()
            }
        } else {
            binding.searchFloatingButton?.setOnClickListener {
                navigateToSearch()
            }
        }
        prepareAppBar()
    }

    private fun navigateToSearch() {
        val bundle = bundleOf("server" to viewModel.state.value.server,
            "tags" to viewModel.state.value.tags)
        if (!resources.getBoolean(R.bool.isTablet)) {
            findNavController().navigate(R.id.action_browse_to_search, bundle)
        } else {
            val searchFragment = SearchFragment()
            searchFragment.arguments = bundle
            searchFragment.show(requireActivity().supportFragmentManager, "dialog")
        }
    }

    private fun prepareAppBar() {
        val savedSearchTitle = arguments?.getString("title")
        if (!resources.getBoolean(R.bool.isTablet)) {
            binding.appBarLayout.statusBarForeground =
                MaterialShapeDrawable.createWithElevationOverlay(requireContext())
        }

        binding.searchPostTopAppBar.menu.clear()
        binding.searchPostTopAppBar.inflateMenu(R.menu.browse_menu)

        if (findNavController().currentDestination?.id != R.id.browseFragment) {
            binding.searchPostTopAppBar.menu.removeItem(R.id.search_history_button)
        }

        binding.searchPostTopAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.save_search_button -> {
                    if (viewModel.state.value.tags.isEmpty()) {
                        Toast.makeText(requireContext(),
                            "Can't save search if selected tags is empty",
                            Toast.LENGTH_LONG).show()
                    } else {
                        val dialog =
                            MaterialAlertDialogBuilder(requireContext()).setView(R.layout.saved_search_title_dialog_fragment)
                                .setTitle(resources.getString(R.string.title))
                                .setMessage(resources.getString(R.string.saved_search_description_title_text))
                                .setPositiveButton(resources.getText(R.string.save)) { dialog, which ->
                                    val title =
                                        (dialog as Dialog).findViewById<TextInputEditText>(R.id.savedSearchTitleInput).text?.toString()

                                    if (viewModel.state.value.tags.isNotEmpty()) {
                                        viewModel.saveSearch(title)
                                        Toast.makeText(requireContext(), "Saved", Toast.LENGTH_LONG)
                                            .show()
                                    }
                                }.show()
                        dialog.findViewById<EditText>(R.id.savedSearchTitleInput)?.text =
                            SpannableStringBuilder(viewModel.state.value.tags.split(" ").first())
                    }
                    true
                }
                R.id.select_server_button -> {
                    val selectServerDialog = ServerDialogFragment()
                    selectServerDialog.show(requireActivity().supportFragmentManager, "dialog")
                    true
                }
                R.id.search_history_button -> {
                    findNavController().navigate(R.id.action_browse_to_searchhistory)
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }

        if (savedSearchTitle != null) {
            binding.searchPostTopAppBar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
            binding.searchPostTopAppBar.setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }
            binding.searchPostTopAppBar.title = savedSearchTitle
        } else {
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.state.collectLatest { state ->
                        state.server?.title?.let {
                            binding.searchPostTopAppBar.title = SpannableStringBuilder(it)
                        }
                    }
                }
            }
        }
    }

    private fun BrowseFragmentBinding.bindState(
        uiState: StateFlow<UiState>,
        pagingData: Flow<PagingData<Post>>,
        uiActions: (UiAction) -> Unit,
    ) {
        val gridCount = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> preferences.getString("grid_column_landscape", null)?.toInt() ?: 5
            else -> preferences.getString("grid_column_portrait", null)?.toInt() ?: 3
        }
        val gridMode = preferences.getString("grid_mode", null) ?: "staggered"
        val quality = preferences.getString("preview_quality", null) ?: "preview"
        val questionableFilter =
            preferences.getString("filter_questionable_content", null) ?: "disable"
        val explicitFilter = preferences.getString("filter_explicit_content", null) ?: "disable"

        val postAdapter = BrowseAdapter(
            gridMode = gridMode,
            quality = quality,
            hideQuestionable = questionableFilter == "hide",
            hideExplicit = explicitFilter == "hide",
            onClick = { position ->
                val bundle = bundleOf("position" to position)
                val id = when (findNavController().currentDestination?.id) {
                    R.id.browseSavedFragment -> R.id.action_browsesaved_to_postslide
                    R.id.browseFragment -> R.id.action_browse_to_postslide
                    else -> null
                }
                id?.let {
                    findNavController().navigate(it, bundle)
                }
            }
        )

        postAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        postsRecyclerView.adapter = postAdapter
        postsRecyclerView.layoutManager = if (gridMode == "staggered") {
            val layoutManager =
                StaggeredGridLayoutManager(gridCount, StaggeredGridLayoutManager.VERTICAL)
            layoutManager.gapStrategy =
                StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
            layoutManager
        } else {
            GridLayoutManager(requireContext(), gridCount)
        }

        bindList(
            postsAdapter = postAdapter,
            uiState = uiState,
            pagingData = pagingData,
            onScrollChanged = uiActions,
            showQuestionable = questionableFilter != "mute",
            showExplicit = explicitFilter != "mute"
        )
    }

    private fun BrowseFragmentBinding.bindList(
        postsAdapter: BrowseAdapter,
        uiState: StateFlow<UiState>,
        pagingData: Flow<PagingData<Post>>,
        onScrollChanged: (UiAction.Scroll) -> Unit,
        showQuestionable: Boolean,
        showExplicit: Boolean,
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

        val hideBottomBarOnScroll = preferences.getBoolean("hide_bottom_bar_on_scroll", true)
        if (hideBottomBarOnScroll) {
            postsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy < 0) {
                        (activity as MainActivity).showNavigation(callback = { binding.searchFloatingButton?.show() })
                    } else if (dy > 0) {
                        (activity as MainActivity).hideNavigation(callback = { binding.searchFloatingButton?.hide() })
                    }
                }
            })

            ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                val footer =
                    EmptyFooterDecoration(systemBars.bottom)
                postsRecyclerView.addItemDecoration(footer)
                insets
            }
        }

        retryButton.isVisible = false
        serverHelpText.isVisible = viewModel.state.value.server == null

        swipeRefreshLayout.setOnRefreshListener {
            postsAdapter.refresh()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                pagingData.collect {
                    postsAdapter.submitData(it.filter { post ->
                        when (post.rating) {
                            Rating.Questionable -> showQuestionable
                            Rating.Explicit -> showExplicit
                            Rating.Safe -> true
                        }
                    })
                }
            }
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
                shouldScrollToTop.collect { shouldScroll ->
                    val isNotEmpty = postsAdapter.itemCount != 0
                    Log.d(TAG,
                        "shouldScroll=$shouldScroll postsAdapter.itemCount=$isNotEmpty")
                    if (shouldScroll) postsRecyclerView.scrollToPosition(0)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                postsAdapter.loadStateFlow.collectLatest { loadState ->
                    val server = viewModel.state.value.server
                    val isListEmpty =
                        loadState.refresh is LoadState.NotLoading && postsAdapter.itemCount == 0
                    postsRecyclerView.isVisible = !isListEmpty
                    swipeRefreshLayout.isRefreshing = loadState.source.refresh is LoadState.Loading
                    retryButton.isVisible =
                        loadState.source.refresh is LoadState.Error && server != null
                    serverHelpText.isVisible = server == null
                }
            }
        }
    }
}