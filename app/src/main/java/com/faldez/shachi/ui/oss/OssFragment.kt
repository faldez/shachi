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
    private lateinit var binding: OssFragmentBinding

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareAppBar()
    }

    private fun prepareAppBar() {
        binding.moreAppbarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())
        binding.moreTopappbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.moreTopappbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }
}