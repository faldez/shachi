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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.faldez.shachi.R
import com.faldez.shachi.data.model.Post
import com.faldez.shachi.data.model.ServerView
import com.faldez.shachi.data.preference.*
import com.faldez.shachi.databinding.BrowseFragmentBinding
import com.faldez.shachi.ui.browse.BrowseAdapter
import com.faldez.shachi.ui.browse.BrowseViewModel
import com.faldez.shachi.ui.browse.UiAction
import com.faldez.shachi.ui.browse.UiState
import com.faldez.shachi.ui.server_dialog.ServerDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareAppBar()

        var server: ServerView? = null
        var tags = ""
        var start: Int? = null

        arguments?.also { args ->
            server = args.get("server") as ServerView?
            tags = args.get("tags") as String? ?: ""
            start = args.get("start") as Int?
        }

        val questionableFilter =
            preferences.getString(ShachiPreference.KEY_FILTER_QUESTIONABLE_CONTENT, null)
                ?.toFilter() ?: Filter.Disable
        val explicitFilter =
            preferences.getString(ShachiPreference.KEY_FILTER_EXPLICIT_CONTENT, null)?.toFilter()
                ?: Filter.Disable

        Log.d("BrowseFragment/onCreateView", "server: $server tags: $tags start: $start")

        if (server != null) {
            viewModel.selectServer(server!!)
        }
        viewModel.accept(UiAction.Search(tags, questionableFilter, explicitFilter, start))

        if (tags.isNotEmpty()) {
            binding.searchPostTopAppBar.subtitle = tags
        }

        binding.bindState(
            uiState = viewModel.state,
            pagingData = viewModel.pagingDataFlow,
            uiActions = viewModel.accept,
        )
    }

    private fun navigateToSearch() {
        val bundle = bundleOf("server" to viewModel.state.value.server,
            "tags" to viewModel.state.value.tags,
            "page" to viewModel.state.value.start)
        findNavController().navigate(R.id.action_browse_to_search, bundle)
    }

    private fun prepareAppBar() {
        val savedSearchTitle = arguments?.getString("title")

        binding.appBarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())

        binding.searchPostTopAppBar.menu.clear()
        binding.searchPostTopAppBar.inflateMenu(R.menu.browse_menu)

        binding.searchPostTopAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.search_button -> {
                    navigateToSearch()
                    true
                }
                R.id.save_search_button -> {
                    if (viewModel.state.value.tags.isEmpty()) {
                        Toast.makeText(requireContext(),
                            "Can't save search if selected tags is empty",
                            Toast.LENGTH_SHORT).show()
                        return@setOnMenuItemClickListener true
                    }

                    val dialog =
                        MaterialAlertDialogBuilder(requireContext()).setView(R.layout.saved_search_title_dialog_fragment)
                            .setTitle(resources.getString(R.string.title))
                            .setMessage(resources.getString(R.string.saved_search_description_title_text))
                            .setPositiveButton(resources.getText(R.string.save)) { dialog, which ->
                                val title =
                                    (dialog as Dialog).findViewById<TextInputEditText>(R.id.savedSearchTitleInput).text?.toString()
                                val tags =
                                    (dialog as Dialog).findViewById<TextInputEditText>(R.id.savedSearchTagsInput).text?.toString()

                                if (tags?.isNotEmpty() == true && title?.isNotEmpty() == true) {
                                    viewModel.saveSearch(title, tags)
                                    Toast.makeText(requireContext(),
                                        "Saved",
                                        Toast.LENGTH_SHORT)
                                        .show()
                                } else {
                                    Toast.makeText(requireContext(),
                                        "Can't save search if selected tags is empty",
                                        Toast.LENGTH_SHORT).show()
                                }
                            }.show()

                    var title = viewModel.state.value.tags.split(" ").first()
                    if (title.isNullOrEmpty()) title = viewModel.state.value.tags
                    dialog.findViewById<EditText>(R.id.savedSearchTitleInput)?.text =
                        SpannableStringBuilder(title)
                    dialog.findViewById<EditText>(R.id.savedSearchTagsInput)?.text =
                        SpannableStringBuilder(viewModel.state.value.tags)
                    true
                }
                R.id.select_server_button -> {
                    val selectServerDialog = ServerDialogFragment()
                    selectServerDialog.show(requireActivity().supportFragmentManager, "dialog")
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
            Configuration.ORIENTATION_LANDSCAPE -> preferences.getString(ShachiPreference.KEY_GRID_COLUMN_LANDSCAPE,
                null)?.toInt() ?: 5
            else -> preferences.getString(ShachiPreference.KEY_GRID_COLUMN_PORTRAIT, null)?.toInt()
                ?: 3
        }
        val gridMode = preferences.getString(ShachiPreference.KEY_GRID_MODE, null)?.toGridMode()
            ?: GridMode.Staggered
        val quality = preferences.getString(ShachiPreference.KEY_PREVIEW_QUALITY, null)?.toQuality()
            ?: Quality.Preview

        val postAdapter = BrowseAdapter(
            gridMode = gridMode,
            quality = quality,
            hideQuestionable = viewModel.state.value.questionableFilter == Filter.Hide,
            hideExplicit = viewModel.state.value.explicitFilter == Filter.Hide,
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
        postsRecyclerView.layoutManager = if (gridMode == GridMode.Staggered) {
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
        )
    }

    private fun BrowseFragmentBinding.bindList(
        postsAdapter: BrowseAdapter,
        uiState: StateFlow<UiState>,
        pagingData: Flow<PagingData<Post>>,
        onScrollChanged: (UiAction.Scroll) -> Unit,
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

        retryButton.isVisible = false
        serverHelpText.isVisible = viewModel.state.value.server == null

        swipeRefreshLayout.setOnRefreshListener {
            postsAdapter.refresh()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                pagingData.collect {
                    postsAdapter.submitData(it)
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