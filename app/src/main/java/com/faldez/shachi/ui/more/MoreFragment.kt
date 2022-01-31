package com.faldez.shachi.ui.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.faldez.shachi.MainActivity
import com.faldez.shachi.R
import com.faldez.shachi.databinding.MoreFragmentBinding
import com.google.android.material.shape.MaterialShapeDrawable

class MoreFragment : Fragment() {

    companion object {
        fun newInstance() = MoreFragment()
    }

    private lateinit var binding: MoreFragmentBinding
    private lateinit var viewModel: MoreViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = MoreFragmentBinding.inflate(inflater, container, false)

        (activity as MainActivity).supportFragmentManager.beginTransaction()
            .replace(R.id.moreFrameLayout, MoreSettingsFragment()).commit()
        prepareAppBar()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MoreViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> {
                (activity as MainActivity).onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun prepareAppBar() {
        (activity as MainActivity).setSupportActionBar(binding.moreTopappbar)
        binding.moreAppbarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())
        val supportActionBar = (activity as MainActivity).supportActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }
}