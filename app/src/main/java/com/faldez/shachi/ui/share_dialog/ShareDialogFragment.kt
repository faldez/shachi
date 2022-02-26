package com.faldez.shachi.ui.share_dialog

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.faldez.shachi.database.AppDatabase
import com.faldez.shachi.databinding.ShareDialogFragmentBinding
import com.faldez.shachi.model.Post
import com.faldez.shachi.repository.ServerRepository
import com.faldez.shachi.util.MimeUtil
import com.faldez.shachi.util.glide.GlideApp
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.net.URL

class ShareDialogFragment : BottomSheetDialogFragment() {

    companion object {
        fun newInstance() = ShareDialogFragment()
    }

    private lateinit var viewModel: ShareDialogViewModel
    private lateinit var binding: ShareDialogFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = ShareDialogFragmentBinding.inflate(inflater, container, false)
        binding.bind()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

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

    private fun ShareDialogFragmentBinding.bind() {
        val post = requireArguments().get("post") as Post

        viewModel = ShareDialogViewModel(ServerRepository(AppDatabase.build(requireContext())),
            post.serverId)

        shareAsImageButton.setOnClickListener {
            val mime = MimeUtil.getMimeTypeFromUrl(post.fileUrl)
            if (mime?.startsWith("video") == true) {
                shareVideo(mime, post)
            } else {
                shareImage(mime ?: "image/*", post)
            }
        }

        shareAsLinkButton.setOnClickListener {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT,
                    viewModel.server.value?.toServer()?.getPostUrl(post.postId))
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(intent, null)
            startActivity(shareIntent)
        }

        shareSourceLinkButton.setOnClickListener {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, post.source)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(intent, null)
            startActivity(shareIntent)
        }
    }
}