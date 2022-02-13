package com.faldez.shachi.ui.post_slide

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
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

    private var topbarMenu: Menu? = null

    private var isAppBarHide = false


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

        val position = requireArguments().getInt("position")
        prepareAppBar()
        prepareViewPager(position)

        return binding.root
    }

    private fun hideAppbar() {
        if (binding.postSlideAppbarLayout.isVisible) {
            binding.postSlideAppbarLayout.animate()
                .translationY(-binding.postSlideAppbarLayout.height.toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        binding.postSlideAppbarLayout.visibility = View.GONE
                    }
                })
        }
    }

    private fun showAppbar() {
        if (!binding.postSlideAppbarLayout.isVisible) {
            binding.postSlideAppbarLayout.animate().translationY(0f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        super.onAnimationStart(animation)
                        binding.postSlideAppbarLayout.visibility = View.VISIBLE
                    }
                })
        }
    }

    private fun prepareViewPager(position: Int) {
        postSlideAdapter = PostSlideAdapter(
            onTap = {
                isAppBarHide = if (isAppBarHide) {
                    showAppbar()
                    false
                } else {
                    hideAppbar()
                    true
                }
                false
            }
        )
        binding.postViewPager.adapter = postSlideAdapter
        lifecycleScope.launch {
            collectPagingData()
        }
        binding.postViewPager.setCurrentItem(position, false)
        binding.postViewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setTitle(position)
                val post = postSlideAdapter.getPostItem(position)
                if (post != null) {
                    setFavoriteButton(post)
                } else {
                    (activity as MainActivity).onBackPressed()
                }
            }
        })
        setTitle(position)
    }

    private fun setTitle(position: Int) {
        (activity as MainActivity).supportActionBar?.title =
            postSlideAdapter.getPostItem(position)?.postId.toString()
    }

    abstract suspend fun collectPagingData()

    private fun setFavoriteButton(post: Post) {
        val (favoriteIcon, favoriteTitle) = if (post.favorite) {
            Pair(R.drawable.ic_baseline_favorite_24, R.string.unfavorite)
        } else {
            Pair(R.drawable.ic_baseline_favorite_border_24, R.string.favorite)
        }

        topbarMenu?.getItem(0)?.apply {
            icon =
                ResourcesCompat.getDrawable(resources,
                    favoriteIcon,
                    requireActivity().theme)
            title = getText(favoriteTitle)
        }


    }

    private fun prepareAppBar() {
        (activity as MainActivity).setSupportActionBar(binding.postSlideTopappbar)
        binding.postSlideAppbarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())
        val supportActionBar = (activity as MainActivity).supportActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.post_slide_menu, menu)
        topbarMenu = menu
        getCurrentPost()?.let { setFavoriteButton(it) }
        Log.d("BasePostSlideFragment", "onCreateOptionsMenu")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                (activity as MainActivity).onBackPressed()
                return true
            }
            R.id.favorite_button -> {
                onFavoriteButton()
                return true
            }
            R.id.detail_button -> {
                getCurrentPost()?.let {
                    navigateToPostSlide(it)
                }
                return true
            }
            R.id.share_button -> {
                getCurrentPost()?.let { post ->
                    val bundle = bundleOf("post" to post)
                    findNavController().navigate(R.id.action_global_to_sharedialog, bundle)
                }
                return true
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
                return true
            }
        }

        return super.onOptionsItemSelected(item)
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

    abstract fun navigateToPostSlide(post: Post?)

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