package com.faldez.shachi.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.faldez.shachi.MainActivity
import com.faldez.shachi.R
import com.faldez.shachi.databinding.SettingsFragmentBinding
import com.google.android.material.shape.MaterialShapeDrawable

class SettingsFragment : Fragment() {
    private lateinit var binding: SettingsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = SettingsFragmentBinding.inflate(inflater, container, false)
        (activity as MainActivity).supportFragmentManager.beginTransaction()
            .replace(R.id.settingsFrameLayout, RootSettingsFragment()).commit()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareAppBar()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.settingsFrameLayout.updatePadding(bottom = systemBars.bottom)
            insets
        }
    }

    private fun prepareAppBar() {
        binding.settingsAppbarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())
        binding.settingsTopappbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.settingsTopappbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }
}