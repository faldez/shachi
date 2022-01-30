package com.faldez.sachi.ui.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.faldez.sachi.MainActivity
import com.faldez.sachi.R
import com.faldez.sachi.databinding.MoreFragmentBinding

class MoreFragment : Fragment() {

    companion object {
        fun newInstance() = MoreFragment()
    }

    private lateinit var binding: MoreFragmentBinding
    private lateinit var viewModel: MoreViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = MoreFragmentBinding.inflate(inflater, container, false)

        (activity as MainActivity).supportFragmentManager.beginTransaction()
            .replace(R.id.moreFrameLayout, MoreSettingsFragment()).commit()

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MoreViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showBottomNavigation()
    }
}