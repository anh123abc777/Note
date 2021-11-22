package com.example.keep.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.keep.R
import com.example.keep.database.Label
import com.example.keep.databinding.ItemLabelInLabelSettingViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import timber.log.Timber


class LabelSettingAdapter(private val clickListener : OnClickListener) : ListAdapter<Label,LabelSettingAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Label>() {
        override fun areItemsTheSame(oldItem: Label, newItem: Label): Boolean {
            return oldItem===newItem
        }

        override fun areContentsTheSame(oldItem: Label, newItem: Label): Boolean {
            return oldItem==newItem
        }

    }

    class ViewHolder
    private constructor (val binding : ItemLabelInLabelSettingViewBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(label: Label, position: Int, clickListener: OnClickListener){
            binding.label = label

            Timber.i("label ${label}")
            when(label.labelId){
                Int.MAX_VALUE -> {
                    binding.startButton.setImageResource(R.drawable.ic_baseline_add_black_24)
                    binding.endButton.setImageResource(R.drawable.ic_baseline_check_black_24)
                    binding.endButton.visibility = View.VISIBLE
                    binding.labelNameView.hint = "new label"
                    binding.labelNameView.requestFocus()
                    binding.endButton.tag = "add"
                }
                else -> {
                    binding.endButton.tag = "focus"
                }

            }

            binding.labelNameView.setOnFocusChangeListener { _, isFocus ->
                if(isFocus){

                            binding.startButton.setImageResource(R.drawable.ic_baseline_delete_outline_24)
                            binding.endButton.setImageResource(R.drawable.ic_baseline_check_24)
                            binding.startButton.tag = "delete"
                            binding.endButton.tag = ""

                    binding.root.setBackgroundResource(R.drawable.stroke)
                }
                else{

                    binding.startButton.setImageResource(R.drawable.ic_outline_label_24)
                    binding.endButton.setImageResource(R.drawable.ic_baseline_edit_24)
                    binding.endButton.tag = "focus"
                    clickListener.onClick(Label(label.labelId,binding.labelNameView.text.toString()),
                        position,"update")

                    binding.root.background = null
                }
            }

            binding.endButton.setOnClickListener {
                when(binding.endButton.tag ){
                    "focus" -> binding.labelNameView.requestFocus()
                    ""  -> binding.labelNameView.clearFocus()
                }
            }

            binding.startButton.setOnClickListener {
                when(binding.startButton.tag ){
                    "focus" -> binding.labelNameView.requestFocus()
                    ""  -> binding.labelNameView.clearFocus()
                    "delete" -> {
                        clickListener
                            .onClick(label,
                                position,
                                binding.startButton.tag.toString()
                            )
                    }
                }
            }
        }

        companion object{
            fun from(parent: ViewGroup) : ViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemLabelInLabelSettingViewBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun addHeaderAndSubmitList(list: List<Label>?) {
//        adapterScope.launch {
//            val items = when (list) {
//                null -> listOf(Label(Int.MAX_VALUE,"df"))
//                else -> listOf(Label(Int.MAX_VALUE,"")) + list
//            }
//            withContext(Dispatchers.Main) {
//                submitList(items)
//            }
//        }
        var items = mutableListOf<Label>()
        items.add(Label(Int.MAX_VALUE,""))
        list?.let { items.addAll(it) }
        Timber.i("labels items ${items}")
        submitList(items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position),position,clickListener)
    }



    class OnClickListener(val clickListener : (Label, Int ,String) -> Unit){
        fun onClick(label: Label, pos: Int,signal: String) = clickListener(label,pos,signal)
    }
}