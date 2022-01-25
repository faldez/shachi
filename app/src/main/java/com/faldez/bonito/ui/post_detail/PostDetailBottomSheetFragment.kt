package com.faldez.bonito.ui.post_detail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import com.faldez.bonito.MainActivity
import com.faldez.bonito.R
import com.faldez.bonito.databinding.PostDetailBottomSheetFragmentBinding
import com.faldez.bonito.model.Post
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.gson.Gson
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class PostDetailBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: PostDetailBottomSheetFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = PostDetailBottomSheetFragmentBinding.inflate(inflater, container, false)
        val gson = Gson()
        val postStr = arguments?.getString("post")
        Log.d("PostDetailBottomSheetFragment", "$postStr")
        val post = gson.fromJson(postStr, Post::class.java)
        binding.sizeTextview.text = "${post.width}x${post.height}"
        binding.sourceUrl.text = post?.source
        binding.ratingTextview.text = post.rating
        binding.scoreTextview.text = "${post.score ?: 0}"
        binding.postedTextview.text = post.createdAt
        post.tags.trim().split(" ").forEach {
            val chip = Chip(requireContext())
            chip.setEnsureMinTouchTargetSize(false)
            chip.text = it
            binding.tagListChipgroup.addView(chip)
        }

        return binding.root
    }
}