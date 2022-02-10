package com.faldez.shachi.ui.search_advanced

import android.app.Dialog
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.faldez.shachi.R
import com.faldez.shachi.databinding.SearchAdvancedFragmentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SearchAdvancedFragment : DialogFragment() {

    companion object {
        fun newInstance() = SearchAdvancedFragment()
    }

    private lateinit var viewModel: SearchAdvancedViewModel
    private lateinit var binding: SearchAdvancedFragmentBinding

    private var dialogView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = SearchAdvancedFragmentBinding.inflate(inflater, container, false)

        val tags = requireArguments().getString("tags") ?: ""
        binding.searchAdvancedTagsInputText.text = SpannableStringBuilder(tags)
        binding.searchAdvancedTagsInputText.addTextChangedListener {
            val input = it.toString()
            viewModel.state.value = UiState(tags = input)
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SearchAdvancedViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.advanced_search).apply {
            dialogView = onCreateView(layoutInflater, null, savedInstanceState)
            dialogView?.let {
                onViewCreated(it, savedInstanceState)
            }
            setView(dialogView)
            setPositiveButton(R.string.search) { _, _ ->
                val tags = viewModel.state.value.tags
                Log.d("SearchAdvancedFragment", "tags $tags")
                applySearch(tags)
            }
        }.create()

    override fun getView(): View? {
        return dialogView
    }

    private fun applySearch(tags: String) {
        findNavController().previousBackStackEntry?.savedStateHandle?.set("tags",
            Pair(null, tags))
    }
}