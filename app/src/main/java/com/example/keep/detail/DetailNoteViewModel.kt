package com.example.keep.detail

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.keep.convertLongToDateString
import com.example.keep.database.DataCheckboxes
import com.example.keep.database.NoteRepository
import com.example.keep.database.NoteWithLabels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber

class DetailNoteViewModel(val noteWithLabels: NoteWithLabels,val repository: NoteRepository) : ViewModel() {

    private var _addButtonClicked = MutableLiveData<Boolean>()
    val addButtonClicked : LiveData<Boolean>
        get() = _addButtonClicked

    private var _stateCheckboxes = MutableLiveData<Boolean>()
    val stateCheckBoxes : LiveData<Boolean>
        get() = _stateCheckboxes

     val timeEditedString = convertLongToDateString(noteWithLabels.note.timeEdited)

    init {
        _stateCheckboxes.value = noteWithLabels.note.checkboxes.isNotEmpty()
    }
    fun addCheckbox(){
        noteWithLabels.note.checkboxes.add(DataCheckboxes(noteWithLabels.note.checkboxes.size,"",))
        Timber.i("checkbox ${noteWithLabels.note.checkboxes}")
        _addButtonClicked.value = true
    }

    fun addCheckboxDone(){
        _addButtonClicked.value = false
    }

    fun updateCheckbox(dataCheckbox: DataCheckboxes){
        noteWithLabels.note.checkboxes.find { it.id == dataCheckbox.id }.apply {
            this!!.text = dataCheckbox.text
        }
    }

    fun removeCheckbox(id : Int){
        noteWithLabels.note.checkboxes.removeIf { it.id == id }
    }

    fun getIntentLibrary() : Intent {

        val getIntent = Intent(Intent.ACTION_GET_CONTENT)
        getIntent.type = "image/*"

        val pickIntent = Intent(
            Intent.ACTION_OPEN_DOCUMENT,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickIntent.type = "image/*"

        val chooserIntent = Intent.createChooser(getIntent,"Select Image")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, Array(1) { pickIntent})

        return chooserIntent
    }


    private var imageUri : String? = null

    fun addImage(uriImage: String=imageUri!!){
        noteWithLabels.note.images.add(uriImage)
        runBlocking {
            withContext(Dispatchers.IO){
                repository.update(noteWithLabels.note)
            }
        }
    }

    fun dispatchImageUri(uri: Uri?){
        imageUri = uri.toString()
    }

    fun addCheckboxes(){
//        noteWithLabels.note.checkboxes!!.add("")

        runBlocking {
            withContext(Dispatchers.IO){
                repository.update(noteWithLabels.note)
            }
        }
        _stateCheckboxes.value = true
    }

    fun addPin() {
        noteWithLabels.note.priority= if(noteWithLabels.note.priority==1) 0 else 1
    }

    fun addReminder(time: Long){
        noteWithLabels.note.timeReminder = time
    }

    fun deleteReminder(){
        noteWithLabels.note.timeReminder = 0L
    }
}