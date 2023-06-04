package com.example.myapplication.ui.`object`

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemCheckboxBinding

class ObjectsAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var data: List<ObjectClass> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =  CheckBoxHolder(
        ItemCheckboxBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as CheckBoxHolder).bind(data[position])
    }

    override fun getItemCount(): Int  = data.size

    override fun getItemViewType(position: Int): Int =  position

    fun setDataList(list: List<ObjectClass>) {
        data = list
        notifyDataSetChanged()
    }

    fun getDataList()= data

    inner class CheckBoxHolder(private val binding: ItemCheckboxBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ObjectClass) {
            binding.name.text = data.name
            binding.cb.isChecked = data.isChecked
            binding.cb.setOnCheckedChangeListener { compoundButton, b -> data.isChecked = b }
        }
    }
}