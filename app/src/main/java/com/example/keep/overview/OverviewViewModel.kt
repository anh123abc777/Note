package com.example.keep.overview

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.keep.R
import com.example.keep.adapter.DataFilterAdapter
import com.example.keep.database.*
import com.example.keep.notification.ReminderBroadcast
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

class OverviewViewModel(val activity: Activity, private val application: Application) : ViewModel() {

    private val ARCHIVE = 1
    private val NORMAL = 0
    private val TRASH = -1
    var recyclerViewState : Parcelable? = null
    private var _noteNavigate = MutableLiveData<Int>()
    var labelNavigate = listOf<Label>()
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

    var emptyNoteDiscarded = MutableLiveData<Boolean>()

    init {
        createNotificationChannel()
        _currentNotesInView.value = NORMAL
        _notesSelected.value = mutableListOf()
        _optionView.value = OptionView.GRIDVIEW
            callback = CustomItemTouchHelper(normalNotes,_notesSelected,repository)
            itemTouchHelper = ItemTouchHelper(callback)
    }

    fun changeOptionView(){
        _optionView.value = if(_optionView.value != OptionView.GRIDVIEW)
                     OptionView.GRIDVIEW
                    else OptionView.LISTVIEW
    }

    fun onClick(note: NoteWithLabels, isSelected: Boolean){

        when {
            isSelected || _notesSelected.value!!.size > 0 -> {
                when {
                    _notesSelected.value!!.contains(note) -> _notesSelected.value!!.remove(note)

                    else -> _notesSelected.value!!.add(note!!)
                }
                postValueNoteSelected()
                callback.updateNoteSelected(_notesSelected)

            }

            else -> _noteNavigate.value = note.note.noteId


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

        var newNote : NoteWithLabels =
            when {
                (image != "") -> {
                    runBlocking {
                        withContext(Dispatchers.IO) {

                            val firstPosition = repository.getFirstPosition()-1

                            repository.insert(
                                Note(images = arrayListOf(image),position = firstPosition)
                            )
                        }
                    }

                    runBlocking {
                        withContext(Dispatchers.IO) {
                            if(labelNavigate.isNotEmpty()){
                                repository.addLabelToNote(repository.getLastNote(),labelNavigate[0])
                            }
                        }
                    }

                    runBlocking {
                        withContext(Dispatchers.IO){
                            repository.get(repository.getLastNote().noteId)
                        }
                    }
                }

                checkboxGroup.isNotEmpty() ->
                    NoteWithLabels(
                        Note(-1), listOf()
                    )

                else -> NoteWithLabels(Note(-2), listOf())
            }

        onClick(newNote,isSelected = false)

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
                runBlocking(Dispatchers.IO) {
                    note.state = -1
                    repository.update(note)
                }
            }
        }
    }

    fun undoDeleteNotes(){
        if (noteDelete!=null){
            noteDelete.forEach { note ->
                runBlocking(Dispatchers.IO) {
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
               runBlocking(Dispatchers.IO){
                    note.state = 1
                    repository.update(note)
                }
            }
        }
    }

    fun makeACopy(note: NoteWithLabels? =null){
        val notesCopy: MutableList<NoteWithLabels> = if(note==null){
            _notesSelected.value!!
        }else{
            mutableListOf(note)
        }

        notesCopy.forEach { noteCopy ->
            val newId = runBlocking {
                withContext(Dispatchers.IO) {
                    repository.getLastNote().noteId + 1
                }
            }

            noteCopy.note.noteId = newId

           runBlocking {
               withContext(Dispatchers.IO) {
                   repository.insert(noteCopy.note)
                   noteCopy.labels?.forEach { label ->
                       repository.addLabelToNote(noteCopy.note, label)
                   }
               }
           }
        }
    }

    fun unarchive(){
        _notesSelected.value!!.forEach { it ->
            runBlocking(Dispatchers.IO){
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
            runBlocking(Dispatchers.IO){
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
//            DataFilterAdapter("Voice", R.drawable.ic_baseline_mic_none_24)
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

    fun setUpLabelNavigate(labels: List<Label>?){
        labelNavigate = labels ?: listOf()
    }

    enum class OptionView{GRIDVIEW,LISTVIEW}



}