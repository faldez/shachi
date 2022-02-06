package com.faldez.shachi.ui.server_dialog

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
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
    private lateinit var viewModel: ServerDialogViewModel
    private val adapter: ServerDialogAdapter = ServerDialogAdapter()

    private var dialogView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = ServerDialogFragmentBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this,
            ServerDialogModelFactory(ServerRepository(AppDatabase.build(requireContext())),
                this)).get(ServerDialogViewModel::class.java)
        binding.bind()

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.select_server).apply {
            dialogView = onCreateView(layoutInflater, null, savedInstanceState)
            dialogView?.let {
                onViewCreated(it, savedInstanceState)
            }
            setView(dialogView)
            setPositiveButton(R.string.ok) { _, _ ->
                adapter.currentList.find { it.selected }?.let {
                    viewModel.insert(it.serverId)
                }
            }
        }.create()

    override fun getView(): View? {
        return dialogView
    }

    private fun ServerDialogFragmentBinding.bind() {
//        adapter = ServerDialogAdapter()
        serverListRecyclerview.adapter = adapter
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        serverListRecyclerview.layoutManager = layoutManager

//        val divider = DividerItemDecoration(serverListRecyclerview.context,
//            layoutManager.orientation)
//        serverListRecyclerview.addItemDecoration(divider)

        lifecycleScope.launch {
            viewModel.serverList.collect {
                Log.d("ServerDialogFragment", "$it")
                adapter.submitList(it)
            }
        }
    }

}