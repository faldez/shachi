package com.faldez.bonito.ui.servers

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.faldez.bonito.R
import com.faldez.bonito.databinding.ServersFragmentBinding
import com.faldez.bonito.model.Server
import com.faldez.bonito.model.ServerType

class ServersFragment : Fragment() {

    companion object {
        fun newInstance() = ServersFragment()
    }

    private lateinit var viewModel: ServersViewModel
    private lateinit var binding: ServersFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = ServersFragmentBinding.inflate(inflater, container, false)

        val adapter = ServerListAdapter()
        adapter.setData(listOf(Server(ServerType.Gelbooru, "Gelbooru", "https://safebooru.org")))
        binding.serverListRecyclerview.adapter = adapter
        binding.serverListRecyclerview.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ServersViewModel::class.java)
        // TODO: Use the ViewModel
    }

}