package com.faldez.bonito.ui.saved

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.faldez.bonito.MainActivity
import com.faldez.bonito.R
import com.faldez.bonito.data.PostRepository
import com.faldez.bonito.data.SavedSearchRepository
import com.faldez.bonito.database.AppDatabase
import com.faldez.bonito.databinding.SavedFragmentBinding
import com.faldez.bonito.service.BooruService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.shape.MaterialShapeDrawable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SavedFragment : Fragment() {
    private lateinit var viewModel: SavedViewModel
    private lateinit var binding: SavedFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = SavedFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

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
                val bundle = bundleOf("server" to it.server, "tags" to it.tags)
                findNavController()
                    .navigate(R.id.action_saved_to_browse,
                        bundle)
                hideBottomNavigation()
            },
            onDelete = {
                viewModel.delete(it)
            }
        )

        binding.savedSearchRecyclerView.adapter = adapter
        binding.savedSearchRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

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
                    adapter.updateDate(savedSearchPosts)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.savedSearches.collect { state ->
                state?.let { savedSearches ->
                    val postMap = viewModel.state
                    val savedSearchPosts = savedSearches.map {
                        SavedSearchPost(savedSearch = it, posts = postMap.value.get(it))
                    }
                    Log.d("SavedFragment", "$savedSearchPosts")
                    adapter.updateDate(savedSearchPosts)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                state.forEach { (savedSearch, list) ->
                    Log.d("SavedFragment", "$savedSearch $list")
                    adapter.updatePosts(SavedSearchPost(savedSearch, list))
                }
                binding.savedSwipeRefreshLayout.isRefreshing = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showBottomNavigation()
    }

    override fun onDestroy() {
        super.onDestroy()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.saved_search_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("SavedFragment", "Search New")
        when (item.itemId) {
            R.id.saved_search_button -> {
                findNavController().navigate(R.id.action_saved_to_browse_new)
                hideBottomNavigation()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showBottomNavigation() {
        val bottomNavigationView =
            (activity as MainActivity).findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.visibility = View.VISIBLE
    }

    private fun hideBottomNavigation() {
        val bottomNavigationView =
            (activity as MainActivity).findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.visibility = View.GONE
    }
}