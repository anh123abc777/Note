package com.example.keep

import android.app.Application
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.keep.adapter.LabelsAdapter
import com.example.keep.checkbox.CheckboxGroupAdapter
import com.example.keep.database.*
import com.example.keep.image.ImagesAdapter
import com.example.keep.label.LabelsInNoteIViewAdapter
import com.example.keep.overview.NotesAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomappbar.BottomAppBar
import java.text.SimpleDateFormat

@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data:List<Note>){
    val adapter = recyclerView.adapter as NotesAdapter
//    adapter.submitList(data)
}


@BindingAdapter("listDataImages")
fun bindImagesRecyclerView(recyclerView: RecyclerView,data: ArrayList<String>?){
    if(!data.isNullOrEmpty()) {
        val adapter = recyclerView.adapter as ImagesAdapter
        adapter.submitList(data)
//        if (data.isEmpty()) {
//        }
        recyclerView.visibility = View.VISIBLE
    }else
        recyclerView.visibility = View.GONE

}

@BindingAdapter("listCheckboxes")
fun bindCheckboxesRecyclerView(recyclerView: RecyclerView,data: List<DataCheckboxes>?){
    if (!data.isNullOrEmpty()) {
        var temp = mutableMapOf<String,String>()
        val adapter = recyclerView.adapter as CheckboxGroupAdapter

        adapter.submitList(data)
        recyclerView.visibility = View.VISIBLE
    }else
        recyclerView.visibility = View.GONE

}

@BindingAdapter(value=["listLabels","timeReminder"])
fun bindLabelsRecyclerView(recyclerView: RecyclerView,data: List<Label>?,timeReminder: Long){
    var list = mutableListOf<Label>()

    if (timeReminder != 0L){
        list.add(Label(Int.MIN_VALUE, SimpleDateFormat("MMM dd, HH:mm")
            .format(timeReminder).toString()))
    }
    if(!data.isNullOrEmpty()) {
        list.addAll(data)
    }

    if (list.isNotEmpty()){
        val adapter = recyclerView.adapter as LabelsInNoteIViewAdapter
        adapter.submitList(list)
        recyclerView.visibility = View.VISIBLE
    }else
        recyclerView.visibility = View.GONE

}

//@BindingAdapter("bindDataImages")
//fun bindRecyclerViewImg(recyclerView: RecyclerView, data: List<Resource>){
//    val adapter = recyclerView.adapter as ImagesAdapter
//    adapter.submitList(data)
//}

@BindingAdapter("resourceImage")
fun setResourceImage(imageView: ImageView, uri : Uri){
    imageView.setImageURI(uri)
}

@BindingAdapter("setIcon")
fun setIcon(imageView: ImageView, resource : Int){
    imageView.setImageResource(resource)
}

@BindingAdapter("visibilityContent")
fun setVisibilityContent(textView: TextView,boolean: Boolean){
    textView.visibility= if(boolean) View.GONE else View.VISIBLE
}

@BindingAdapter("visibilityButtonAdd")
fun setVisibilityButtonAdd(imageButton: ImageButton,boolean: Boolean){
    imageButton.visibility = if(boolean) View.VISIBLE else View.GONE
}

@BindingAdapter("visibilityContent")
fun setVisibilityContent(textView: TextView,checkboxGroup : List<DataCheckboxes>?){
    textView.visibility= if(checkboxGroup.isNullOrEmpty()) View.VISIBLE else View.GONE
}
//
//@BindingAdapter("visibilityButtonAdd")
//fun setVisibilityButtonAdd(imageButton: ImageButton,checkboxes : ArrayList<String>?){
//    imageButton.visibility = if(checkboxes!!.isEmpty()) View.GONE else View.VISIBLE
//}

@BindingAdapter(value =["actionBarVisibility","window"])
fun setActionBarVisibility(actionBar: MaterialToolbar, notesSelected : MutableList<NoteWithLabels>, window: Window){
    if(notesSelected.isNotEmpty()){
        actionBar.visibility = View.VISIBLE
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.decorView.systemUiVisibility = View.STATUS_BAR_HIDDEN
        window.statusBarColor = Color.WHITE
    }
    else {
        actionBar.visibility = View.GONE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.statusBarColor = Color.TRANSPARENT
    }
}

@BindingAdapter("filterViewVisibility")
fun setFilterViewVisibility(linearLayout: LinearLayout, isSearching: Boolean){
    linearLayout.visibility = if(isSearching) View.GONE else View.VISIBLE
}

@BindingAdapter("checked")
fun setStateCheckbox(checkbox: CheckBox, isChecked: Boolean){
    checkbox.isChecked = isChecked
}

@BindingAdapter("clickableWithStateNote")
fun setClickableTextEdit(editText: EditText, stateNote : Int){
    editText.isEnabled = stateNote != -1
}

@BindingAdapter("clickableWithStateNote")
fun setClickableBottomBar(bottomAppBar: BottomAppBar, stateNote : Int){
    bottomAppBar.isEnabled = stateNote != -1
    if(stateNote==-1) {
        bottomAppBar.setNavigationOnClickListener {  }
        bottomAppBar.menu.setGroupEnabled(0,false)
    }
}

@BindingAdapter("strikeThruText")
fun setStrikeText(editText: EditText,checked: Boolean){
    editText.paint.isStrikeThruText =  checked
}