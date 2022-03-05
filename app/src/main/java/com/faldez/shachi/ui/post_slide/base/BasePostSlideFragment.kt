package com.faldez.shachi.ui.post_slide.base

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.faldez.shachi.MainActivity
import com.faldez.shachi.R
import com.faldez.shachi.databinding.PostSlideFragmentBinding
import com.faldez.shachi.data.model.Post
import com.faldez.shachi.service.DownloadService
import com.faldez.shachi.ui.comment.CommentFragment
import com.faldez.shachi.ui.post_detail.PostDetailFragment
import com.faldez.shachi.ui.post_slide.PostSlideAdapter
import com.faldez.shachi.util.MimeUtil
import com.faldez.shachi.util.glide.GlideApp
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.net.URL


abstract class BasePostSlideFragment : Fragment() {

    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
    }

    protected val postSlideAdapter: PostSlideAdapter by lazy {
        val quality = preferences.getString("detail_quality", null) ?: "sample"

        PostSlideAdapter(
            quality,
            onTap = {
                onPhotoTap()
            }
        )
    }

    protected lateinit var binding: PostSlideFragmentBinding
    private var isToolbarHide: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = PostSlideFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareAppBar()

        val position = requireArguments().getInt("position")
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
                onPageChange(position)
            }
        })
        setAppBar(position)
        onPageChange(position)
    }

    open fun onPageChange(position: Int) {}

    private fun setAppBar(position: Int) {
        postSlideAdapter.getPostItem(position)?.let {
            binding.postSlideTopappbar.title =
                postSlideAdapter.getPostItem(position)?.postId.toString()
            setFavoriteButton(it)
        }
    }

    private fun onPhotoTap() {
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(requireActivity().window.decorView) ?: return
        // Configure the behavior of the hidden system bars
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        isToolbarHide = if (isToolbarHide) {
            showAppBar()
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())

            val value = TypedValue()
            context?.theme?.resolveAttribute(R.attr.colorSurface, value, true)
            binding.postSlideLayout.setBackgroundColor(value.data)
            false
        } else {
            hideAppBar()
            // Hide both the status bar and the navigation bar
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

            binding.postSlideLayout.setBackgroundColor(resources.getColor(android.R.color.black,
                null))
            true
        }
    }

    abstract suspend fun collectPagingData(showQuestionable: Boolean, showExplicit: Boolean)

    private fun setFavoriteButton(post: Post) {
        binding.postSlideTopappbar.menu.findItem(R.id.favorite_action_button)?.apply {
            if (post.favorite) {
                icon = ResourcesCompat.getDrawable(resources,
                    R.drawable.ic_baseline_favorite_24,
                    requireActivity().theme)
                title = resources.getText(R.string.unfavorite)
            } else {
                icon = ResourcesCompat.getDrawable(resources,
                    R.drawable.ic_baseline_favorite_border_24,
                    requireActivity().theme)
                title = resources.getText(R.string.favorite)
            }
        }
    }

    private fun prepareAppBar() {
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
                R.id.favorite_action_button -> {
                    onFavoriteButton()
                    true
                }
                R.id.detail_action_button -> {
                    onDetailButton()
                    true
                }
                R.id.download_action_button -> {
                    onDownloadButton()
                    true
                }
                R.id.select_image_button -> {
                    onSelectImage()
                    true
                }
                R.id.share_image_button -> {
                    getCurrentPost()?.let { post ->
                        val mime = MimeUtil.getMimeTypeFromUrl(post.fileUrl)
                        if (mime?.startsWith("video") == true) {
                            shareVideo(mime, post)
                        } else {
                            shareImage(mime ?: "image/*", post)
                        }
                    }
                    true
                }
                R.id.share_link_button -> {
                    getCurrentPost()?.let { post ->
                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, post.postUrl)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(intent, null)
                        startActivity(shareIntent)
                    }
                    true
                }
                R.id.share_source_link_button -> {
                    getCurrentPost()?.let { post ->
                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, post.source)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(intent, null)
                        startActivity(shareIntent)
                    }
                    true
                }
                R.id.comment_button -> {
                    getCurrentPost()?.let {
                        val bundle = bundleOf("server_id" to it.serverId, "post_id" to it.postId)
                        val commentDialog = CommentFragment()
                        commentDialog.arguments = bundle
                        commentDialog.show(requireActivity().supportFragmentManager, "dialog")
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun onDetailButton() {
        getCurrentPost()?.let {
            val bundle = navigateToPostDetailBundle(it)
            if (!resources.getBoolean(R.bool.isTablet)) {
                findNavController().navigate(R.id.action_postslide_to_postdetail, bundle)
            } else {
                val postDetailDialog = PostDetailFragment()
                postDetailDialog.arguments = bundle
                postDetailDialog.show(requireActivity().supportFragmentManager, "dialog")
            }
        }
    }

    private fun onDownloadButton() {
        getCurrentPost()?.let { post ->
            val downloadDir = getDownloadDir()
            if (downloadDir != null) {
                showSnackbar(R.string.downloading)
                downloadFile(downloadDir, post)
            } else {
                showSnackbar(R.string.download_path_not_set)
            }
        }
    }

    private fun onSelectImage() {
        getCurrentPost()?.let { post ->
            val items = ArrayList<String>()
            if (post.previewUrl != null) {
                if (post.previewWidth != null && post.previewHeight != null) {
                    items.add("Preview: ${post.previewWidth}x${post.previewHeight}")
                } else {
                    items.add("Preview")
                }
            }

            if (post.sampleUrl != null) {
                if (post.sampleWidth != null && post.sampleHeight != null) {
                    items.add("Sample: ${post.sampleWidth}x${post.sampleHeight}")
                } else {
                    items.add("Sample")
                }
            }

            items.add("Original: ${post.width}x${post.height}")

            val checked = items.indexOfFirst {
                it.substringBefore(":").lowercase() == post.quality ?: postSlideAdapter.quality
            }

            Log.d("BasePostSlideFragment/onSelectImage", "$post $items")

            MaterialAlertDialogBuilder(requireContext()).setTitle("Select Image")
                .setSingleChoiceItems(items.toTypedArray(), checked) { dialog, choice ->
                    postSlideAdapter.setPostQuality(binding.postViewPager.currentItem,
                        items[choice].substringBefore(":").lowercase())
                    dialog.dismiss()
                }.show()
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

    abstract fun navigateToPostDetailBundle(post: Post?): Bundle

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

    private fun hideAppBar() {
        binding.postSlideAppbarLayout.hide()
    }

    private fun showAppBar() {
        binding.postSlideAppbarLayout.show()
    }

    private fun AppBarLayout.hide() {
        if (!isToolbarHide) animate().alpha(0f)
    }

    private fun AppBarLayout.show() {
        if (isToolbarHide) animate().alpha(1f)
    }

    abstract fun deleteFavoritePost(post: Post)

    abstract fun favoritePost(post: Post)

    private fun shareImage(mime: String, post: Post) {
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
                        type = mime
                    }

                    startActivity(Intent.createChooser(shareIntent, null))
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun shareVideo(mime: String, post: Post) {
        val fileUrl = post.fileUrl
        val filename = Uri.parse(fileUrl).lastPathSegment!!

        val videoPath = File(context?.filesDir, "videos")
        videoPath.mkdirs()
        val newFile = File(videoPath, filename)
        newFile.createNewFile()

        CoroutineScope(Dispatchers.IO).launch {
            URL(fileUrl).openStream().use { input ->
                context?.contentResolver?.openOutputStream(newFile.toUri())?.use { output ->
                    input.copyTo(output)
                }
                val contentUri: Uri =
                    FileProvider.getUriForFile(requireContext(),
                        "com.faldez.shachi.fileprovider",
                        newFile)
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    type = mime
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                startActivity(Intent.createChooser(shareIntent, null))
            }
        }
    }
}