package com.example.keep.image

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.example.keep.databinding.ElementImageBinding

open class ImageAdapter(private val canEdit: Boolean, private val clickListener: OnClickListener) :
    RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    private var list: List<String> = emptyList()
    override fun getItemCount() = list.size

    fun submitList(sources: MutableList<String>) {
        if (sources != null) {
            list = sources
        }
        notifyDataSetChanged()
    }

    class ViewHolder
        private constructor (val binding : ElementImageBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(source: String, position: Int, canEdit: Boolean, clickListener: OnClickListener){
            val uri : Uri = Uri.parse(source)
            binding.reSource = uri
            binding.isOverview = !canEdit
            binding.executePendingBindings()

            if(canEdit) {
                binding.deleteImageBtn.visibility = View.VISIBLE
                binding.deleteImageBtn.setOnClickListener {
                    clickListener.onClick(source,position)
                }
            }

        }

        companion object{
            fun from(parent: ViewGroup) : ViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ElementImageBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position],position,canEdit,clickListener)
    }


    class OnClickListener(val clickListener : (imageUri: String, position : Int) -> Unit){
        fun onClick(imageUri: String, position: Int) = clickListener(imageUri,position)
    }


}