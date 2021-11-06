package com.example.keep.labelsetting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.keep.R
import com.example.keep.adapter.LabelSettingAdapter
import com.example.keep.databinding.FragmentLabelSettingBinding
import timber.log.Timber


class LabelSettingFragment : Fragment() {

    private lateinit var viewModel: LabelSettingViewModel
    private lateinit var binding: FragmentLabelSettingBinding
    private lateinit var adapter: LabelSettingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentLabelSettingBinding.inflate(inflater)
        val application = requireNotNull(activity).application
        val factory = LabelSettingViewModelFactory(application)
        viewModel = ViewModelProvider(this,factory).get(LabelSettingViewModel::class.java)

        initAdapter()

        initFrameAddLabel()

        setUpOnBackPress()

        return binding.root
    }

    private fun initAdapter(){
        adapter = LabelSettingAdapter(LabelSettingAdapter.OnClickListener{id, pos,signal ->
            when(signal){
                "delete" -> {
                    viewModel.removeLabel(id)
                    adapter.notifyItemRemoved(pos)
                }
                else -> Timber.i("have problem")
            }
        })

        viewModel.allLabels.observe(viewLifecycleOwner){
            adapter.submitList(it)
        }

        binding.listLabel.adapter = adapter

    }

    private fun setUpOnBackPress(){
        binding.toolbar.setNavigationOnClickListener {
           requireActivity().onBackPressed()
        }
    }

    private fun initFrameAddLabel() {

        setUpInitialState()

        setUpStateChangeAllButton()

        setUpFunctionEndButton()

        setUpFunctionStartButton()
    }

    private fun setUpInitialState(){
        binding.frameAddItemLabel.startButton.setImageResource(R.drawable.ic_baseline_add_black_24)
        binding.frameAddItemLabel.endButton.setImageResource(R.drawable.ic_baseline_check_black_24)
        binding.frameAddItemLabel.endButton.visibility = View.VISIBLE
        binding.frameAddItemLabel.labelNameView.hint = "new label"
        binding.frameAddItemLabel.labelNameView.requestFocus()
        binding.frameAddItemLabel.endButton.tag = "add"

    }

    private fun setUpStateChangeAllButton(){
        binding.frameAddItemLabel.labelNameView.setOnFocusChangeListener { _, isFocus ->

            if (isFocus) {

                binding.frameAddItemLabel.startButton.setImageResource(R.drawable.ic_baseline_close_24)
                binding.frameAddItemLabel.endButton.visibility = View.VISIBLE
                binding.frameAddItemLabel.startButton.tag = ""
                binding.frameAddItemLabel.endButton.tag = "add"
                binding.frameAddItemLabel.frame.setBackgroundResource(R.drawable.stroke)

            } else {
                binding.frameAddItemLabel.startButton.setImageResource(R.drawable.ic_baseline_add_black_24)
                binding.frameAddItemLabel.endButton.visibility = View.INVISIBLE
                binding.frameAddItemLabel.startButton.tag = "focus"
                binding.frameAddItemLabel.frame.background = null
            }
        }
    }

    private fun setUpFunctionStartButton(){
        binding.frameAddItemLabel.startButton.setOnClickListener {
            when(binding.frameAddItemLabel.startButton.tag ){
                "focus" -> binding.frameAddItemLabel.labelNameView.requestFocus()
                ""  -> binding.frameAddItemLabel.labelNameView.clearFocus()

            }
        }
    }

    private fun setUpFunctionEndButton() {
        binding.frameAddItemLabel.endButton.setOnClickListener {
            viewModel.addLabel(binding.frameAddItemLabel.labelNameView.text.toString())
            binding.frameAddItemLabel.labelNameView.text.clear()
            adapter.notifyItemInserted(viewModel.allLabels.value!!.size+1)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveData()
    }
}