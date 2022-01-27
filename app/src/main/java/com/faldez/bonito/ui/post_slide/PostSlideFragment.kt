package com.faldez.bonito.ui.post_slide

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.viewpager2.widget.ViewPager2.*
import com.faldez.bonito.MainActivity
import com.faldez.bonito.R
import com.faldez.bonito.data.FavoriteRepository
import com.faldez.bonito.data.PostRepository
import com.faldez.bonito.data.ServerRepository
import com.faldez.bonito.database.AppDatabase
import com.faldez.bonito.databinding.PostSlideFragmentBinding
import com.faldez.bonito.model.Post
import com.faldez.bonito.service.BooruService
import com.faldez.bonito.ui.browse_server.BrowseServerViewModel
import com.faldez.bonito.ui.browse_server.BrowseServerViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.gson.Gson
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PostSlideFragment : Fragment() {
    companion object {
        const val TAG = "PostSlideFragment"
    }

    private lateinit var postSlideAdapter: PostSlideAdapter

    private lateinit var binding: PostSlideFragmentBinding

    private lateinit var topbarMenu: Menu


    private val viewModel: BrowseServerViewModel by
    navGraphViewModels(R.id.nav_graph) {
        val db = AppDatabase.build(requireContext())
        val favoriteRepository = FavoriteRepository(db)
        BrowseServerViewModelFactory(PostRepository(BooruService()),
            ServerRepository(db),
            favoriteRepository,
            this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = PostSlideFragmentBinding.inflate(inflater, container, false)


        return binding.root
    }

    private fun prepareViewPager(position: Int) {
        postSlideAdapter = PostSlideAdapter()
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
        (activity as MainActivity).findViewById<BottomNavigationView>(R.id.bottomNavigationView).visibility =
            View.VISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.post_slide_menu, menu)
        topbarMenu = menu
        postSlideAdapter.getPostItem(binding.postViewPager.currentItem)?.let { setFavoriteButton(it) }
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
                val gson = Gson()
                val post = postSlideAdapter.getPostItem(binding.postViewPager.currentItem)
                Log.d(TAG, "$post")
                val bundle = bundleOf("post" to gson.toJson(post))
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