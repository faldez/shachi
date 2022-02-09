package com.faldez.shachi.ui.share_dialog

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.faldez.shachi.databinding.ShareDialogFragmentBinding
import com.faldez.shachi.model.Post
import com.faldez.shachi.util.glide.GlideApp
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.modernstorage.storage.AndroidFileSystem
import java.io.File
import java.io.FileOutputStream
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

//    private fun downloadFile(fileUrl: String): Uri? {
//        val fileUri = Uri.parse(fileUrl)
//        val resolver = requireContext().contentResolver
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            val contentValues = ContentValues().apply {
//                put(MediaStore.MediaColumns.DISPLAY_NAME, fileUri.lastPathSegment)
//                put(MediaStore.MediaColumns.MIME_TYPE, "image/*")
//            }
//            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
//        } else {
//            val authority = "${requireContext().packageName}.provider"
//            val destination = File(Environment.getDownloadCacheDirectory().toURI())
//            FileProvider.getUriForFile(requireContext(), authority, destination)
//        }?.also { uri ->
//            URL(fileUrl).openStream().use { input ->
//                FileOutputStream(File(uri.toString())).use { output ->
//                    input.copyTo(output)
//                }
//            }
//        }
//    }

    private fun shareImage(post: Post) {
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
    }

    private fun ShareDialogFragmentBinding.bind() {
        val post = requireArguments().get("post") as Post
        shareAsImageButton.setOnClickListener {
            shareImage(post)
        }

        shareAsLinkButton.setOnClickListener {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, post.fileUrl)
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ShareDialogViewModel::class.java)
        // TODO: Use the ViewModel
    }

}