package com.faldez.shachi.ui.post_detail

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.faldez.shachi.R
import com.faldez.shachi.data.TagRepository
import com.faldez.shachi.databinding.PostDetailBottomSheetFragmentBinding
import com.faldez.shachi.databinding.TagsDetailsBinding
import com.faldez.shachi.model.Post
import com.faldez.shachi.model.Server
import com.faldez.shachi.model.ServerView
import com.faldez.shachi.model.Tag
import com.faldez.shachi.service.BooruService
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
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

        val server = requireArguments().get("server") as ServerView?
        val post = requireArguments().get("post") as Post?

        val currentSearchTags = requireArguments().get("tags") as List<Tag>?
        Log.d("PostDetailBottomSheetFragment", "currentSearchTags $currentSearchTags")
        val currentSearchTagsSet = currentSearchTags?.map { it.name }?.toSet()

        viewModel = PostDetailBottomSheetViewModel(server, TagRepository(BooruService()))

        post?.let { p ->
            binding.sizeTextview.text = "${p.width}x${p.height}"
            binding.sourceUrl.text = p.source
            binding.ratingTextview.text = p.rating
            binding.scoreTextview.text = "${p.score ?: 0}"
            binding.postedTextview.text = p.createdAt

            viewModel.getTags(p.tags)
        }

        lifecycleScope.launch {
            viewModel.state.collect { tags ->
                binding.loadingLabel.isVisible = tags == null
                val splitTags =
                    (tags ?: listOf()).groupBy { currentSearchTagsSet?.contains(it.name) ?: false }
                val groupedTags = splitTags[false]?.groupBy { it.type }
                Log.d("SearchSimpleFragment", "collect $groupedTags")
                binding.currentSearchTagsHeader.isVisible = false
                tagDetailsBinding.generalTagsHeader.isVisible = false
                tagDetailsBinding.artistTagsHeader.isVisible = false
                tagDetailsBinding.copyrightTagsHeader.isVisible = false
                tagDetailsBinding.characterTagsHeader.isVisible = false
                tagDetailsBinding.metadataTagsHeader.isVisible = false
                tagDetailsBinding.otherTagsHeader.isVisible = false

                binding.currentSearchTagsHeader.isVisible = !splitTags[true].isNullOrEmpty()
                binding.currentSearchTagsChipGroup.isVisible = !splitTags[true].isNullOrEmpty()
                splitTags[true]?.sortedBy { it.type }?.forEach { tag ->
                    val textColor = when (tag.type) {
                        0 -> R.color.tag_general
                        1 -> R.color.tag_artist
                        3 -> R.color.tag_copyright
                        4 -> R.color.tag_character
                        5 -> R.color.tag_metadata
                        else -> null
                    }

                    val chip = Chip(requireContext())
                    chip.bind(binding.currentSearchTagsChipGroup, textColor, tag)
                }

                groupedTags?.forEach { (type, tags) ->
                    lateinit var group: ChipGroup
                    lateinit var header: TextView
                    var textColor: Int? = null
                    when (type) {
                        0 -> {
                            group = tagDetailsBinding.generalTagsChipGroup
                            header = tagDetailsBinding.generalTagsHeader
                            textColor = R.color.tag_general
                        }
                        1 -> {
                            group = tagDetailsBinding.artistTagsChipGroup
                            header = tagDetailsBinding.artistTagsHeader
                            textColor = R.color.tag_artist
                        }
                        3 -> {
                            group = tagDetailsBinding.copyrightTagsChipGroup
                            header = tagDetailsBinding.copyrightTagsHeader
                            textColor = R.color.tag_copyright
                        }
                        4 -> {
                            group = tagDetailsBinding.characterTagsChipGroup
                            header = tagDetailsBinding.characterTagsHeader
                            textColor = R.color.tag_character
                        }
                        5 -> {
                            group = tagDetailsBinding.metadataTagsChipGroup
                            header = tagDetailsBinding.metadataTagsHeader
                            textColor = R.color.tag_metadata
                        }
                        else -> {
                            group = tagDetailsBinding.otherTagsChipGroup
                            header = tagDetailsBinding.otherTagsHeader
                        }
                    }
                    header.isVisible = tags.isNotEmpty()
                    group.isVisible = tags.isNotEmpty()

                    tags.forEach { tag ->
                        val chip = Chip(requireContext())
                        chip.bind(group, textColor, tag)
                    }
                }
            }
        }

        return binding.root
    }

    private fun Chip.bind(group: ChipGroup, textColor: Int?, tag: Tag) {
        Log.d("PostDetailBottomSheetFragment", "$tag")

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