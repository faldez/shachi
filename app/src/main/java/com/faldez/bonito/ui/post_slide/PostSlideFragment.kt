package com.faldez.bonito.ui.post_slide

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.faldez.bonito.MainActivity
import com.faldez.bonito.R
import com.faldez.bonito.data.GelbooruRepository
import com.faldez.bonito.databinding.PostSlideFragmentBinding
import com.faldez.bonito.model.Post
import com.faldez.bonito.service.GelbooruService
import com.faldez.bonito.ui.search_post.SearchPostViewModel
import com.faldez.bonito.ui.search_post.SearchPostViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.gson.Gson
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

class PostSlideFragment : Fragment() {
    companion object {
        const val TAG = "PostSlideFragment"
    }

    private lateinit var postSlideAdapter: PostSlideAdapter

    private lateinit var binding: PostSlideFragmentBinding

    private val gelbooruService = GelbooruService.getInstance("https://safebooru.org")

    private val viewModel: SearchPostViewModel by
    navGraphViewModels(R.id.nav_graph) {
        SearchPostViewModelFactory(GelbooruRepository(gelbooruService), this)
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

        val view = binding.root

        val position = arguments!!.getInt("position")

        (activity as MainActivity).setSupportActionBar(binding.postSlideTopappbar)
        binding.postSlideAppbarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())


        postSlideAdapter = PostSlideAdapter()
        binding.postViewPager.adapter = postSlideAdapter
        lifecycleScope.launch {
            viewModel.pagingDataFlow.collect(postSlideAdapter::submitData)
        }

        binding.postViewPager.setCurrentItem(position, false)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).findViewById<BottomNavigationView>(R.id.bottomNavigationView).visibility =
            View.VISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.post_slide_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.favorite_button -> {
                true
            }
            R.id.detail_button -> {
                val gson = Gson()
                val post = postSlideAdapter.getPostItem(binding.postViewPager.currentItem)
                Log.d(TAG, "$post")
                val bundle = bundleOf("post" to gson.toJson(post))
                findNavController().navigate(R.id.action_postslide_to_postdetail, bundle)
                true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}