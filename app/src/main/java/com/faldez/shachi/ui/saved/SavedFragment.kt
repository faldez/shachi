package com.faldez.shachi.ui.saved

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.faldez.shachi.MainActivity
import com.faldez.shachi.R
import com.faldez.shachi.database.AppDatabase
import com.faldez.shachi.databinding.SavedFragmentBinding
import com.faldez.shachi.repository.PostRepository
import com.faldez.shachi.repository.SavedSearchRepository
import com.faldez.shachi.service.BooruService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.MaterialShapeDrawable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class SavedFragment : Fragment() {
    private lateinit var viewModel: SavedViewModel
    private lateinit var binding: SavedFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = SavedFragmentBinding.inflate(inflater, container, false)

        (activity as MainActivity).setSupportActionBar(binding.savedTopappbar)

        binding.savedAppbarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())

        viewModel = ViewModelProvider(this,
            SavedViewModelFactory(SavedSearchRepository(AppDatabase.build(requireContext())),
                PostRepository(
                    BooruService()))).get(
            SavedViewModel::class.java)

        val adapter = SavedSearchAdapter(
            onBrowse = {
                val bundle = bundleOf("title" to it.savedSearch.savedSearchTitle,
                    "server" to it.server,
                    "tags" to it.savedSearch.tags)
                findNavController()
                    .navigate(R.id.action_saved_to_browse,
                        bundle)
            },
            onDelete = { savedSearch ->
                MaterialAlertDialogBuilder(requireContext()).setTitle("Delete " + savedSearch.savedSearch.savedSearchTitle)
                    .setMessage("Are you Sure?")
                    .setPositiveButton("Yes"
                    ) { _, _ -> viewModel.delete(savedSearch.savedSearch) }
                    .setNegativeButton("No", null).show()
            }
        )

        binding.savedSearchRecyclerView.adapter = adapter

        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.savedSearchRecyclerView.layoutManager = layoutManager

        val divider = DividerItemDecoration(binding.savedSearchRecyclerView.context,
            layoutManager.orientation)
        binding.savedSearchRecyclerView.addItemDecoration(divider)

        binding.savedSwipeRefreshLayout.setOnRefreshListener {
            viewModel.clearPosts()
            viewModel.refreshAll()
        }

        lifecycleScope.launch {
            viewModel.savedSearches.collect { state ->
                state?.let { savedSearches ->
                    val postMap = viewModel.state
                    val savedSearchPosts = savedSearches.map {
                        SavedSearchPost(savedSearch = it, posts = postMap.value.get(it))
                    }
                    Log.d("SavedFragment", "$savedSearchPosts")
                    adapter.submitList(savedSearchPosts)
                    binding.savedSwipeRefreshLayout.isRefreshing = false
                }
            }
        }

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                state.let { savedSearches ->
                    val savedSearchPosts = savedSearches.map { (savedSearch, posts) ->
                        SavedSearchPost(savedSearch = savedSearch, posts = posts)
                    }
                    Log.d("SavedFragment", "$savedSearchPosts")
                    adapter.submitList(savedSearchPosts)
                    binding.savedSwipeRefreshLayout.isRefreshing = false
                }
            }
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
    }


//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.saved_search_menu, menu)
//        super.onCreateOptionsMenu(menu, inflater)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        Log.d("SavedFragment", "Search New")
//        when (item.itemId) {
//            R.id.more_button -> {
//                findNavController().navigate(R.id.action_saved_to_more)
//                return true
//            }
//        }
//
//        return super.onOptionsItemSelected(item)
//    }
}