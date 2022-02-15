package com.faldez.shachi.ui.server_edit

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.faldez.shachi.MainActivity
import com.faldez.shachi.R
import com.faldez.shachi.database.AppDatabase
import com.faldez.shachi.databinding.ServerEditFragmentBinding
import com.faldez.shachi.model.Server
import com.faldez.shachi.model.ServerType
import com.faldez.shachi.repository.PostRepository
import com.faldez.shachi.repository.ServerRepository
import com.faldez.shachi.repository.TagRepository
import com.faldez.shachi.service.BooruService
import com.google.android.material.shape.MaterialShapeDrawable
import kotlinx.coroutines.launch

class ServerEditFragment : Fragment() {
    private val viewModel: ServerEditViewModel by viewModels {
        val server = arguments?.getParcelable<Server>("server")
        val db = AppDatabase.build(requireContext())
        val service = BooruService()
        ServerEditViewModelFactory(server, PostRepository(service),
            ServerRepository(db), TagRepository(service, db),
            this)
    }
    private lateinit var binding: ServerEditFragmentBinding

    private fun prepareAppBar() {
        binding.serverNewAppbarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())
        binding.serverNewTopappbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        binding.serverNewTopappbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.serverNewTopappbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.save_server_edit_button -> {
                    Log.d("ServerEditFragment", "Save")
                    val error = viewModel.validate()
                    if (error == null) {
                        viewModel.server.value?.let {
                            binding.testProgressBar.isVisible = true
                            viewModel.test(it)
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = ServerEditFragmentBinding.inflate(inflater, container, false)

        ArrayAdapter(requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            ServerType.values()).also { adapter ->
            adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
            binding.serverTypeSpinner.adapter = adapter
        }

        binding.serverNameInput.doOnTextChanged { text, _, _, _ ->
            viewModel.setTitle(text.toString())
        }

        binding.serverUrlInput.doOnTextChanged { text, _, _, _ ->
            viewModel.setUrl(text.toString())
        }

        binding.usernameInput.doOnTextChanged { text, _, _, _ ->
            viewModel.setUsername(text.toString())
        }

        binding.passwordInput.doOnTextChanged { text, _, _, _ ->
            viewModel.setPassword(text.toString())
        }

        binding.serverTypeSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    val type = ServerType.values().get(position)
                    viewModel.setType(type)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }

            }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareAppBar()

        viewModel.server.value?.let { server ->
            binding.serverNewTopappbar.title = "Edit server"
            binding.serverNameInput.text = SpannableStringBuilder(server.title)
            binding.serverUrlInput.text = SpannableStringBuilder(server.url)
            val position = ServerType.values().indexOf(server.type)
            binding.serverTypeSpinner.setSelection(position)
            server.username?.let {
                binding.usernameInput.text = SpannableStringBuilder(it)
            }
            server.password?.let {
                binding.passwordInput.text = SpannableStringBuilder(it)
            }
        }

        lifecycleScope.launch {
            viewModel.state.collect {
                when (it) {
                    State.Idle -> {
                        Log.d("ServerEditFragement", "Idle")
                    }
                    State.Success -> {
                        Toast.makeText(activity?.applicationContext, "Success", Toast.LENGTH_SHORT)
                            .show()
                        Log.d("ServerEditFragement", "Success")
                        (activity as MainActivity).onBackPressed()
                        binding.testProgressBar.isVisible = false
                    }
                    is State.Failed -> {
                        Toast.makeText(activity?.applicationContext,
                            "Failed: ${it.reason}",
                            Toast.LENGTH_SHORT)
                            .show()
                        Log.d("ServerEditFragement", "Failed")
                        binding.testProgressBar.isVisible = false
                    }
                }
                viewModel.state.value = State.Idle
            }
        }
    }
}