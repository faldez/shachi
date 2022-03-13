package com.faldez.shachi.ui.blacklisted_tag

import android.content.DialogInterface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.faldez.shachi.R
import com.faldez.shachi.data.database.AppDatabase
import com.faldez.shachi.data.model.BlacklistedTag
import com.faldez.shachi.data.model.BlacklistedTagWithServer
import com.faldez.shachi.data.repository.BlacklistTagRepository
import com.faldez.shachi.data.repository.ServerRepository
import com.faldez.shachi.databinding.BlacklistedTagEditDialogBinding
import com.faldez.shachi.databinding.BlacklistedTagFragmentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.MaterialShapeDrawable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BlacklistedTagFragment : Fragment() {

    private lateinit var binding: BlacklistedTagFragmentBinding
    private lateinit var viewModel: BlacklistedTagViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.build(requireContext())
        viewModel = ViewModelProvider(this,
            BlacklistedTagViewModelFactory(ServerRepository(db), BlacklistTagRepository(db))).get(
            BlacklistedTagViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = BlacklistedTagFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareAppBar()

        binding.bind()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = systemBars.bottom)
            insets
        }
    }

    private fun prepareAppBar() {
        binding.blacklistedTagAppbarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())
        binding.blacklistedTagToolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.blacklistedTagToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun BlacklistedTagFragmentBinding.bind() {
        val adapter = BlacklistedTagAdapter(
            onEdit = { position ->
                val servers =
                    viewModel.serverList.value ?: listOf()
                val item = viewModel.blacklistedTagFlow.value?.get(position)
                val selectedServer = servers.map { server ->
                    item?.servers?.indexOfFirst { it.serverId == server.serverId } != -1
                }.toMutableList()

                Log.d("Dialog", "$servers $selectedServer")
                val dialogBinding =
                    BlacklistedTagEditDialogBinding.inflate(LayoutInflater.from(requireContext()))
                dialogBinding.blacklistedTagsInput.text =
                    SpannableStringBuilder(item?.blacklistedTag?.tags)
                val serverAdapter =
                    BlacklistedTagServerAdapter(servers, selectedServer)
                dialogBinding.serverListView.adapter = serverAdapter
                dialogBinding.serverListView.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Blacklist tags")
                    .setView(dialogBinding.root)
                    .setPositiveButton("Save", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            val tags = dialogBinding.blacklistedTagsInput.text.toString()
                            val selectedServers = serverAdapter.getSelected()
                            val data =
                                item?.copy(blacklistedTag = item.blacklistedTag.copy(tags = tags),
                                    servers = selectedServers)
                            Log.d("Dialog", "$data")
                            data?.let {
                                viewModel.insertBlacklistTag(it)
                            }
                        }

                    })
                    .show()
            },
            onDelete = { position ->
                val item = viewModel.blacklistedTagFlow.value?.get(position)
                item?.blacklistedTag?.let {
                    viewModel.deleteBlacklistTag(it)
                }
            }
        )
        blacklistedTagListRecyclerview.adapter = adapter

        blacklistedTagListRecyclerview.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        blacklistedTagFloatingButton.setOnClickListener {
            val servers =
                viewModel.serverList.value ?: listOf()
            Log.d("Dialog", "$servers")
            val dialogBinding =
                BlacklistedTagEditDialogBinding.inflate(LayoutInflater.from(requireContext()))
            val serverAdapter =
                BlacklistedTagServerAdapter(servers, servers.map { false }.toMutableList())
            dialogBinding.serverListView.adapter = serverAdapter
            dialogBinding.serverListView.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Blacklist tags")
                .setView(dialogBinding.root)
                .setPositiveButton("Save", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        val tags = dialogBinding.blacklistedTagsInput.text.toString()
                        val selectedServers = serverAdapter.getSelected()
                        val data = BlacklistedTagWithServer(
                            blacklistedTag = BlacklistedTag(tags = tags),
                            servers = selectedServers
                        )
                        Log.d("Dialog", "$data")
                        viewModel.insertBlacklistTag(data)
                    }

                })
                .show()
        }


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.blacklistedTagFlow.collectLatest { list ->
                    list?.let {
                        Log.d("Dialog", "$it")
                        adapter.setData(it)
                    }
                }
            }
        }
    }
}