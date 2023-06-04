package com.example.myapplication.ui.`object`

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.DialogFilterBinding
import com.example.myapplication.extensions.loadLabelsFile
import com.example.myapplication.ui.MainActivity

class CustomDialog : DialogFragment() {

    private lateinit var binding: DialogFilterBinding
    private lateinit var _adapter: ObjectsAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogFilterBinding.inflate(layoutInflater)
        isCancelable = false
        binding.rv.setupRecycler()
        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setPositiveButton(getString(R.string.apply)){dialog,_->
                _adapter.getDataList().filter { it.isChecked }.let {
                    MainActivity.List = it.ifEmpty { null }
                }

                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)){dialog,_-> dialog.dismiss() }
            .create()
    }

    private fun RecyclerView.setupRecycler(){
        layoutManager = LinearLayoutManager(context)
        adapter =setupAdapter().also {adapter-> _adapter = adapter;adapter.apply {
            context.loadLabelsFile("pretrained_lables.txt").apply {
                map { ObjectClass(it) }.also {mapped->
                    mapped.filter {it.name != "???" }.let {modelsList->
                        modelsList.forEach {outer->
                            MainActivity.List?.forEach {inner->
                                if (outer.name == inner.name) outer.isChecked = inner.isChecked
                            }
                        }
                        modelsList
                    }.also {result->setDataList(result.sortedBy { it.name })  }
                }
            }
        }
        }
        setHasFixedSize(true)
    }

    private fun setupAdapter() = ObjectsAdapter()
}