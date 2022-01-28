package com.faldez.bonito.ui.post_detail

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.faldez.bonito.R
import com.faldez.bonito.data.TagRepository
import com.faldez.bonito.databinding.PostDetailBottomSheetFragmentBinding
import com.faldez.bonito.model.Post
import com.faldez.bonito.model.Server
import com.faldez.bonito.model.Tag
import com.faldez.bonito.service.BooruService
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PostDetailBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: PostDetailBottomSheetFragmentBinding
    private lateinit var viewModel: PostDetailBottomSheetViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = PostDetailBottomSheetFragmentBinding.inflate(inflater, container, false)

        val server = requireArguments().getParcelable("server") as Server?
        val post = requireArguments().getParcelable("post") as Post?

        viewModel = PostDetailBottomSheetViewModel(server, TagRepository(BooruService()))

        post?.let { post ->
            binding.sizeTextview.text = "${post.width}x${post.height}"
            binding.sourceUrl.text = post?.source
            binding.ratingTextview.text = post.rating
            binding.scoreTextview.text = "${post.score ?: 0}"
            binding.postedTextview.text = post.createdAt

            viewModel.getTags(post.tags)
        }

        lifecycleScope.launch {
            viewModel.state.collect { tags ->
                binding.loadingLabel.isVisible = tags == null
                tags?.forEach { tag ->
                    val chip = Chip(requireContext())
                    chip.bind(tag)
                }

            }
        }

        return binding.root
    }


    private fun Chip.bind(tag: Tag) {
        val textColor = when (tag.type) {
            0 -> R.color.tag_general
            1 -> R.color.tag_artist
            3 -> R.color.tag_copyright
            4 -> R.color.tag_character
            5 -> R.color.tag_metadata
            else -> null
        }

        this.apply {
            text = tag.name
            textColor?.let {
                setTextColor(ColorStateList.valueOf(ResourcesCompat.getColor(resources,
                    textColor,
                    requireActivity().theme)))
            }
            setEnsureMinTouchTargetSize(false)
        }

        binding.tagListChipgroup.addView(this)
    }
}