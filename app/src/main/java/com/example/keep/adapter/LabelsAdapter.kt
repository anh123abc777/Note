package com.example.keep.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.keep.database.Label
import com.example.keep.database.NoteWithLabels
import com.example.keep.databinding.ElementLabelInLabelViewBinding
import timber.log.Timber


data class DataLabelsAdapter(val label: Label,var stateCheckbox: Int)

class LabelsAdapter(private val clickListener : OnClickListener) : ListAdapter<DataLabelsAdapter, LabelsAdapter.ViewHolder>(DiffCallback) {
    private var labels =  mutableListOf<DataLabelsAdapter>()
    companion object DiffCallback : DiffUtil.ItemCallback<DataLabelsAdapter>() {
        override fun areItemsTheSame(oldItem: DataLabelsAdapter, newItem: DataLabelsAdapter): Boolean {
            return oldItem===newItem
        }

        override fun areContentsTheSame(oldItem: DataLabelsAdapter, newItem: DataLabelsAdapter): Boolean {
            return oldItem==newItem
        }

    }

    class ViewHolder
    private constructor (val binding : ElementLabelInLabelViewBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(label: DataLabelsAdapter, clickListener: OnClickListener){
            binding.label = label
            binding.executePendingBindings()
            binding.elementLabelCheckbox.setOnClickListener {
                clickListener.onClick()
            }
        }

        companion object{
            fun from(parent: ViewGroup) : ViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ElementLabelInLabelViewBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position),clickListener)
    }

    fun filter(text: String) {
        var text = text
        var items = mutableListOf<DataLabelsAdapter>()
        if (text.isEmpty()) {
            items.addAll(labels)
        } else {
            text = text.lowercase()
            for (item in labels) {
                if (item.label.labelName.lowercase().contains(text)) {
                    items.add(item)
                }
            }
        }
        submitList(items)
        notifyDataSetChanged()
    }

    fun updateAllLabels(list: MutableList<DataLabelsAdapter>?=currentList){
        labels.clear()
        labels.addAll(list!!)
        Timber.i("labels ${labels}")
    }

    class OnClickListener(val clickListener : () -> Unit){
        fun onClick() = clickListener()
    }
}