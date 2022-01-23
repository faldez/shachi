package com.faldez.bonito.ui.post_slide

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.viewpager2.widget.ViewPager2.*
import com.faldez.bonito.MainActivity
import com.faldez.bonito.R
import com.faldez.bonito.data.Repository
import com.faldez.bonito.databinding.PostSlideFragmentBinding
import com.faldez.bonito.service.BooruService
import com.faldez.bonito.service.GelbooruService
import com.faldez.bonito.ui.search_post.SearchPostViewModel
import com.faldez.bonito.ui.search_post.SearchPostViewModelFactory
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


    private val viewModel: SearchPostViewModel by
    navGraphViewModels(R.id.nav_graph) {
        SearchPostViewModelFactory(Repository(BooruService()), this)
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

        prepareAppBar()

        val view = binding.root

        val position = arguments!!.getInt("position")

        prepareViewPager(position)

        return view
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
            }
        })
        (activity as MainActivity).supportActionBar?.title =
            "" + (position + 1) + "/" + postSlideAdapter.itemCount
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
            android.R.id.home -> {
                (activity as MainActivity).onBackPressed()
                return true
            }
            R.id.favorite_button -> {
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
}