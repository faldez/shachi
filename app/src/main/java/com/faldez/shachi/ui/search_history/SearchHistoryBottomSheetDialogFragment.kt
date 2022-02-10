package com.faldez.shachi.ui.search_history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.faldez.shachi.MainActivity
import com.faldez.shachi.databinding.SearchHistoryBottomSheetDialogFragmentBinding
import com.faldez.shachi.model.SearchHistoryServer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SearchHistoryBottomSheetDialogFragment : BottomSheetDialogFragment() {
    private lateinit var binding: SearchHistoryBottomSheetDialogFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = SearchHistoryBottomSheetDialogFragmentBinding.inflate(inflater,
            container,
            false)

        val searchHistories =
            (requireArguments().get("search_histories") as List<*>).filterIsInstance<SearchHistoryServer>()
        Log.d("SearchHistoryBottomSheetDialogFragment/onCreateView",
            "searchHistories $searchHistories")
        binding.bind(searchHistories)

        return binding.root
    }

    private fun SearchHistoryBottomSheetDialogFragmentBinding.bind(searchHistories: List<SearchHistoryServer>) {
        val adapter = SearchHistoryAdapter(
            onClick = { searchHistoryServer ->
                findNavController().previousBackStackEntry?.savedStateHandle?.set("tags",
                    Pair(searchHistoryServer.server, searchHistoryServer.searchHistory.tags))
                (activity as MainActivity).onBackPressed()
            }
        ).apply {
            submitList(searchHistories)
        }
        searchHistoryRecyclerView.adapter = adapter
    }
}