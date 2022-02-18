package com.faldez.shachi.ui.search_history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.faldez.shachi.R
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
                val bundle = bundleOf("server" to searchHistoryServer.server,
                    "tags" to searchHistoryServer.searchHistory.tags)
                findNavController().navigate(R.id.action_searchhistory_to_browse, bundle)
            }
        ).apply {
            submitList(searchHistories)
        }
        searchHistoryRecyclerView.adapter = adapter
    }
}