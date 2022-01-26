package com.faldez.bonito.ui.server_edit

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
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
    private val viewModel: ServerEditViewModel by
    navGraphViewModels(R.id.nav_graph) {
        ServerEditViewModelFactory(PostRepository(BooruService()),
            ServerRepository(AppDatabase.build(requireContext())),
            this)
    }
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
        prepareAppBar()

        ArrayAdapter(requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            ServerType.values()).also { adapter ->
            adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
            binding.serverTypeSpinner.adapter = adapter
        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel

        Toast.makeText(activity?.applicationContext, "Test", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            viewModel.state.collect {
                when (it) {
                    State.Idle -> {
                        Log.d("ServerEditFragement", "Idle")
                    }
                    State.Success -> {
                        Toast.makeText(activity?.applicationContext, "Success", Toast.LENGTH_SHORT).show()
                        Log.d("ServerEditFragement", "Success")
                        (activity as MainActivity).onBackPressed()
                    }
                    State.Failed -> {
                        Toast.makeText(activity?.applicationContext, "Failed", Toast.LENGTH_SHORT).show()
                        Log.d("ServerEditFragement", "Failed")
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
                var error = false

                val type = binding.serverTypeSpinner.selectedItem as ServerType

                var name = binding.serverNameInput.text ?: ""
                name = name.trim()
                if (name.isEmpty()) {
                    binding.serverNameInputLayout.error = "Name cannot be empty"
                    error = true
                }

                var url = binding.serverUrlInput.text ?: ""
                url = url.trim().trimEnd { it == '/' }
                if (url.isEmpty()) {
                    binding.serverUrlInputLayout.error = "Url cannot be empty"
                    error = true
                } else if (!url.startsWith("http")) {
                    binding.serverUrlInputLayout.error = "Specify http:// or https://"
                    error = true
                }

                if (error) {
                    return true
                }

                viewModel.test(Server(type = type,
                    title = name.toString(),
                    url = url.toString()))

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}