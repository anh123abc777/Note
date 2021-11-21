package com.example.keep.detail

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.keep.R
import com.example.keep.adapter.ImageGridAdapter
import com.example.keep.checkbox.CheckboxGroupAdapter
import com.example.keep.database.*
import com.example.keep.databinding.DateTimePickerBinding
import com.example.keep.databinding.FragmentDetailNoteBinding
import com.example.keep.databinding.FunctionEditContentNoteBottomSheetLayoutBinding
import com.example.keep.databinding.FunctionSettingNoteBottomSheetLayoutBinding
import com.example.keep.image.ImageAdapter
import com.example.keep.label.LabelFragment
import com.example.keep.label.LabelsInNoteIViewAdapter
import com.example.keep.overview.OverviewViewModel
import com.example.keep.overview.OverviewViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

//

class DetailNoteFragment : Fragment() {

    private lateinit var binding : FragmentDetailNoteBinding
    private lateinit var viewModel : DetailNoteViewModel
    private lateinit var repository: NoteRepository
    private lateinit var checkboxesAdapter: CheckboxGroupAdapter
    private lateinit var imageAdapter: ImageGridAdapter
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var application: Application
    private var noteId: Int = 0
    private lateinit var overviewModel: OverviewViewModel
    private var noteWithLabels = NoteWithLabels(Note(-1), listOf())


    private val REMOVE = 0
    private val EDIT = 1


    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding = FragmentDetailNoteBinding.inflate(inflater)

        noteId = DetailNoteFragmentArgs.fromBundle(requireArguments()).noteId

        application = requireNotNull(activity).application
        repository = NoteRepository(NoteDatabase.getInstance(application).noteDao)

        val overFactory = OverviewViewModelFactory(requireActivity(),application)
        overviewModel = ViewModelProvider(requireActivity(),overFactory).get(OverviewViewModel::class.java)

        noteWithLabels =
            try {
                runBlocking {
                    withContext(Dispatchers.IO) {
                        repository.get(noteId)
                    }
                }
            } catch (e : Exception){
                NoteWithLabels(Note(-1), listOf())
            }

        if(noteWithLabels==null){

            val lastId = runBlocking {
                withContext(Dispatchers.IO){
                    repository.getLastNote().noteId+1
                }
            }

            val firstPosition = runBlocking {
                withContext(Dispatchers.IO){
                    repository.getFirstPosition()-1
                }
            }

            when(requireArguments().getInt("noteId")) {
                -2 -> noteWithLabels = NoteWithLabels(Note(lastId,position = firstPosition), overviewModel.labelNavigate)

                -1 -> noteWithLabels = NoteWithLabels(
                    Note(lastId,checkboxes = arrayListOf(DataCheckboxes(0,"")),position = firstPosition),
                    overviewModel.labelNavigate)
            }
        }

        Timber.i("${overviewModel.labelNavigate}  $noteWithLabels")


        val factory = DetailNoteViewModelFactory(noteWithLabels,repository)
        viewModel = ViewModelProvider(viewModelStore,factory).get(DetailNoteViewModel::class.java)
        viewModel.noteWithLabels = noteWithLabels
        application = requireNotNull(activity).application
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        imageAdapter =
            ImageGridAdapter(canEdit = true, ImageAdapter.OnClickListener { imageUri, position ->
                viewModel!!.deleteImage(imageUri)
                imageAdapter.submitList(viewModel.noteWithLabels.note.images)
            })

        checkboxesAdapter = CheckboxGroupAdapter(CheckboxGroupAdapter.OnClickListener {dataCheckbox,pos,removeOrEdit ->
            when(removeOrEdit){
                REMOVE -> {
                    viewModel.removeCheckbox(dataCheckbox.id)
                    checkboxesAdapter.notifyItemRemoved(pos)
                }

                else -> {
                    viewModel.updateCheckbox(dataCheckbox)
                    checkboxesAdapter.notifyItemChanged(pos)
                }
            }

        },true)

        binding.checkboxGroup.adapter = checkboxesAdapter


        val labelsAdapter = LabelsInNoteIViewAdapter()
        binding.labels.adapter = labelsAdapter
//            adapter.submitList(noteWithLabels.labels)


        viewModel.addButtonClicked.observe(viewLifecycleOwner){
            if(it){
                checkboxesAdapter.notifyItemInserted(noteWithLabels.note.checkboxes.size +1)
                viewModel.addCheckboxDone()
            }
        }

        viewModel.stateCheckBoxes.observe(viewLifecycleOwner){
            if(it){
                checkboxesAdapter.submitList(viewModel.noteWithLabels.note.checkboxes)
                binding.checkboxGroup.visibility = View.VISIBLE
            }
        }

        setUpBottomAppbar()
        setUpAppbar()

        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == Activity.RESULT_OK){
                val data : String? = result.data?.dataString
                Timber.i("Data ${result.data}")
                if(data!=null) {
                    val contentResolver = application.applicationContext.contentResolver

                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    // Check for the freshest data.
                    contentResolver.takePersistableUriPermission(data!!.toUri(), takeFlags)
                    viewModel.addImage(data)
                    imageAdapter.submitList(viewModel.noteWithLabels.note.images)
                    Timber.i("images ${viewModel.noteWithLabels.note.images}")
                    imageAdapter.notifyDataSetChanged()
                }

            }
        }

        return binding.root
    }

    companion object {
        const val TAG = "DetailNoteFragment"
    }

    private fun setUpAppbar(){
        binding.contextualActionBar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.pin -> {
                    viewModel.addPin()
                    if (viewModel.noteWithLabels.note.priority==1){
                        menuItem.setIcon(R.drawable.ic_pinned)
                    } else
                        menuItem.setIcon(R.drawable.ic_pin)
                }
                else -> showDateTimePicker(listOf(viewModel.noteWithLabels.note))
            }
            true
        }

        if(noteWithLabels.note.state==-1){
            binding.contextualActionBar.menu.forEach {
                it.isVisible = false
            }
        }

        setUpOnBack()
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun showDateTimePicker(notes : List<Note>) {
        val dialogViewBinding = DateTimePickerBinding.inflate(layoutInflater)
        val alertDialog = AlertDialog.Builder(activity).create()

        dialogViewBinding.date.text = SimpleDateFormat("MMM dd").format(System.currentTimeMillis()).toString()
        dialogViewBinding.time.text = SimpleDateFormat("HH:mm").format(System.currentTimeMillis()).toString()


        val currentTime = Calendar.getInstance()
        var mYear = currentTime[Calendar.YEAR] // current year

        var mMonth = currentTime[Calendar.MONTH] // current month

        var mDay = currentTime[Calendar.DAY_OF_MONTH] // current day

        var hour = currentTime[Calendar.HOUR_OF_DAY]
        var minute = currentTime[Calendar.MINUTE]

        dialogViewBinding.time.setOnClickListener {

            val timePicker = TimePickerDialog(context, { timePicker, selectedHour, selectedMinute ->
                dialogViewBinding.time.text = "$selectedHour:$selectedMinute"
                hour=selectedHour
                minute=selectedMinute
            },
                hour,
                minute,
                true
            )
            timePicker.setTitle("Select time")
            timePicker.show()
        }


        dialogViewBinding.date.setOnClickListener {

            val datePickerDialog = DatePickerDialog(requireContext(),
                { _, year, monthOfYear, dayOfMonth -> // set day of month , month and year value in the edit text
                    val dateString = "${monthOfYear+1}/$dayOfMonth/$year"
                    val formatter = SimpleDateFormat("MM/dd/yyyy")
                    val date = formatter.parse(dateString)
                    val desiredFormat = SimpleDateFormat("MMM dd yyyy").format(date)

                    dialogViewBinding.date.text = desiredFormat

                    mYear=year
                    mMonth=monthOfYear + 1
                    mDay = dayOfMonth
                }, mYear, mMonth, mDay
            )
            datePickerDialog.show()
        }

        alertDialog.setView(dialogViewBinding.root)
        alertDialog.setTitle("Add reminder")
        alertDialog.show()

        var timeReminder = -1L
        dialogViewBinding.saveAction.setOnClickListener {

            val timeReminderString = mMonth.toString() +" "+ mDay.toString() +" "+  mYear.toString()+" " +
                    hour.toString()+ ":" + minute.toString()
            val formatter = SimpleDateFormat("MM dd yyyy HH:mm")
            val date = formatter.parse(timeReminderString)
            timeReminder = date.time
            Toast.makeText(requireContext(),"current " +
                    "${SimpleDateFormat("MMM dd yyyy HH:mm").format(System.currentTimeMillis())}," +
                    " time ${date}",Toast.LENGTH_LONG).show()
            notes.forEach {
                overviewModel.sendNotification(it, timeReminder)
                viewModel.addReminder(timeReminder)
            }
            alertDialog.hide()
            overviewModel.clearView()

        }

        dialogViewBinding.cancelAction.setOnClickListener {
            alertDialog.hide()
            overviewModel.clearView()

        }

        if(notes.size==1 && notes[0]!!.timeReminder!=0L) {
            dialogViewBinding.deleteAction.visibility = View.VISIBLE
            dialogViewBinding.deleteAction.setOnClickListener {
                overviewModel.deleteReminder(notes[0])
                viewModel.deleteReminder()
                alertDialog.hide()
                overviewModel.clearView()
            }
        }

    }


    private fun setUpOnBack(){
        binding.contextualActionBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }



    private fun setUpBottomAppbar(){
        setupFunctionSettingNoteBottomSheet()
        setupFunctionEditContentNoteBottomSheet()
    }

    private fun setupFunctionEditContentNoteBottomSheet(){
        val bottomSheetDialog = BottomSheetDialog(requireContext())

        val bindingEditContentNote = FunctionEditContentNoteBottomSheetLayoutBinding.inflate(layoutInflater)

        bottomSheetDialog.setContentView(bindingEditContentNote.root)

        binding.bottomAppBar.setNavigationOnClickListener {
            bottomSheetDialog.show()
        }

        bindingEditContentNote.takePhoto.setOnClickListener {
            takePhoto()
            bottomSheetDialog.hide()
        }

        bindingEditContentNote.addImage.setOnClickListener {
            addImage()
            bottomSheetDialog.hide()
        }
//
//        bindingEditContentNote.recording.setOnClickListener {
//            addRecording()
//            bottomSheetDialog.hide()
//        }

        bindingEditContentNote.checkboxes.setOnClickListener {
            addCheckboxes()
            bottomSheetDialog.hide()
        }
    }

    private fun takePhoto(){

        Toast.makeText(requireContext(),"take photo click",Toast.LENGTH_SHORT).show()
        dispatchTakePictureIntent()
    }


    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) {
            success ->
        if (success) {
            viewModel.addImage()
            viewModel.dispatchImageUri(null)
        }else {
        }
    }

    private fun dispatchTakePictureIntent()  {
        // Create the File where the photo should go
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            // Error occurred while creating the File
            null
        }
        // Continue only if the File was successfully created
        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                "com.example.keep.fileprovider",
                it
            )

            takePicture.launch(photoURI)
            viewModel.dispatchImageUri(photoURI)

        }

    }

    lateinit var currentPhotoPath: String

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }


    private fun addImage(){
        Toast.makeText(requireContext(),"add Image click",Toast.LENGTH_SHORT).show()
        resultLauncher.launch(viewModel.getIntentLibrary())
    }

    private fun addRecording(){
        Toast.makeText(requireContext(),"addRecording click",Toast.LENGTH_SHORT).show()

    }

    private fun addCheckboxes(){
        Toast.makeText(requireContext(),"checkboxes click",Toast.LENGTH_SHORT).show()
        viewModel.addCheckboxes()
    }

    private fun setupFunctionSettingNoteBottomSheet(){
        val bottomSheetDialog = BottomSheetDialog(requireContext())

        val bindingEditContentNote = FunctionSettingNoteBottomSheetLayoutBinding.inflate(layoutInflater)

        bottomSheetDialog.setContentView(bindingEditContentNote.root)

        binding.bottomAppBar.setOnMenuItemClickListener {
            bottomSheetDialog.show()
            true
        }

        bindingEditContentNote.delete.setOnClickListener {
            delete()
            bottomSheetDialog.hide()
        }

        bindingEditContentNote.makeACopy.setOnClickListener {
            makeACopy()
            bottomSheetDialog.hide()
        }


        bindingEditContentNote.labels.setOnClickListener {
            addLabels()
            bottomSheetDialog.hide()
        }
    }

    private fun delete(){
        overviewModel.deleteNotes(listOf(noteWithLabels.note))
        requireActivity().onBackPressed()
    }

    private fun makeACopy(){
        overviewModel.makeACopy(noteWithLabels.note)
        requireActivity().onBackPressed()
    }

    private fun addLabels(){

        viewModel.navigateToLabel = true
        val fragment = LabelFragment()
        val args = Bundle()

        args.putIntArray("noteIdsToLabel", intArrayOf(noteId))
        args.putBoolean("isFromDetail",true)
        fragment.arguments = args

        val ft: FragmentTransaction = parentFragmentManager.beginTransaction()
//                ft.setCustomAnimations(R.anim.zoom_in,R.anim.zoom_out)
        ft.replace(R.id.view, fragment)
        ft.addToBackStack(null)
        ft.commit()
    }

    override fun onPause() {
        super.onPause()
        var isEmptyNote = false
        runBlocking {
            withContext(Dispatchers.IO) {
                val currentNote = viewModel.noteWithLabels.note
                currentNote?.apply {
                    title = binding.title.text.toString()
                    content = binding.content.text.toString()



                    var list = mutableListOf<DataCheckboxes>()

                    checkboxes.forEachIndexed { index, s ->

                        val labelCheckbox =
                            (binding.checkboxGroup.findViewHolderForAdapterPosition(index)
                                    as CheckboxGroupAdapter.ViewHolder).binding.textCheckbox.text.toString()
                        val checked = (binding.checkboxGroup.findViewHolderForAdapterPosition(index)
                                as CheckboxGroupAdapter.ViewHolder).binding.elementCheckbox.isChecked
                        list.add(
                            DataCheckboxes(
                                index,
                                labelCheckbox,
                                checked
                            )
                        )
                    }
                    checkboxes.clear()
                    checkboxes.addAll(list)

                    if(repository.getRawNote(noteId)!=currentNote) {
                        timeEdited = System.currentTimeMillis()

                        if(content.isNotEmpty() || checkboxes.isNotEmpty() ||
                            (checkboxes.isNotEmpty() && !(checkboxes.size == 1 && checkboxes[0].text == "")) ||
                            title.isNotEmpty() || images.isNotEmpty() || viewModel.navigateToLabel){
                                Timber.i("${content.isNotEmpty()} ${ checkboxes.isNotEmpty()} " +
                                        "${(checkboxes.isNotEmpty() && !(checkboxes.size == 1 && checkboxes[0].text == ""))}" +
                                        " ${title.isNotEmpty()} ${images.isNotEmpty()} ${ checkboxes.isNotEmpty()}")
                                repository.insert(currentNote)

                            viewModel.noteWithLabels.labels?.forEach {
                                repository.addLabelToNote(currentNote, it)
                            }
                        } else{
                            isEmptyNote = true
                        }

                    }
                }

            }
        }
        overviewModel.emptyNoteDiscarded.value = isEmptyNote
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.run {


            imageList.adapter = imageAdapter

//            if (noteWithLabels?.note.images!!.isNotEmpty()) {
            imageAdapter.submitList(noteWithLabels.note.images)
            imageList.layoutManager = GridLayoutManager(requireContext(), 3).apply {
                spanSizeLookup = imageAdapter.variableSpanSizeLookup
            }
//            }
        }

//        overviewModel.createNotificationChannel()
    }

    override fun onStop() {
        overviewModel.labelNavigate = listOf()
        viewModel.navigateToLabel = false
        super.onStop()
    }


}