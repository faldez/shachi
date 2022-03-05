package com.faldez.shachi.util

import android.view.View
import androidx.core.view.isVisible
import com.faldez.shachi.R
import com.faldez.shachi.databinding.TagsDetailsBinding
import com.faldez.shachi.data.model.Category
import com.google.android.material.chip.ChipGroup


fun TagsDetailsBinding.hideAll() {
    generalTagsHeader.isVisible = false
    artistTagsHeader.isVisible = false
    copyrightTagsHeader.isVisible = false
    characterTagsHeader.isVisible = false
    metadataTagsHeader.isVisible = false
    otherTagsHeader.isVisible = false
    blacklistTagsHeader.isVisible = false
}

fun TagsDetailsBinding.clearAllGroup() {
    generalTagsChipGroup.removeAllViews()
    artistTagsChipGroup.removeAllViews()
    copyrightTagsChipGroup.removeAllViews()
    characterTagsChipGroup.removeAllViews()
    metadataTagsChipGroup.removeAllViews()
    otherTagsChipGroup.removeAllViews()
    blacklistTagsChipGroup.removeAllViews()
}

fun TagsDetailsBinding.getGroupHeaderTextColor(type: Category): Triple<ChipGroup, View, Int?> =
    when (type) {
        Category.General -> {
            Triple(generalTagsChipGroup,
                generalTagsHeader,
                R.color.tag_general)
        }
        Category.Artist -> {
            Triple(artistTagsChipGroup,
                artistTagsHeader,
                R.color.tag_artist)
        }
        Category.Copyright -> {
            Triple(copyrightTagsChipGroup,
                copyrightTagsHeader,
                R.color.tag_copyright)
        }
        Category.Character -> {
            Triple(characterTagsChipGroup,
                characterTagsHeader,
                R.color.tag_character)
        }
        Category.Metadata -> {
            Triple(metadataTagsChipGroup,
                metadataTagsHeader,
                R.color.tag_metadata)
        }
        else -> {
            Triple(otherTagsChipGroup,
                otherTagsHeader, null)
        }
    }