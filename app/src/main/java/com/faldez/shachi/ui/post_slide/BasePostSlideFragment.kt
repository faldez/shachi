package com.faldez.shachi.ui.post_slide

import android.animation.Animator
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.faldez.shachi.GlideApp
import com.faldez.shachi.MainActivity
import com.faldez.shachi.R
import com.faldez.shachi.databinding.PostSlideFragmentBinding
import com.faldez.shachi.model.Post
import com.google.android.material.shape.MaterialShapeDrawable
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
                    false
                } else {
                    hideAppbar()
                    true
                }
                false
            },
            onDoubleTap = {
                onFavoriteButton()
                false
            },
            onLoadStart = {

            },
            onLoadEnd = {
                binding.postLoadingIndicator.isVisible = false
            },
            onLoadError = {
                binding.postLoadingIndicator.isVisible = false
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

                binding.postLoadingIndicator.isVisible =
                    !postSlideAdapter.loadedPost.contains(position)
            }
        })
        setTitle(position)
    }

    private fun setTitle(position: Int) {
        (activity as MainActivity).supportActionBar?.title =
            "" + (position + 1) + "/" + postSlideAdapter.itemCount
    }

    abstract suspend fun collectPagingData()

    private fun setFavoriteButton(post: Post) {
        val icon = if (post.favorite) {
            R.drawable.ic_baseline_favorite_24
        } else {
            R.drawable.ic_baseline_favorite_border_24
        }

        topbarMenu.apply {
            Log.d("BasePostSlideFragment", "setFavoriteButton ${post.favorite}")
            this?.getItem(0)?.icon =
                ResourcesCompat.getDrawable(resources,
                    icon,
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.post_slide_menu, menu)
        topbarMenu = menu
        postSlideAdapter.getPostItem(binding.postViewPager.currentItem)
            ?.let { setFavoriteButton(it) }
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
                postSlideAdapter.getPostItem(binding.postViewPager.currentItem)?.let {
                    navigateToPostSlide(it)
                }
                return true
            }
            R.id.share_button -> {
                postSlideAdapter.getPostItem(binding.postViewPager.currentItem)?.let { post ->
                    GlideApp.with(requireContext()).asBitmap().load(post.fileUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?,
                            ) {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_STREAM,
                                        Uri.parse(MediaStore.Images.Media.insertImage(requireContext().contentResolver,
                                            resource,
                                            post.md5,
                                            null)))
                                    type = "image/*"
                                }

                                startActivity(Intent.createChooser(shareIntent, null))
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {}
                        })

                    return true
                }

            }
        }

        return super.onOptionsItemSelected(item)
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