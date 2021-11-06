package com.example.keep.label

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.keep.database.Label
import com.example.keep.database.NoteWithLabels
import com.example.keep.databinding.ElementLabelInNoteViewBinding

class LabelsInNoteIViewAdapter  : ListAdapter<Label, LabelsInNoteIViewAdapter.ViewHolder>(DiffCallback) {
    companion object DiffCallback : DiffUtil.ItemCallback<Label>() {
        override fun areItemsTheSame(oldItem: Label, newItem: Label): Boolean {
            return oldItem===newItem
        }

        override fun areContentsTheSame(oldItem: Label, newItem: Label): Boolean {
            return oldItem==newItem
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder
    private constructor (private val binding : ElementLabelInNoteViewBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(label : Label){
            binding.label = label
            binding.executePendingBindings()

        }

        companion object{
            fun from(parent: ViewGroup) : ViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ElementLabelInNoteViewBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }

}