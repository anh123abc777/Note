package com.example.keep.search

import android.app.Application
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.keep.R
import com.example.keep.adapter.FilterAdapter
import com.example.keep.adapter.TypeFilter
import com.example.keep.databinding.FragmentSearchBinding
import com.example.keep.detail.DetailNoteFragment
import com.example.keep.overview.NotesAdapter
import com.example.keep.overview.OverviewViewModel
import com.example.keep.overview.OverviewViewModelFactory

class SearchFragment : Fragment() {

    private lateinit var binding : FragmentSearchBinding
    private lateinit var viewModel: OverviewViewModel
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var application: Application

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSearchBinding.inflate(inflater)

        application = requireNotNull(this.activity).application

        val factory = OverviewViewModelFactory(requireActivity(),application)

        viewModel = ViewModelProvider(this,factory).get(OverviewViewModel::class.java)

        binding.viewModel = viewModel

        binding.lifecycleOwner = this

        createFilterByTypeAdapter()
        createFilterByLabelAdapter()
        createNotesAdapter()
        observeNavigateToDetailView()
        setupSearch()
        setUpOnBack()

        return binding.root
    }

    private fun setupSearch(){

        binding.searchBar.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(query: CharSequence?, p1: Int, p2: Int, p3: Int) {
                notesAdapter.filterQuery(query.toString())

                if (query.isNullOrEmpty()){
                    viewModel.stopSearching()
                    notesAdapter.initNotes(viewModel.normalNotes.value!!)
                }else
                    viewModel.startSearching()
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

    private fun observeNavigateToDetailView(){
        viewModel.noteNavigate.observe(viewLifecycleOwner){
            if(it!=null) {
                val fragment = DetailNoteFragment()
                val args = Bundle()

//                val lastId = runBlocking {
//                    withContext(Dispatchers.IO){
//                        noteRepository.getLastNote().noteId
//                    }
//                }

                args.putInt("noteId", it)

                fragment.arguments = args

                val ft: FragmentTransaction = parentFragmentManager.beginTransaction()
//                ft.setCustomAnimations(R.anim.zoom_in,R.anim.zoom_out)
                ft.replace(R.id.view, fragment)
                ft.addToBackStack(null)
                ft.commit()
                viewModel.doneNavigating()
            }
        }
    }

    private fun createNotesAdapter() {

        notesAdapter = NotesAdapter(NotesAdapter.OnClickListener { note, isSelected ->
            viewModel.onClick(note, isSelected)
        }, application)
        viewModel.normalNotes.observe(viewLifecycleOwner) {
            if (it != null) {
                notesAdapter.initNotes(viewModel.normalNotes.value!!)
                binding.listItem.adapter = notesAdapter
            }
        }

        viewModel.labels.observe(viewLifecycleOwner) {
            if (it != null) {
                notesAdapter.initLabels(viewModel.labels)
            }
        }
    }

    private fun createFilterByTypeAdapter(){
        val filterByTypeAdapterItemClick = FilterAdapter.OnClickListener { query ->
            notesAdapter.filterType(query)
            viewModel.startSearching()
            binding.searchBar.hint = "search within $query"
        }
        val filterByTypeAdapter = FilterAdapter(filterByTypeAdapterItemClick,TypeFilter.TYPE)
        filterByTypeAdapter.submitList(viewModel.dispatchFilterByType())
        binding.filterNotesView.filterType.adapter = filterByTypeAdapter
    }

    private fun createFilterByLabelAdapter(){
        val filterByLabelAdapterItemClick = FilterAdapter.OnClickListener { query ->
            notesAdapter.filterLabel(query,true)
            viewModel.startSearching()
            binding.searchBar.hint = "search within $query"

        }
        val filterByLabelAdapter = FilterAdapter(filterByLabelAdapterItemClick,TypeFilter.LABEL)
        viewModel.labels.observe(viewLifecycleOwner) {
            if(viewModel.labels!=null) {
                filterByLabelAdapter.submitList(viewModel.dispatchFilterByLabel())
                binding.filterNotesView.filterLabels.adapter = filterByLabelAdapter
            }
        }
    }

}