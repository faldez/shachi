package com.faldez.shachi.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.faldez.shachi.MainActivity
import com.faldez.shachi.R
import com.faldez.shachi.databinding.AboutFragmentBinding
import com.faldez.shachi.databinding.MoreFragmentBinding

class AboutFragment : Fragment() {
    private lateinit var binding: AboutFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = AboutFragmentBinding.inflate(inflater, container, false)

        (activity as MainActivity).supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, AboutSettingsFragment()).commit()
        return binding.root
    }
}