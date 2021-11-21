package com.example.keep.label

import android.app.Application
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.keep.TriStateMaterialCheckBox
import com.example.keep.adapter.LabelsAdapter
import com.example.keep.database.Label
import com.example.keep.database.NoteDatabase
import com.example.keep.database.NoteRepository
import com.example.keep.database.NoteWithLabels
import com.example.keep.databinding.FragmentLabelBinding
import com.example.keep.detail.DetailNoteFragment
import com.example.keep.detail.DetailNoteViewModel
import com.example.keep.detail.DetailNoteViewModelFactory
import com.example.keep.overview.OverviewViewModel
import com.example.keep.overview.OverviewViewModelFactory
import timber.log.Timber

class LabelFragment : Fragment() {

    private lateinit var binding: FragmentLabelBinding
    private lateinit var viewModel: LabelViewModel
    private lateinit var adapter: LabelsAdapter
    private lateinit var overViewModel: OverviewViewModel
    private lateinit var application: Application
    private lateinit var detailNoteViewModel: DetailNoteViewModel
    private lateinit var noteIdsToLabel : IntArray

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentLabelBinding.inflate(inflater)

        noteIdsToLabel = LabelFragmentArgs.fromBundle(requireArguments()).noteIdsToLabel
        application = requireNotNull(activity).application

        val factory = LabelViewModelFactory(noteIdsToLabel,application)
        viewModel = ViewModelProvider(this,factory).get(LabelViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        adapter = LabelsAdapter(LabelsAdapter.OnClickListener {
            saveLabelNotes()
        })

        binding.listLabel.adapter =adapter


        var a = 1
        viewModel.labels.observe(viewLifecycleOwner){
            if(it!=null && a==1){

                    adapter.submitList(viewModel.createDataAdapter())
                    adapter.updateAllLabels()
                a++
            }
        }


        setUpSearchable()
        setUpOnBack()
        checkboxListener()


        return binding.root
    }

    private fun setUpSearchable(){
        var a =1
        binding.searchBar.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(chasequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter.filter(chasequence.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

    }


    private fun setUpOnBack(){
        binding.toolbar.setNavigationOnClickListener {
        requireActivity().onBackPressed()
        }
    }



    override fun onPause() {
        super.onPause()
        Timber.i("${viewModel.labels.value}")
        saveLabelNotes()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.noteLabelCrossRef.observe(viewLifecycleOwner){

                if(viewModel.labels.value!=null ) {
                viewModel.updateNotesToLabel()
                    viewModel.createDataAdapter()
                    adapter.submitList(viewModel.dataAdapter)
                    adapter.updateAllLabels(viewModel.dataAdapter)

                Timber.i("noteLabelCrossRef ${viewModel.createDataAdapter()}")
            }

        }

        val overFactory = OverviewViewModelFactory(requireActivity(),application)
        overViewModel = ViewModelProvider(requireActivity(),overFactory).get(OverviewViewModel::class.java)
        val data = Bundle().apply { putString("ARGUMENT_MESSAGE", "Hello from FragmentB") }
        overViewModel.bundleFromFragmentBToFragmentA.value = data
        overViewModel.tamp = 4

    }

    private fun saveLabelNotes(){

        var listLabels = mutableListOf<Label>()
            adapter.currentList.forEachIndexed { index, dataLabelsAdapter ->
                val viewHolder = binding.listLabel
                    .findViewHolderForAdapterPosition(index) as LabelsAdapter.ViewHolder

                when (viewHolder.binding.elementLabelCheckbox.state) {
                    TriStateMaterialCheckBox.STATE_CHECKED -> {
                        viewModel
                            .addLabelToNote(dataLabelsAdapter.label)
                        listLabels.add(dataLabelsAdapter.label)
                    }

                    TriStateMaterialCheckBox.STATE_UNCHECKED -> {
                        viewModel
                            .removeAllLabelOnNotes(dataLabelsAdapter.label)
                        listLabels.removeIf { it == dataLabelsAdapter.label }
                    }

                    else -> Timber.i("nothing happens")
                }
            }

        if(arguments?.getBoolean("isFromDetail") == true) {
            overViewModel.setUpLabelNavigate(listLabels)
            Timber.i("${overViewModel.labelNavigate}")
        }

    }



    private fun checkboxListener(){

        binding.listLabel.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener{
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                Timber.i("on intercept Touch")

                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
                Timber.i("on  Touch")
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
                Timber.i("on Request Touch")
            }
        })
    }
}
