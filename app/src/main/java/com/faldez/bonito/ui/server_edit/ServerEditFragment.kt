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
import androidx.core.widget.addTextChangedListener
import com.faldez.bonito.MainActivity
import com.faldez.bonito.R
import com.faldez.bonito.databinding.ServerEditFragmentBinding
import com.faldez.bonito.model.ServerType
import com.google.android.material.shape.MaterialShapeDrawable
import java.util.prefs.AbstractPreferences

class ServerEditFragment : Fragment() {

    companion object {
        fun newInstance() = ServerEditFragment()
    }

    private lateinit var viewModel: ServerEditViewModel
    private lateinit var binding: ServerEditFragmentBinding
    private lateinit var sharedPreferences: SharedPreferences

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

        sharedPreferences =
            requireActivity().getSharedPreferences(getString(R.string.server_list_key),
                Context.MODE_PRIVATE)

        ArrayAdapter(requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            ServerType.values()).also { adapter ->
            adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
            binding.serverTypeSpinner.adapter = adapter
        }

        binding.serverNameInput.addTextChangedListener {
            Log.d("ServerEditFragment", "title: $it")
        }
        binding.serverUrlInput.addTextChangedListener {
            Log.d("ServerEditFragment", "url: $it")
        }
        binding.serverTypeSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    id: Long,
                ) {
                    val it = ServerType.values().get(pos)
                    Log.d("ServerEditFragment", "type: $it")
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }

            }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ServerEditViewModel::class.java)
        // TODO: Use the ViewModel
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
                if (binding.serverNameInput.text.isNullOrEmpty()) {
                    binding.serverNameInputLayout.error = "Name cannot be empty"
                }
                if (binding.serverUrlInput.text.isNullOrEmpty()) {
                    binding.serverUrlInputLayout.error = "Url cannot be empty"
                }

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}