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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.faldez.shachi.R
import com.faldez.shachi.database.AppDatabase
import com.faldez.shachi.databinding.PostDetailFragmentBinding
import com.faldez.shachi.databinding.TagsDetailsBinding
import com.faldez.shachi.model.Category
import com.faldez.shachi.model.Post
import com.faldez.shachi.model.TagDetail
import com.faldez.shachi.repository.ServerRepository
import com.faldez.shachi.repository.TagRepository
import com.faldez.shachi.service.BooruService
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PostDetailFragment : Fragment() {
    private lateinit var binding: PostDetailFragmentBinding
    private lateinit var tagDetailsBinding: TagsDetailsBinding
    private val viewModel: PostDetailViewModel by viewModels {
        val post = requireArguments().get("post") as Post
        val db = AppDatabase.build(requireContext())
        PostDetailViewModelFactory(post, ServerRepository(db),
            TagRepository(BooruService(), db),
            this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = PostDetailFragmentBinding.inflate(inflater, container, false)
        tagDetailsBinding = TagsDetailsBinding.bind(binding.root)
        binding.bind()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentSearchTags = requireArguments().getString("tags", null) ?: ""
        viewModel.accept(UiAction.SetSelectedTags(currentSearchTags))
    }

    private fun TagsDetailsBinding.hideAll() {
        generalTagsHeader.isVisible = false
        artistTagsHeader.isVisible = false
        copyrightTagsHeader.isVisible = false
        characterTagsHeader.isVisible = false
        metadataTagsHeader.isVisible = false
        otherTagsHeader.isVisible = false
    }

    private fun PostDetailFragmentBinding.bind() {
        viewModel.post.let { p ->
            sizeTextview.text = "${p.width}x${p.height}"
            sourceUrl.text = p.source
            ratingTextview.text = p.rating.toString()
            scoreTextview.text = "${p.score ?: 0}"
            postedTextview.text = p.createdAt
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.state.collectLatest { state ->
                    Log.d("PostDetailFragment", "state $state")

                    loadingLabel.isVisible = state.tags == null
                    currentSearchTagsHeader.isVisible = false
                    tagDetailsBinding.hideAll()

                    currentSearchTagsHeader.isVisible = !state.includedTags.isNullOrEmpty()
                    currentSearchTagsChipGroup.isVisible =
                        currentSearchTagsHeader.isVisible
                    state.includedTags?.forEach { tag ->
                        val textColor = when (tag.type) {
                            Category.General -> R.color.tag_general
                            Category.Artist -> R.color.tag_artist
                            Category.Copyright -> R.color.tag_copyright
                            Category.Character -> R.color.tag_character
                            Category.Metadata -> R.color.tag_metadata
                            else -> null
                        }

                        val chip = Chip(requireContext())
                        chip.bind(currentSearchTagsChipGroup, textColor, tag)
                    }

                    tagDetailsBinding.blacklistTagsHeader.isVisible =
                        !state.blacklistedTags.isNullOrEmpty()
                    tagDetailsBinding.blacklistTagsChipGroup.isVisible =
                        tagDetailsBinding.blacklistTagsHeader.isVisible
                    state.blacklistedTags?.forEach { tag ->
                        val textColor = when (tag.type) {
                            Category.General -> R.color.tag_general
                            Category.Artist -> R.color.tag_artist
                            Category.Copyright -> R.color.tag_copyright
                            Category.Character -> R.color.tag_character
                            Category.Metadata -> R.color.tag_metadata
                            else -> null
                        }

                        val chip = Chip(requireContext())
                        chip.bind(tagDetailsBinding.blacklistTagsChipGroup, textColor, tag)
                    }

                    state.tags?.groupBy { it.type }?.forEach { (type, tags) ->
                        lateinit var group: ChipGroup
                        lateinit var header: TextView
                        var textColor: Int? = null
                        when (type) {
                            Category.General -> {
                                group = tagDetailsBinding.generalTagsChipGroup
                                header = tagDetailsBinding.generalTagsHeader
                                textColor = R.color.tag_general
                            }
                            Category.Artist -> {
                                group = tagDetailsBinding.artistTagsChipGroup
                                header = tagDetailsBinding.artistTagsHeader
                                textColor = R.color.tag_artist
                            }
                            Category.Copyright -> {
                                group = tagDetailsBinding.copyrightTagsChipGroup
                                header = tagDetailsBinding.copyrightTagsHeader
                                textColor = R.color.tag_copyright
                            }
                            Category.Character -> {
                                group = tagDetailsBinding.characterTagsChipGroup
                                header = tagDetailsBinding.characterTagsHeader
                                textColor = R.color.tag_character
                            }
                            Category.Metadata -> {
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
        }
    }

    private fun Chip.bind(group: ChipGroup, textColor: Int?, tag: TagDetail) {
        Log.d("PostDetailFragment", "$tag")

        this.apply {
            text = tag.toString()
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