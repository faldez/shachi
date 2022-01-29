package com.faldez.bonito.ui.saved

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.faldez.bonito.MainActivity
import com.faldez.bonito.R
import com.faldez.bonito.data.PostRepository
import com.faldez.bonito.data.SavedSearchRepository
import com.faldez.bonito.database.AppDatabase
import com.faldez.bonito.databinding.SavedFragmentBinding
import com.faldez.bonito.service.BooruService
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SavedFragment : Fragment() {

    companion object {
        fun newInstance() = SavedFragment()
    }

    private lateinit var viewModel: SavedViewModel
    private var _binding: SavedFragmentBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = SavedFragmentBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this,
            SavedViewModelFactory(SavedSearchRepository(AppDatabase.build(requireContext())),
                PostRepository(
                    BooruService()))).get(
            SavedViewModel::class.java)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val adapter = SavedSearchAdapter(
            onBrowse = {
                val bundle = bundleOf("server" to it.server, "tags" to it.tags)
                (activity as MainActivity).navController
                    .navigate(R.id.action_global_to_browse,
                        bundle)
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}