package com.example.keep.overview

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.keep.R
import com.example.keep.database.NoteRepository
import com.example.keep.database.NoteWithLabels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*

data class CustomItemTouchHelper(
    var noteGroup: LiveData<List<NoteWithLabels>>,
    var notesSelected: MutableLiveData<MutableList<NoteWithLabels>>,
    var repository: NoteRepository
) : ItemTouchHelper.Callback() {



    fun updateNoteSelected(noteSelected: MutableLiveData<MutableList<NoteWithLabels>>){
        this.notesSelected = noteSelected
        noteSelected.postValue(noteSelected.value)
    }

    fun clearNotesSelected() {
//        this.notesSelected.value!!.clear()
//        Timber.i(":  ) t đã xoá noteWithLabels đc chọn ${notesSelected.value!!.isEmpty()}")
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val layoutParams = viewHolder!!.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
        if(layoutParams.isFullSpan)
            return 0
        else {
            val dragFlags = ItemTouchHelper.UP
                .or(ItemTouchHelper.DOWN)
                .or(ItemTouchHelper.RIGHT)
                .or(ItemTouchHelper.LEFT)
            val swipeFlags = 0
            return makeMovementFlags(dragFlags, swipeFlags)
        }
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        var startPosition = viewHolder.adapterPosition
        var endPosition = target.adapterPosition
        var numOfNotePin =  noteGroup.value!!.count { note ->
            note.note.priority==1}
        var node = -1
        if(numOfNotePin>0){
            node = numOfNotePin+1
        }

        if(node!=-1){
            if((startPosition < node) && (endPosition < node))  {
                recyclerView.adapter?.notifyItemMoved(startPosition, endPosition)

                startPosition -= 1

                endPosition -=1

                Collections.swap(noteGroup.value, startPosition, endPosition)

                keepScreenWhenSwap(startPosition, endPosition, recyclerView)
                Timber.i("start ${startPosition} end ${endPosition}")
            }
            else if((startPosition > node) && (endPosition > node)){

                recyclerView.adapter?.notifyItemMoved(startPosition, endPosition)

                startPosition -= 2

                endPosition -= 2

                Collections.swap(noteGroup.value, startPosition, endPosition)

                keepScreenWhenSwap(startPosition, endPosition, recyclerView)

            }
        }
        else{
            Collections.swap(noteGroup.value, startPosition, endPosition)
            recyclerView.adapter?.notifyItemMoved(startPosition, endPosition)
            keepScreenWhenSwap(startPosition, endPosition, recyclerView)
            Timber.i("start ${startPosition} end ${endPosition}")

        }


            if (notesSelected.value!!.size<=1) {
                notesSelected.value!!.clear()
                postValueNoteSelected()
            }
//        }
        return false
    }

    private fun keepScreenWhenSwap(startPosition: Int,endPosition: Int,recyclerView: RecyclerView){
        if (endPosition < 6)
            recyclerView.scrollToPosition(0)
        else
            recyclerView.scrollToPosition(startPosition)
    }

    private fun postValueNoteSelected(){
        notesSelected.postValue(
            notesSelected.value
        )
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    override fun clearView(recyclerView: RecyclerView,
                           viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        if (notesSelected.value?.isEmpty() == true) {
            viewHolder.itemView.background?.let {
//                (viewHolder.itemView.background as GradientDrawable)
//                    .setStroke(2, Color.parseColor("#bdbdbd"))
                viewHolder.itemView.background = viewHolder.itemView.context.getDrawable(R.drawable.border)
                viewHolder.itemView.tag = "isNotSelect"
            }
        }

    }

    private fun updateOrderNotes(recyclerView: RecyclerView){
        val numOfNotePin = noteGroup.value!!.count {it.note.priority==1 }

        for(pos in 0 until recyclerView.adapter!!.itemCount) {
            recyclerView.findViewHolderForAdapterPosition(pos)
                ?.let {it ->
                    runBlocking {
                        withContext(Dispatchers.IO){
                            if (it is NotesAdapter.ViewHolder ) {
                                val note = it.binding.noteWithLabels!!.note
                                if(noteGroup.value!!.size==recyclerView.adapter!!.itemCount)
                                    note.position = pos
                                else if(pos<=numOfNotePin){
                                    note.position = pos-1
                                } else{
                                    note.position = pos-2
                                }
                                repository.update(note)
                            }
                        }
                        Timber.i("done update order Notes")
                    }
                }
        }
        Timber.i("done update order Notes")

    }


     fun selectAll(recyclerView: RecyclerView,
                           viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
//        if (notesSelected.value!!.isEmpty()) {
            (viewHolder.itemView.background as GradientDrawable)
                .setStroke(3,Color.BLACK)
            viewHolder.itemView.tag = "isSelected"
//        }
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
//                (viewHolder!!.itemView.background as GradientDrawable).setStroke(3, Color.BLACK)
//                    viewHolder!!.itemView.tag = "isSelected"

            }
    }

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }
}