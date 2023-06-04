package com.example.myapplication.ui.language

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemLanguageBinding

class LanguageAdapter(
    var itemClick:(id:String)->Unit = {}
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var data: List<Language> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =  CheckBoxHolder(
        ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as CheckBoxHolder).bind(data[position])
    }
    override fun getItemCount(): Int  = data.size

    override fun getItemViewType(position: Int): Int =  position

    fun setDataList(list: List<Language>) {
        data = list
        notifyDataSetChanged()
    }
    inner class CheckBoxHolder(private val binding: ItemLanguageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Language) {
            binding.name.text = data.name
            itemView.setOnClickListener { itemClick(data.name) }
        }
    }
}