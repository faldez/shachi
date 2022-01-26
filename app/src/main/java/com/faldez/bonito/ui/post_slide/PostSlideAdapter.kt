package com.faldez.bonito.ui.post_slide

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.faldez.bonito.databinding.PostSlideItemBinding
import com.faldez.bonito.model.Post

class PostSlideAdapter :
    PagingDataAdapter<Post, PostSlideViewHolder>(POST_COMPARATOR) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostSlideViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val binding = PostSlideItemBinding.inflate(inflater, parent, false)
        return PostSlideViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostSlideViewHolder, position: Int) {
        val post = getItem(position)
        post?.let {
            val postImageView = holder.binding.postImageView
            val circularProgress = CircularProgressDrawable(postImageView.context)
            circularProgress.strokeWidth = 5f
            circularProgress.centerRadius = 30f
            circularProgress.start()
            val requestOptions =
                RequestOptions().placeholder(circularProgress)
            Glide.with(postImageView.context).load(it.fileUrl)
                .thumbnail(Glide.with(postImageView.context).load(it.previewUrl)
                    .apply(requestOptions))
                .apply(requestOptions)
                .into(postImageView)
        }
    }

    fun getPostItem(position: Int): Post? {
        return getItem(position)
    }

    fun setFavorite(position: Int, favorite: Boolean) {
        val item = getItem(position)
        item?.let {
            it.favorite = favorite
            notifyItemChanged(position)
        }
    }

    companion object {
        private val POST_COMPARATOR = object : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem.postId == newItem.postId && oldItem.serverUrl == newItem.serverUrl


            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem == newItem

        }
    }
}

class PostSlideViewHolder(val binding: PostSlideItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

}
