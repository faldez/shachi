package com.faldez.sachi.ui.servers

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.faldez.sachi.MainActivity
import com.faldez.sachi.R
import com.faldez.sachi.data.ServerRepository
import com.faldez.sachi.database.AppDatabase
import com.faldez.sachi.databinding.ServersFragmentBinding
import com.faldez.sachi.model.Server
import com.google.android.material.shape.MaterialShapeDrawable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ServersFragment : Fragment() {

    private val viewModel: ServersViewModel by
    navGraphViewModels(R.id.nav_graph) {
        ServersViewModelFactory(ServerRepository(AppDatabase.build(requireContext())), this)
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
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        prepareAppBar()

        adapter = ServerListAdapter(
            onTap = {
                viewModel.insert(it)
            },
            onEdit = {
                val serverWithSelected = adapter.serverList.get(it)
                val server = Server(
                    serverId = serverWithSelected.serverId,
                    type = serverWithSelected.type,
                    title = serverWithSelected.title,
                    url = serverWithSelected.url
                )
                val bundle = bundleOf("server" to server)
                findNavController().navigate(R.id.action_servers_to_serveredit, bundle)
            },
            onDelete = {
                val server = adapter.serverList.get(it)
                viewModel.delete(server)
            })
        binding.serverListRecyclerview.adapter = adapter
        binding.serverListRecyclerview.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.serverListRecyclerview.addOnItemTouchListener(object :
            RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
            }

        })

        lifecycleScope.launch {
            viewModel.serverList.collect {
                Log.d("ServersFragment", "$it")
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