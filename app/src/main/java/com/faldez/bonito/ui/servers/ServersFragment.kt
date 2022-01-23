package com.faldez.bonito.ui.servers

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.faldez.bonito.MainActivity
import com.faldez.bonito.R
import com.faldez.bonito.database.AppDatabase
import com.faldez.bonito.databinding.ServersFragmentBinding
import com.faldez.bonito.model.Server
import com.faldez.bonito.model.ServerType
import com.google.android.material.shape.MaterialShapeDrawable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ServersFragment : Fragment() {

    companion object {
        fun newInstance() = ServersFragment()
    }

    private val viewModel: ServersViewModel by
    navGraphViewModels(R.id.nav_graph) {
        ServersViewModelFactory(AppDatabase.build(requireContext()), this)
    }
    private lateinit var binding: ServersFragmentBinding
    private lateinit var adapter: ServerListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = ServersFragmentBinding.inflate(inflater, container, false)
        prepareAppBar()
        adapter = ServerListAdapter()
        binding.serverListRecyclerview.adapter = adapter
        binding.serverListRecyclerview.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        lifecycleScope.launch {
            viewModel.serverList.distinctUntilChanged().collect {
                adapter.setData(it ?: listOf())
            }
        }
    }

    private fun prepareAppBar() {
        (activity as MainActivity).setSupportActionBar(binding.serversTopappbar)
        binding.serversAppbarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())
        val supportActionBar = (activity as MainActivity).supportActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.servers_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> {
                (activity as MainActivity).onBackPressed()
                return true
            }
            R.id.new_server_button -> {
                findNavController().navigate(R.id.action_servers_to_serveredit)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}