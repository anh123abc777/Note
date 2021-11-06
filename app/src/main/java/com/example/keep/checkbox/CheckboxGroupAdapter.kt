package com.example.keep.checkbox

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.keep.database.DataCheckboxes
import com.example.keep.databinding.ElementCheckboxBinding
import timber.log.Timber

private val REMOVE = 0
private val EDIT = 1
class CheckboxGroupAdapter(private val clickListener: OnClickListener?, private val canEdit : Boolean) : ListAdapter<DataCheckboxes, CheckboxGroupAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<DataCheckboxes>() {
        override fun areItemsTheSame(oldItem: DataCheckboxes, newItem: DataCheckboxes): Boolean {
            return oldItem===newItem
        }

        override fun areContentsTheSame(oldItem: DataCheckboxes, newItem: DataCheckboxes): Boolean {
            return oldItem==newItem
        }

    }

    class ViewHolder
    private constructor (val binding : ElementCheckboxBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(dataCheckboxes: DataCheckboxes, clickListener: OnClickListener?, position: Int, canEdit: Boolean){
            binding.dataCheckbox = dataCheckboxes
            binding.executePendingBindings()

            binding.textCheckbox.setOnFocusChangeListener { view, isFocus ->
                if(isFocus)
                    binding.deleteCheckbox.visibility = View.VISIBLE
                else
                    binding.deleteCheckbox.visibility = View.GONE

                if(!isFocus && binding.textCheckbox.isFocused){
                    clickListener?.onClick(dataCheckboxes,position, EDIT)
                }
            }
            binding.deleteCheckbox.setOnClickListener {
                clickListener?.onClick(dataCheckboxes,position,REMOVE)
                Timber.i("on click")
            }


            if(!canEdit){
                binding.textCheckbox.isEnabled = false
                binding.elementCheckbox.isClickable = false
                binding.elementCheckbox.isEnabled = false
            }

        }

        companion object{
            fun from(parent: ViewGroup) : ViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ElementCheckboxBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position),clickListener,position,canEdit)
    }

    class OnClickListener(val clickListener : (DataCheckboxes,Int,Int) -> Unit){
        fun onClick(dataCheckboxes: DataCheckboxes, pos : Int, removeOrEdit: Int) = clickListener(dataCheckboxes,pos,removeOrEdit)
    }

}