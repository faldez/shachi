package com.faldez.shachi.ui.servers

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.faldez.shachi.R
import com.faldez.shachi.data.database.AppDatabase
import com.faldez.shachi.data.model.Server
import com.faldez.shachi.data.repository.ServerRepository
import com.faldez.shachi.data.repository.server.ServerRepositoryImpl
import com.faldez.shachi.databinding.ServersFragmentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.MaterialShapeDrawable
import kotlinx.coroutines.launch

class ServersFragment : Fragment() {

    private val viewModel: ServersViewModel by
    navGraphViewModels(R.id.nav_graph) {
        ServersViewModelFactory(ServerRepositoryImpl(AppDatabase.build(requireContext())), this)
    }
    private lateinit var binding: ServersFragmentBinding
    private lateinit var adapter: ServerListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = ServersFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                    url = serverWithSelected.url,
                    username = serverWithSelected.username,
                    password = serverWithSelected.password
                )
                val bundle = bundleOf("server" to server)
                findNavController().navigate(R.id.action_servers_to_serveredit, bundle)
            },
            onDelete = {
                val server = adapter.serverList.get(it)
                MaterialAlertDialogBuilder(requireContext()).setTitle("Delete " + server.title)
                    .setMessage("Are you Sure? Saved searches, favorite, and search history for this server will be deleted")
                    .setPositiveButton("Yes"
                    ) { _, _ -> viewModel.delete(server) }
                    .setNegativeButton("No", null).show()
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

        binding.newServerButton.setOnClickListener {
            findNavController().navigate(R.id.action_servers_to_serveredit)
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = systemBars.bottom)
            insets
        }

        lifecycleScope.launch {
            viewModel.serverList.collect {
                Log.d("ServersFragment", "$it")
                adapter.setData(it ?: listOf())
            }
        }
    }

    private fun prepareAppBar() {
        binding.serversAppbarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())
        binding.serversTopappbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.serversTopappbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }
}