package com.example.myapplication.ui.language

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.DialogLanguageBinding
import com.example.myapplication.pref.PreferenceCacheImpl

class LanguageDialog : DialogFragment() {

    companion object {
        val languages: ArrayList<Language> = arrayListOf<Language>().apply {
            add(Language(1,"en"))
            add(Language(2,"ar"))
            add(Language(3,"fr"))
        }
    }

    private lateinit var binding: DialogLanguageBinding
    private lateinit var _adapter: LanguageAdapter



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogLanguageBinding.inflate(layoutInflater)
        isCancelable = false
        binding.rv.setupRecycler()
        return AlertDialog.Builder(requireContext()).setView(binding.root).create()
    }

    private fun RecyclerView.setupRecycler(){
        layoutManager = LinearLayoutManager(context)
        adapter =setupAdapter().also { _adapter = it;it.apply { setDataList(languages)}}
        setHasFixedSize(true)
    }

    private fun setupAdapter() = LanguageAdapter{
        PreferenceCacheImpl.setLanguageCode(it)
        dismiss()
    }
}