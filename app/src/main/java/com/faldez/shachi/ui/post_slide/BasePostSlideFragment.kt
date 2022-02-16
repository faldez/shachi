package com.faldez.shachi.ui.post_slide

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.faldez.shachi.MainActivity
import com.faldez.shachi.R
import com.faldez.shachi.databinding.PostSlideFragmentBinding
import com.faldez.shachi.model.Post
import com.faldez.shachi.service.DownloadService
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch


abstract class BasePostSlideFragment : Fragment() {
    protected lateinit var postSlideAdapter: PostSlideAdapter
    protected lateinit var binding: PostSlideFragmentBinding

    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = PostSlideFragmentBinding.inflate(inflater, container, false)

        val quality = preferences.getString("detail_quality", null) ?: "sample"
        postSlideAdapter = PostSlideAdapter(
            quality,
            onTap = {
                getCurrentPost()?.let {
                    navigateToPostDetail(it)
                }
            }
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareAppBar()

        val position = requireArguments().getInt("position")
        Log.d("BasePostSlideFragment/onViewCreated", "position $position")
        binding.postViewPager.bind(position)
    }

    private fun ViewPager2.bind(position: Int) {
        val questionableFilter =
            preferences.getString("filter_questionable_content", null) ?: "disable"
        val explicitFilter = preferences.getString("filter_explicit_content", null) ?: "disable"

        adapter = postSlideAdapter
        lifecycleScope.launch {
            collectPagingData(
                showQuestionable = questionableFilter != "mute",
                showExplicit = explicitFilter != "mute"
            )
        }
        setCurrentItem(position, false)
        registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setAppBar(position)
            }
        })
        setAppBar(position)
    }

    private fun setAppBar(position: Int) {
        postSlideAdapter.getPostItem(position)?.let {
            binding.postSlideTopappbar.title =
                postSlideAdapter.getPostItem(position)?.postId.toString()
            setFavoriteButton(it)
        }
    }

    abstract suspend fun collectPagingData(showQuestionable: Boolean, showExplicit: Boolean)

    private fun setFavoriteButton(post: Post) {
        val (favoriteIcon, favoriteTitle) = if (post.favorite) {
            Pair(R.drawable.ic_baseline_favorite_24, R.string.unfavorite)
        } else {
            Pair(R.drawable.ic_baseline_favorite_border_24, R.string.favorite)
        }

        binding.postSlideTopappbar.menu.findItem(R.id.favorite_button)?.apply {
            icon =
                ResourcesCompat.getDrawable(resources,
                    favoriteIcon,
                    requireActivity().theme)
            title = getText(favoriteTitle)
        }


    }

    private fun prepareAppBar() {
        binding.postSlideAppbarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())

        binding.postSlideTopappbar.menu.clear()
        binding.postSlideTopappbar.inflateMenu(R.menu.post_slide_menu)
        binding.postSlideTopappbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.postSlideTopappbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        getCurrentPost()?.let { setFavoriteButton(it) }
        binding.postSlideTopappbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                android.R.id.home -> {
                    (activity as MainActivity).onBackPressed()
                    true
                }
                R.id.favorite_button -> {
                    onFavoriteButton()
                    true
                }
                R.id.detail_button -> {
                    getCurrentPost()?.let {
                        navigateToPostDetail(it)
                    }
                    true
                }
                R.id.share_button -> {
                    getCurrentPost()?.let { post ->
                        val bundle = bundleOf("post" to post)
                        findNavController().navigate(R.id.action_global_to_sharedialog, bundle)
                    }
                    true
                }
                R.id.download_button -> {
                    getCurrentPost()?.let { post ->
                        val downloadDir = getDownloadDir()
                        if (downloadDir != null) {
                            showSnackbar(R.string.downloading)
                            downloadFile(downloadDir, post)
                        } else {
                            showSnackbar(R.string.download_path_not_set)
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun getCurrentPost(): Post? =
        postSlideAdapter.getPostItem(binding.postViewPager.currentItem)

    private fun getDownloadDir(): String? {
        return PreferenceManager.getDefaultSharedPreferences(requireContext())
            .getString("download_path", null)
    }

    private fun showSnackbar(text: Int) {
        Snackbar.make(requireView(), text, Snackbar.LENGTH_SHORT)
            .show()
    }

    private fun downloadFile(downloadDir: String, post: Post) {
        Intent(requireContext(), DownloadService::class.java).also { intent ->
            val bundle = bundleOf("download_dir" to downloadDir, "post" to post)
            intent.putExtras(bundle)
            requireContext().startService(intent)
        }
    }

    abstract fun navigateToPostDetail(post: Post?)

    private fun onFavoriteButton() {
        val currentItem = binding.postViewPager.currentItem
        postSlideAdapter.getPostItem(currentItem)?.let { post ->
            Log.d("BasePostSlideFragment", "$post")
            if (post.favorite) {
                deleteFavoritePost(post)
            } else {
                favoritePost(post)
            }
            postSlideAdapter.setFavorite(currentItem, !post.favorite)
            setFavoriteButton(post)
        }
    }

    abstract fun deleteFavoritePost(post: Post)

    abstract fun favoritePost(post: Post)
}