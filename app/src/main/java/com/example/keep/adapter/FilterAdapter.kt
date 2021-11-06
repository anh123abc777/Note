package com.example.keep.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.keep.R
import com.example.keep.databinding.ItemFilterBinding

data class DataFilterAdapter(val filter : String, val icon : Int)
enum class TypeFilter{TYPE, LABEL}
class FilterAdapter(private val clickListener: OnClickListener, val type: TypeFilter) : ListAdapter<DataFilterAdapter, FilterAdapter.ViewHolder>(DiffCallBack) {

    companion object DiffCallBack: DiffUtil.ItemCallback<DataFilterAdapter>() {
        override fun areItemsTheSame(oldItem: DataFilterAdapter, newItem: DataFilterAdapter): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: DataFilterAdapter, newItem: DataFilterAdapter): Boolean {
            return oldItem.filter == newItem.filter
        }

    }

    class ViewHolder
        private constructor(private val binding: ItemFilterBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DataFilterAdapter, clickListener: OnClickListener, type: TypeFilter) {
            binding.item = item
            when(type){
                TypeFilter.TYPE -> binding.frameItemFilter.setBackgroundColor(Color.BLUE)
                TypeFilter.LABEL -> binding.frameItemFilter.setBackgroundResource(R.color.grayColor)
            }

            binding.root.setOnClickListener {
                clickListener.onClick(item.filter)
            }
        }

        companion object{
            fun from(parent: ViewGroup) : ViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemFilterBinding.inflate(layoutInflater,parent,false)

                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position),clickListener,type)
    }

    class OnClickListener(val query: (String) -> Unit){
        fun onClick(text: String)  = query(text)
    }
}