package com.faldez.bonito.ui.post_detail

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.faldez.bonito.R
import com.faldez.bonito.data.TagRepository
import com.faldez.bonito.databinding.PostDetailBottomSheetFragmentBinding
import com.faldez.bonito.databinding.TagsDetailsBinding
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
    private lateinit var tagDetailsBinding: TagsDetailsBinding
    private lateinit var viewModel: PostDetailBottomSheetViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = PostDetailBottomSheetFragmentBinding.inflate(inflater, container, false)
        tagDetailsBinding = TagsDetailsBinding.bind(binding.root)

        val server = requireArguments().get("server") as Server?
        val post = requireArguments().get("post") as Post?

        val test = requireArguments().get("tags") as List<Tag>?
        Log.d("PostDetailBottomSheetFragment", "currentSearchTags $test")
        val currentSearchTags = test?.map { it.name }?.toSet()

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
                val splitTags =
                    (tags ?: listOf()).groupBy { currentSearchTags?.contains(it.name) ?: false }
                val currentSearchGroupedTags = splitTags[true]?.groupBy { it.type }
                val groupedTags = splitTags[false]?.groupBy { it.type }
                Log.d("SearchSimpleFragment", "collect $groupedTags")
                binding.currentSearchTagsHeader.isVisible = false
                tagDetailsBinding.generalTagsHeader.isVisible = false
                tagDetailsBinding.artistTagsHeader.isVisible = false
                tagDetailsBinding.copyrightTagsHeader.isVisible = false
                tagDetailsBinding.characterTagsHeader.isVisible = false
                tagDetailsBinding.metadataTagsHeader.isVisible = false
                tagDetailsBinding.otherTagsHeader.isVisible = false

                currentSearchGroupedTags?.forEach { (type, tags) ->
                    var group = binding.currentSearchTagsChipGroup
                    var header = binding.currentSearchTagsHeader
                    group.removeAllViews()
                    header.isVisible = tags.isNotEmpty()
                    group.isVisible = tags.isNotEmpty()

                    tags.forEach { tag ->
                        val chip = Chip(requireContext())
                        chip.bindCurrentSearch(tag)
                    }
                }

                groupedTags?.forEach { (type, tags) ->
                    var group = tagDetailsBinding.otherTagsChipGroup
                    var header = tagDetailsBinding.otherTagsHeader
                    when (type) {
                        0 -> {
                            group = tagDetailsBinding.generalTagsChipGroup
                            header = tagDetailsBinding.generalTagsHeader
                        }
                        1 -> {
                            group = tagDetailsBinding.artistTagsChipGroup
                            header = tagDetailsBinding.artistTagsHeader
                        }
                        3 -> {
                            group = tagDetailsBinding.copyrightTagsChipGroup
                            header = tagDetailsBinding.copyrightTagsHeader
                        }
                        4 -> {
                            group = tagDetailsBinding.characterTagsChipGroup
                            header = tagDetailsBinding.characterTagsHeader
                        }
                        5 -> {
                            group = tagDetailsBinding.metadataTagsChipGroup
                            header = tagDetailsBinding.metadataTagsHeader
                        }
                        else -> tagDetailsBinding.otherTagsChipGroup
                    }
                    group.removeAllViews()
                    header.isVisible = tags.isNotEmpty()
                    group.isVisible = tags.isNotEmpty()

                    tags.forEach { tag ->
                        val chip = Chip(requireContext())
                        chip.bind(tag)
                    }
                }
            }
        }

        return binding.root
    }

    private fun Chip.bindCurrentSearch(tag: Tag) {
        var textColor: Int? = null
        var group = binding.currentSearchTagsChipGroup
        when (tag.type) {
            0 -> {
                textColor = R.color.tag_general
            }
            1 -> {
                textColor = R.color.tag_artist
            }
            3 -> {
                textColor = R.color.tag_copyright
            }
            4 -> {
                textColor = R.color.tag_character
            }
            5 -> {
                textColor = R.color.tag_metadata
            }
        }

        this.apply {
            text = tag.name
            textColor?.let {
                setTextColor(ColorStateList.valueOf(ResourcesCompat.getColor(resources,
                    textColor,
                    requireActivity().theme)))
            }
            setEnsureMinTouchTargetSize(false)
            group.addView(this)
        }
    }


    private fun Chip.bind(tag: Tag) {
        Log.d("PostDetailBottomSheetFragment", "$tag")
        var textColor: Int? = null
        var group = tagDetailsBinding.otherTagsChipGroup
        when (tag.type) {
            0 -> {
                textColor = R.color.tag_general
                group = tagDetailsBinding.generalTagsChipGroup
            }
            1 -> {
                textColor = R.color.tag_artist
                group = tagDetailsBinding.artistTagsChipGroup
            }
            3 -> {
                textColor = R.color.tag_copyright
                group = tagDetailsBinding.copyrightTagsChipGroup
            }
            4 -> {
                textColor = R.color.tag_character
                group = tagDetailsBinding.characterTagsChipGroup
            }
            5 -> {
                textColor = R.color.tag_metadata
                group = tagDetailsBinding.metadataTagsChipGroup
            }
        }

        this.apply {
            text = tag.name
            textColor?.let {
                setTextColor(ColorStateList.valueOf(ResourcesCompat.getColor(resources,
                    textColor,
                    requireActivity().theme)))
            }
            setEnsureMinTouchTargetSize(false)
            group.addView(this)
        }
    }
}