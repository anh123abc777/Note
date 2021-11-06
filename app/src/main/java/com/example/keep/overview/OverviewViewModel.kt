package com.example.keep.overview

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.keep.R
import com.example.keep.adapter.DataFilterAdapter
import com.example.keep.database.*
import com.example.keep.databinding.DateTimePickerBinding
import com.example.keep.notification.ReminderBroadcast
import kotlinx.coroutines.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class OverviewViewModel(val activity: Activity, private val application: Application) : ViewModel() {

    private val ARCHIVE = 1
    private val NORMAL = 0
    private val TRASH = -1
    var recyclerViewState : Parcelable? = null
    private var _noteNavigate = MutableLiveData<Int>()
    private var _notesSelected = MutableLiveData<MutableList<NoteWithLabels>>()
    private var _optionView = MutableLiveData<OptionView>()
    private var _navigateToSearch = MutableLiveData<Boolean>()
    private var _navigateToLabelSetting = MutableLiveData<Boolean>()
    private var _needClearView = MutableLiveData<Boolean>()
    private var _currentNotesInView = MutableLiveData<Int>()
    private val repository = NoteRepository(NoteDatabase.getInstance(application).noteDao)
    val bundleFromFragmentBToFragmentA = MutableLiveData<Bundle>()

    var tamp = 3

    private val viewModeJob = Job()
    private val coroutineScope = CoroutineScope(viewModeJob + Dispatchers.Main)

    var normalNotes : LiveData<List<NoteWithLabels>> = repository.allNotes
    var trashNotes : LiveData<List<NoteWithLabels>> = repository.allNotesTrash
    var archiveNotes : LiveData<List<NoteWithLabels>> = repository.allNotesArchive


    val noteNavigate : LiveData<Int>
        get() = _noteNavigate

    val navigateToSearch: LiveData<Boolean>
        get() = _navigateToSearch

    val navigateToLabelSetting: LiveData<Boolean>
        get() = _navigateToLabelSetting

    val notesSelected : LiveData<MutableList<NoteWithLabels>>
        get() = _notesSelected

    val optionView : LiveData<OptionView>
        get() = _optionView

    val needClearView : LiveData<Boolean>
        get() = _needClearView

    val currentNotesInView : LiveData<Int>
        get() = _currentNotesInView

    var callback : CustomItemTouchHelper
    var itemTouchHelper : ItemTouchHelper

//    private var callbackPriority : CustomItemTouchHelper
//    var itemTouchHelperPriority : ItemTouchHelper

    init {
        createNotificationChannel()
        _currentNotesInView.value = NORMAL
        _notesSelected.value = mutableListOf()
        _optionView.value = OptionView.GRIDVIEW
            callback = CustomItemTouchHelper(normalNotes,_notesSelected,repository)
            itemTouchHelper = ItemTouchHelper(callback)

    }

    fun changeOptionView(){
        _optionView.value = if(_optionView.value == OptionView.GRIDVIEW)
                        OptionView.LISTVIEW
                    else OptionView.GRIDVIEW
    }

    fun onClick(note: NoteWithLabels?, isSelected: Boolean){

        when {
            isSelected || _notesSelected.value!!.size > 0 -> {
                when {
                    _notesSelected.value!!.contains(note) -> _notesSelected.value!!.remove(note)

                    else -> _notesSelected.value!!.add(note!!)
                }
                postValueNoteSelected()
                callback.updateNoteSelected(_notesSelected)

            }

            else ->
                if (note != null) {
                    _noteNavigate.value = note.note.noteId
                }
                else{
                    _noteNavigate.value = Int.MIN_VALUE
                }
        }

    }

        fun doneNavigating(){
        _noteNavigate.value = null
    }

    fun clearSelected(){
        _notesSelected.value!!.clear()
        postValueNoteSelected()
    }

    private fun postValueNoteSelected(){
        _notesSelected.postValue(
            _notesSelected.value
        )
    }

    fun addPin(){

        when(_currentNotesInView.value) {
            NORMAL ->
                if (checkPinOrUnpin()) {
                    _notesSelected.value!!.forEach { note ->
                        note.note.priority = 0
                        val numOfNotePin = normalNotes.value!!.count { it.note.priority == 1 }
                        while (normalNotes.value!!.indexOf(note) < numOfNotePin) {
                            Collections.swap(
                                normalNotes.value,
                                normalNotes.value!!.indexOf(note),
                                normalNotes.value!!.indexOf(note) + 1
                            )
                        }
                    }
                } else {
                    _notesSelected.value!!.forEach { note ->
                        if (note.note.priority != 1)
                            while (normalNotes.value!!.indexOf(note) != 0) {
                                Collections.swap(
                                    normalNotes.value,
                                    normalNotes.value!!.indexOf(note),
                                    normalNotes.value!!.indexOf(note) - 1
                                )
                            }
                        note.note.priority = 1

                    }
                    callback.noteGroup = normalNotes
                }
            else ->
                if (checkPinOrUnpin()) {
                    _notesSelected.value!!.forEach { note ->
                        note.note.priority = 0
                        val numOfNotePin = archiveNotes.value!!.count { it.note.priority == 1 }
                        while (archiveNotes.value!!.indexOf(note) < numOfNotePin) {
                            Collections.swap(
                                archiveNotes.value,
                                archiveNotes.value!!.indexOf(note),
                                archiveNotes.value!!.indexOf(note) + 1
                            )
                        }
                    }
                } else {
                    _notesSelected.value!!.forEach { note ->
                        if (note.note.priority != 1)
                            while (archiveNotes.value!!.indexOf(note) != 0) {
                                Collections.swap(
                                    archiveNotes.value,
                                    archiveNotes.value!!.indexOf(note),
                                    archiveNotes.value!!.indexOf(note) - 1
                                )
                            }
                        note.note.priority = 1

                    }
                }
        }

    }

    fun checkPinOrUnpin(): Boolean = _notesSelected.value!!.all { it.note.priority ==1 }


    fun getIntentLibrary() : Intent{

        val getIntent = Intent(Intent.ACTION_GET_CONTENT)
        getIntent.type = "image/*"

        val pickIntent = Intent(Intent.ACTION_OPEN_DOCUMENT,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickIntent.type = "image/*"

        val chooserIntent = Intent.createChooser(getIntent,"Select Image")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, Array(1) { pickIntent })

        return chooserIntent
    }

    fun addNote(image : String="",checkboxGroup: ArrayList<DataCheckboxes> = arrayListOf()) {
//        runBlocking {
//            withContext(Dispatchers.IO) {
//                val position = repository.getLastPosition()+1
//                if (image == "")
//                    repository.insert(Note(checkboxes = checkboxGroup,position = position))
//                else
//                    repository.insert(
//                        Note(
//                            images = arrayListOf(image!!),
//                            checkboxes = checkboxGroup,
//                            position = position
//                        )
//                    )
//            }
//        }
//
//        val idNewNote = runBlocking {
//            withContext(Dispatchers.IO){
//                repository.getLastNote().noteId
//            }
//        }
//        Timber.i("id new Note ${idNewNote}")
//
//        val newNoteWithLabels = runBlocking {
//            withContext(Dispatchers.IO){
//                repository.get(idNewNote)
//            }
//        }
//        newNoteWithLabels

        onClick(null,isSelected = false)

        Timber.i("add noteWithLabels")
    }

    private var noteDelete = listOf<Note>()
    fun deleteNotes(note: List<Note>?=null){
         noteDelete= if(note==null){
            _notesSelected.value!!.map { it.note }
        }else{
            note!!
        }
        if (noteDelete!=null) {
//            noteDelete.forEach { note ->
//                coroutineScope.launch(Dispatchers.IO) {
//                    repository.delete(note)
//                }
//            }
            noteDelete.forEach { note ->
                coroutineScope.launch(Dispatchers.IO) {
                    note.state = -1
                    repository.update(note)
                }
            }
        }
    }

    fun undoDeleteNotes(){
        if (noteDelete!=null){
            noteDelete.forEach { note ->
                coroutineScope.launch(Dispatchers.IO) {
                    note.state = 0
                    repository.update(note)
                }
            }
        }
    }

    fun storageNotes(note: List<Note>?=null){
        val notesArchive= if(note==null){
            _notesSelected.value!!.map { it.note }
        }else{
            note!!
        }

        if (notesArchive!=null){
            notesArchive.forEach { note ->
                coroutineScope.launch(Dispatchers.IO){
                    note.state = 1
                    repository.update(note)
                }
            }
        }
    }

    fun makeACopy(note: Note? =null){
        val noteCopy: Note = if(note==null && _notesSelected.value!!.size==1){
            _notesSelected.value!![0].note
        }else{
            note!!
        }
        if(noteCopy!=null){

            val newId = runBlocking {
                withContext(Dispatchers.IO){
                    repository.getLastNote().noteId+1
                }
            }

            noteCopy.noteId = newId

            coroutineScope.launch(Dispatchers.IO) {
                repository.insert(noteCopy)
            }
        }
    }

    fun unarchive(){
        _notesSelected.value!!.forEach { it ->
            coroutineScope.launch(Dispatchers.IO){
                it.note.state = 0
                repository.update(it.note)
            }
        }
    }

    fun deleteForever(){
        _notesSelected.value!!.forEach { it ->
            coroutineScope.launch(Dispatchers.IO){
                repository.delete(it.note)
            }
        }
    }

    fun restore(){
        _notesSelected.value!!.forEach { it ->
            coroutineScope.launch(Dispatchers.IO){
                it.note.state = 0
                repository.update(it.note)
            }
        }
    }

    fun emptyTrash(){
        trashNotes.value!!.forEach {
            coroutineScope.launch(Dispatchers.IO){
                repository.delete(it.note)
            }
        }
    }

    fun navigateToSearchView(){
        _navigateToSearch.value = true
    }

    fun doneNavigateToSearchView(){
        _navigateToSearch.value = false
    }

    private val labelsRepository = LabelRepository(NoteDatabase.getInstance(application).labelDao)
    val labels = labelsRepository.allLabelWithNotes
    fun dispatchFilterByLabel(): MutableList<DataFilterAdapter>{
        val data = mutableListOf<DataFilterAdapter>()
        labels.value!!.forEach {
            if(it.notes.isNotEmpty())
                data.add(DataFilterAdapter(it.label.labelName, R.drawable.ic_outline_label_24))
        }
        return data
    }

    fun dispatchFilterByType(): MutableList<DataFilterAdapter> {
        return mutableListOf(
            DataFilterAdapter("Lists", R.drawable.ic_outline_check_box_24),
            DataFilterAdapter("Images", R.drawable.ic_outline_image_24),
            DataFilterAdapter("Voice", R.drawable.ic_baseline_mic_none_24)
        )
    }

    private var _isSearching = MutableLiveData<Boolean>()
    val isSearching: LiveData<Boolean>
        get() = _isSearching

    fun stopSearching(){
        _isSearching.value = false
    }

    fun startSearching(){
        _isSearching.value = true
    }

    fun navigateToLabelSetting(){
        _navigateToLabelSetting.value = true
    }

    fun doneNavigateToLabelSetting(){
        _navigateToLabelSetting.value = false
    }

    fun deleteReminder(note: Note){
//        private fun deleteReminder(note: Note){
            val notificationManager = NotificationManagerCompat.from(application)
            notificationManager.cancel(note.noteId)
//        }
       normalNotes.value!!.find { it.note==note }!!.note.timeReminder = 0L
    }

    override fun onCleared() {
        super.onCleared()
        viewModeJob.cancel()
    }


    @SuppressLint("SimpleDateFormat", "SetTextI18n")
     fun showDateTimePicker(notes : List<Note>,layoutInflater: LayoutInflater) {
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

            val timePicker = TimePickerDialog(application, { _, selectedHour, selectedMinute ->
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

            val datePickerDialog = DatePickerDialog(application,
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
        val timeReminderString = mMonth.toString() +" "+ mDay.toString() +" "+  mYear.toString()+" " +
                hour.toString()+ ":" + minute.toString()
        dialogViewBinding.saveAction.setOnClickListener {

            val formatter = SimpleDateFormat("MM dd yyyy HH:mm")
            val date = formatter.parse(timeReminderString)
            timeReminder = date.time

            notes.forEach {

                sendNotification(it, timeReminder)
            }
            alertDialog.hide()
            clearView()

        }

        dialogViewBinding.cancelAction.setOnClickListener {
            alertDialog.hide()
            clearView()

        }

        if(notes.size==1 && notes[0]!!.timeReminder!=0L) {
            dialogViewBinding.deleteAction.visibility = View.VISIBLE
            dialogViewBinding.deleteAction.setOnClickListener {
                deleteReminder(notes[0])
                alertDialog.hide()
                clearView()
            }
        }

    }

    fun clearView(){
        _needClearView.value = true
    }

    fun cleanedView(){
        _needClearView.value = false
    }

    fun sendNotification(note: Note, timeReminder: Long){

        val intent = Intent(application, ReminderBroadcast::class.java)

        intent.putExtra("id", note.noteId)
        intent.putExtra("timeReminder",timeReminder)
        intent.putExtra("title",note.title)
        intent.putExtra("content",note.content)

        val pendingIntent = PendingIntent.getBroadcast(application, note.noteId,intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager = activity.getSystemService(android.content.Context.ALARM_SERVICE) as AlarmManager

        alarmManager.set(AlarmManager.RTC_WAKEUP,timeReminder,pendingIntent)
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "this is channel"
            val description = "this is description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("notifyId", name, importance)
            channel.description = description


            val notificationManager =
                activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    fun setMenuItemForArchiveNotes(){
        _currentNotesInView.value = ARCHIVE
    }

    fun setMenuItemForTrashNotes(){
        _currentNotesInView.value = TRASH
    }

    fun setMenuItemForNormalNotes(){
        _currentNotesInView.value = NORMAL
    }

    enum class OptionView{GRIDVIEW,LISTVIEW}


}