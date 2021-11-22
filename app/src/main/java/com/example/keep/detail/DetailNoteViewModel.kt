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

class DetailNoteViewModel(private val note: NoteWithLabels,val repository: NoteRepository) : ViewModel() {

    private var _noteWithLabels = MutableLiveData<NoteWithLabels>()
    val noteWithLabels : LiveData<NoteWithLabels>
        get() = _noteWithLabels

    private var _addButtonClicked = MutableLiveData<Boolean>()
    val addButtonClicked : LiveData<Boolean>
        get() = _addButtonClicked

    private var _stateCheckboxes = MutableLiveData<Boolean>()
    val stateCheckBoxes : LiveData<Boolean>
        get() = _stateCheckboxes

     val timeEditedString = noteWithLabels.value?.note?.timeEdited?.let { convertLongToDateString(it) }
    var navigateToLabel = false


    init {
        _noteWithLabels.value = note
        _stateCheckboxes.value = noteWithLabels.value!!.note.checkboxes.isNotEmpty()
    }

    fun setNoteWithLabelValue(noteWithLabels: NoteWithLabels){
        _noteWithLabels.value = noteWithLabels
    }

    fun addCheckbox(){
        noteWithLabels.value!!.note.checkboxes.add(DataCheckboxes(noteWithLabels.value!!.note.checkboxes.size,"",))
        _addButtonClicked.value = true
    }

    fun addCheckboxDone(){
        _addButtonClicked.value = false
    }

    fun updateCheckbox(dataCheckbox: DataCheckboxes){
        _noteWithLabels.value!!.note.checkboxes.find { it.id == dataCheckbox.id }.apply {
            this!!.text = dataCheckbox.text
        }
    }

    fun removeCheckbox(id : Int){
        _noteWithLabels.value!!.note.checkboxes.removeIf { it.id == id }
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
        _noteWithLabels.value!!.note.images.add(uriImage)
        runBlocking {
            withContext(Dispatchers.IO){
                repository.update(_noteWithLabels.value!!.note)
            }
        }
    }

    fun dispatchImageUri(uri: Uri?){
        imageUri = uri.toString()
    }

    fun addCheckboxes(){
//        noteWithLabels.note.checkboxes!!.add("")

        _noteWithLabels.value!!.note.checkboxes.add(DataCheckboxes(noteWithLabels.value!!.note.checkboxes.size,"",))

        _stateCheckboxes.value = true
    }

    fun addPin() {
        _noteWithLabels.value!!.note.priority= if(noteWithLabels.value!!.note.priority==1) 0 else 1
    }

    fun addReminder(time: Long){
        _noteWithLabels.value!!.note.timeReminder = time
        _noteWithLabels.postValue(
            _noteWithLabels.value
        )
    }

    fun deleteReminder(){
        noteWithLabels.value!!.note.timeReminder = 0L
    }

    fun deleteImage(imageUri : String){
        _noteWithLabels.value!!.note.images.removeIf { it == imageUri }
    }
}