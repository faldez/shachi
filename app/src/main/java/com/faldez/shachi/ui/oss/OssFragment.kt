package com.faldez.shachi.ui.oss

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.faldez.shachi.MainActivity
import com.faldez.shachi.R
import com.faldez.shachi.databinding.OssFragmentBinding
import com.google.android.material.shape.MaterialShapeDrawable
import com.mikepenz.aboutlibraries.LibsBuilder

class OssFragment : Fragment() {

    companion object {
        fun newInstance() = OssFragment()
    }

    private lateinit var binding: OssFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = OssFragmentBinding.inflate(inflater, container, false)
        val fragment = LibsBuilder()
            .withVersionShown(true)
            .withLicenseShown(true)
            .withLicenseDialog(true)
            .supportFragment()

        (activity as MainActivity).supportFragmentManager.beginTransaction()
            .replace(R.id.moreFrameLayout, fragment).commit()
        prepareAppBar()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
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