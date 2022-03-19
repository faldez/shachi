package com.faldez.shachi.ui.comment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.faldez.shachi.data.database.AppDatabase
import com.faldez.shachi.data.repository.comment.CommentRepositoryImpl
import com.faldez.shachi.data.repository.server.ServerRepositoryImpl
import com.faldez.shachi.databinding.CommentFragmentBinding
import com.faldez.shachi.data.api.BooruApiImpl
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CommentFragment : BottomSheetDialogFragment() {

    private lateinit var binding: CommentFragmentBinding
    private val viewModel: CommentViewModel by viewModels {
        CommentViewModelFactory(serverRepository = ServerRepositoryImpl(AppDatabase.build(
            requireContext())),
            commentRepository = CommentRepositoryImpl(BooruApiImpl()), this)
    }
    private lateinit var adapter: CommentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = CommentFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val serverId = requireArguments().getInt("server_id")
        val postId = requireArguments().getInt("post_id")

        viewModel.accept(UiAction.GetComment(serverId, postId))

        adapter = CommentAdapter()

        binding.commentRecyclerView.layoutManager =
            StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL).apply {
                gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
            }
        binding.commentRecyclerView.adapter = adapter

        lifecycleScope.launch {
            viewModel.commentFlow.collectLatest { comments ->
                binding.circularProgressIndicator.isVisible = false
                if (comments?.isNotEmpty() == true) {
                    Log.d("CommentFragment", "$comments")
                    adapter.submitList(comments)
                } else {
                    binding.emptyTextView.isVisible = true
                }
            }
        }
    }

}