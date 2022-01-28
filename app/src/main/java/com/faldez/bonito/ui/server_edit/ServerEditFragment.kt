package com.faldez.bonito.ui.server_edit

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navGraphViewModels
import com.faldez.bonito.MainActivity
import com.faldez.bonito.R
import com.faldez.bonito.data.PostRepository
import com.faldez.bonito.data.ServerRepository
import com.faldez.bonito.database.AppDatabase
import com.faldez.bonito.databinding.ServerEditFragmentBinding
import com.faldez.bonito.model.Server
import com.faldez.bonito.model.ServerType
import com.faldez.bonito.service.BooruService
import com.google.android.material.shape.MaterialShapeDrawable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.prefs.AbstractPreferences

class ServerEditFragment : Fragment() {
    private lateinit var viewModel: ServerEditViewModel
    private lateinit var binding: ServerEditFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    private fun prepareAppBar() {
        (activity as MainActivity).setSupportActionBar(binding.serverNewTopappbar)
        binding.serverNewAppbarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())
        val supportActionBar = (activity as MainActivity).supportActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = ServerEditFragmentBinding.inflate(inflater, container, false)

        val server = arguments?.getParcelable<Server>("server")

        viewModel = ServerEditViewModelFactory(server, PostRepository(BooruService()),
            ServerRepository(AppDatabase.build(requireContext())),
            this).create(ServerEditViewModel::class.java)

        server?.let {
            binding.serverNewTopappbar.title = "Edit server"
            binding.serverNameInput.text = SpannableStringBuilder(it.title)
            binding.serverUrlInput.text = SpannableStringBuilder(it.url)
            val position = ServerType.values().indexOf(it.type)
            binding.serverTypeSpinner.setSelection(position)
        }

        prepareAppBar()

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Toast.makeText(activity?.applicationContext, "Test", Toast.LENGTH_SHORT).show()

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
                    State.Failed -> {
                        Toast.makeText(activity?.applicationContext, "Failed", Toast.LENGTH_SHORT)
                            .show()
                        Log.d("ServerEditFragement", "Failed")
                        binding.testProgressBar.isVisible = false
                    }
                }
                viewModel.state.value = State.Idle
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.server_edit_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> {
                (activity as MainActivity).onBackPressed()
                return true
            }
            R.id.save_server_edit_button -> {
                Log.d("ServerEditFragment", "Save")
                val error = viewModel.validate()
                if (error != null) {
                    return true
                }

                binding.testProgressBar.isVisible = true

                viewModel.server.value?.let {
                    viewModel.test(it)
                }

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}