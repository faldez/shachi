package com.faldez.shachi.ui.post_slide

import android.animation.Animator
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.faldez.shachi.MainActivity
import com.faldez.shachi.R
import com.faldez.shachi.databinding.PostSlideFragmentBinding
import com.faldez.shachi.model.Post
import com.faldez.shachi.ui.base.BaseBrowseViewModel
import com.faldez.shachi.ui.browse.BrowseFragment
import com.faldez.shachi.ui.browse.BrowseViewModel
import com.faldez.shachi.ui.browse.SavedSearchBrowseViewModel
import com.google.android.material.shape.MaterialShapeDrawable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PostSlideFragment : Fragment() {
    companion object {
        const val TAG = "PostSlideFragment"
    }

    private lateinit var postSlideAdapter: PostSlideAdapter

    private lateinit var binding: PostSlideFragmentBinding

    private lateinit var topbarMenu: Menu

    private var isAppBarHide = false

    private lateinit var viewModel: BaseBrowseViewModel

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
        val currentDestinationId = findNavController().currentDestination?.id
        Log.d(BrowseFragment.TAG, "$currentDestinationId ${R.id.browseServerFragment}")
        when (currentDestinationId) {
            R.id.postSlideFragment -> {
                val vm: BrowseViewModel by navGraphViewModels(R.id.nav_graph)
                viewModel = vm
            }
            R.id.savedSearchPostSlideFragment -> {
                val vm: SavedSearchBrowseViewModel by navGraphViewModels(R.id.nav_graph)
                viewModel = vm
            }
        }

        return binding.root
    }

    private fun showSystemUi() {
        val window = (activity as MainActivity).window
        WindowInsetsControllerCompat(window,
            window.decorView).show(WindowInsetsCompat.Type.systemBars())
    }

    private fun hideSystemUi() {
        val window = (activity as MainActivity).window
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun hideAppbar() {
        if (binding.postSlideAppbarLayout.isVisible) {
            binding.postSlideAppbarLayout.animate()
                .translationY(-binding.postSlideAppbarLayout.height.toFloat())
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(p0: Animator?) {}

                    override fun onAnimationEnd(p0: Animator?) {
                        binding.postSlideAppbarLayout.visibility = View.GONE
                    }

                    override fun onAnimationCancel(p0: Animator?) {}

                    override fun onAnimationRepeat(p0: Animator?) {}
                })
        }
    }

    private fun showAppbar() {
        if (!binding.postSlideAppbarLayout.isVisible) {
            binding.postSlideAppbarLayout.animate().translationY(0f)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(p0: Animator?) {
                        binding.postSlideAppbarLayout.visibility = View.VISIBLE
                    }

                    override fun onAnimationEnd(p0: Animator?) {}

                    override fun onAnimationCancel(p0: Animator?) {}

                    override fun onAnimationRepeat(p0: Animator?) {}
                })
        }
    }

    private fun prepareViewPager(position: Int) {
        postSlideAdapter = PostSlideAdapter(
            onTap = {
                isAppBarHide = if (isAppBarHide) {
                    showAppbar()
                    showSystemUi()
                    false
                } else {
                    hideAppbar()
                    hideSystemUi()
                    true
                }
            },
            onLoadStart = {

            },
            onLoadEnd = {
                binding.postLoadingIndicator.isVisible = false
            })
        binding.postViewPager.adapter = postSlideAdapter
        lifecycleScope.launch {
            viewModel.pagingDataFlow.collect(postSlideAdapter::submitData)
        }
        binding.postViewPager.setCurrentItem(position, false)
        binding.postViewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                (activity as MainActivity).supportActionBar?.title =
                    "" + (position + 1) + "/" + postSlideAdapter.itemCount
                postSlideAdapter.getPostItem(position)?.let { setFavoriteButton(it) }

                binding.postLoadingIndicator.isVisible =
                    !postSlideAdapter.loadedPost.contains(position)
            }
        })

        (activity as MainActivity).supportActionBar?.title =
            "" + (position + 1) + "/" + postSlideAdapter.itemCount
    }

    private fun setFavoriteButton(post: Post) {
        if (post.favorite) {
            topbarMenu.getItem(0).icon =
                ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_favorite_24,
                    requireActivity().theme)
        } else {
            topbarMenu.getItem(0).icon =
                ResourcesCompat.getDrawable(resources,
                    R.drawable.ic_baseline_favorite_border_24,
                    requireActivity().theme)
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val position = requireArguments().getInt("position")
        prepareAppBar()
        prepareViewPager(position)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isAppBarHide) {
            showSystemUi()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.post_slide_menu, menu)
        topbarMenu = menu
        postSlideAdapter.getPostItem(binding.postViewPager.currentItem)
            ?.let { setFavoriteButton(it) }
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
                val post = postSlideAdapter.getPostItem(binding.postViewPager.currentItem)
                val bundle = bundleOf("post" to post,
                    "server" to viewModel.state.value.server,
                    "tags" to viewModel.state.value.tags)
                findNavController().navigate(R.id.action_postslide_to_postdetail, bundle)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun onFavoriteButton() {
        postSlideAdapter.getPostItem(binding.postViewPager.currentItem)?.let { post ->
            if (post.favorite) {
                viewModel.deleteFavoritePost(post)
            } else {
                viewModel.favoritePost(post)
            }
            postSlideAdapter.setFavorite(binding.postViewPager.currentItem, !post.favorite)
            postSlideAdapter.getPostItem(binding.postViewPager.currentItem)?.let { post ->
                setFavoriteButton(post)
            }
        }
    }
}