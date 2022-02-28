package com.faldez.shachi.ui.server_dialog

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.faldez.shachi.R
import com.faldez.shachi.database.AppDatabase
import com.faldez.shachi.databinding.ServerDialogFragmentBinding
import com.faldez.shachi.repository.ServerRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class ServerDialogFragment : DialogFragment() {

    private lateinit var binding: ServerDialogFragmentBinding
    private val viewModel: ServerDialogViewModel by viewModels {
        ServerDialogModelFactory(ServerRepository(AppDatabase.build(requireContext())),
            this)
    }
    private val adapter: ServerDialogAdapter = ServerDialogAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = ServerDialogFragmentBinding.inflate(inflater, container, false)

        binding.bind()

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.select_server).apply {
            setPositiveButton(R.string.ok) { _, _ ->
                adapter.currentList.find { it.selected }?.let {
                    viewModel.insert(it.serverId)
                }
            }
        }.create()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (dialog as AlertDialog?)?.setView(view)
    }

    private fun ServerDialogFragmentBinding.bind() {
        serverListRecyclerview.adapter = adapter
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        serverListRecyclerview.layoutManager = layoutManager

        lifecycleScope.launch {
            viewModel.serverList.collect {
                Log.d("ServerDialogFragment", "$it")
                adapter.submitList(it)
            }
        }
    }

}