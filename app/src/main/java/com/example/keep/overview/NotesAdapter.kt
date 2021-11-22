package com.example.keep.overview


import android.annotation.SuppressLint
import android.app.Application
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.*
import android.widget.Toast
import androidx.core.view.allViews
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.*
import com.example.keep.checkbox.CheckboxGroupAdapter
import com.example.keep.database.*
import com.example.keep.databinding.ElementNoteBinding
import com.example.keep.databinding.HeaderBinding
import com.example.keep.image.ImageAdapter
import com.example.keep.label.LabelsInNoteIViewAdapter
import kotlinx.coroutines.*
import timber.log.Timber


private val ITEM_VIEW_TYPE_HEADER = 0
private val ITEM_VIEW_TYPE_ITEM = 1

class NotesAdapter(private val clickListener : OnClickListener, private val application: Application) : ListAdapter<NotesAdapter.DataItem,RecyclerView.ViewHolder>(DiffCallback){
    companion object DiffCallback : DiffUtil.ItemCallback<DataItem>() {

        override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem===newItem
        }

        override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem==newItem
        }
    }

    class ViewHolder private constructor
        (var binding: ElementNoteBinding, private val parent: ViewGroup) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("ClickableViewAccessibility")
        fun bind(note: NoteWithLabels, clickListener: OnClickListener){

//            binding.root.post {
//                val params: ViewGroup.LayoutParams = binding.root.layoutParams
//                params.height = maxHeight
//                binding.root.layoutParams = params
//            }

            binding.executePendingBindings()
            binding.noteWithLabels = note

            setAdapterEachView(note)
            setMaxHeightEachView()

            val gesture = createGesture(clickListener,note)

//            binding.root.setOnTouchListener { _, motionEvent ->
//                gesture.onTouchEvent(motionEvent)
//                true
//            }

            itemView.allViews.all {
                it.setOnTouchListener { _, motionEvent ->
                    gesture.onTouchEvent(motionEvent)
                    true
                }
                true
            }

        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ElementNoteBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding,parent)
            }
        }

        private fun setMaxHeightEachView(){
            binding.root.post {
            val maxHeight = Resources.getSystem().displayMetrics.heightPixels/3


                binding.checkboxGroup.post {
                    if(binding.checkboxGroup.height > maxHeight) {
                        val params: ViewGroup.LayoutParams = binding.checkboxGroup.layoutParams
                        params.height = maxHeight
                        binding.checkboxGroup.layoutParams = params
                    }
                }

                binding.images.post {
                    val params: ViewGroup.LayoutParams = binding.images.layoutParams
                    if (binding.images.height > maxHeight) {
                        params.height = maxHeight
                        binding.images.layoutParams = params
                    }
                }
//
            }
        }

        private fun setAdapterEachView(note: NoteWithLabels){
            setAdapterImgView(note)
            setAdapterLabel(note)
            setAdapterCheckboxGroup(note)
        }
//
        private fun setAdapterImgView(note: NoteWithLabels){
//            if (note.note.images!!.isNotEmpty()) {
                val adapter = ImageAdapter(false,ImageAdapter.OnClickListener{ _, _ ->  })
                binding.images.adapter = adapter
                binding.images.setHasFixedSize(false)

//                adapter.submitList(note.note.images)
//            }
        }
//
        private fun setAdapterLabel(note: NoteWithLabels){
            val adapter = LabelsInNoteIViewAdapter()
            binding.labels.adapter = adapter
            binding.labels.setHasFixedSize(false)
        }
//
        private fun setAdapterCheckboxGroup(note: NoteWithLabels){
//            if(note.note.checkboxes!!.isNotEmpty()){
                val adapter = CheckboxGroupAdapter(clickListener = null,false)
                binding.checkboxGroup.adapter = adapter
                binding.checkboxGroup.apply {
                   setHasFixedSize(false)
                }
//                binding.content.visibility = View.GONE

//                var dataCheckboxes = mutableListOf<CheckboxGroupAdapter.DataCheckboxes>()
//                note.note.checkboxes.forEachIndexed { index, text ->
//                    dataCheckboxes.add(CheckboxGroupAdapter.DataCheckboxes(index,text))
//                }
//                adapter.submitList(dataCheckboxes)
//            }
        }


        private fun createGesture(clickListener : OnClickListener, note: NoteWithLabels) =
         GestureDetector(itemView.context,object : GestureDetector.SimpleOnGestureListener() {

            override fun onLongPress(e: MotionEvent?) {
                clickListener.onLongPress(note)
                onChangeState()
            }

            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                clickListener.onClick(note)
                onChangeState()
                return true
            }

            private fun onChangeState(){
                if(binding.frameNote.tag=="isNotSelect"){
                    val drawable = itemView.background as GradientDrawable
                    drawable.setStroke(3,Color.BLACK)
//                    (itemView.background as GradientDrawable).setStroke(3,Color.BLUE)
                    itemView.tag = "isSelected"

                } else
                if(binding.frameNote.tag=="isSelected") {
                    (itemView.background as GradientDrawable).setStroke(
                        2,
                        Color.parseColor("#bdbdbd")
                    )
                    itemView.tag = "isNotSelect"
                }
            }
        })
    }


    override fun getItemId(position: Int): Long {
        return getItem(position).id.toLong()
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder   {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> TextViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType ${viewType}")
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val noteItem = getItem(position) as DataItem.NoteItem
                holder.bind(noteItem.note, clickListener)
            }

            is TextViewHolder -> {
                val textHeader = getItem(position) as DataItem.Header
                holder.bind(textHeader.text)
            }
        }

//        holder.bind(getItem(position),onClickListener)
    }


//    override fun getItemId(position: Int): Long {
//        return getItem(position).id.toLong()
//    }

//    DataItem.NoteItem
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Header -> ITEM_VIEW_TYPE_HEADER
            else -> ITEM_VIEW_TYPE_ITEM
        }
    }

    private val adapterScope = CoroutineScope(Dispatchers.Default)
    fun addHeaderAndSubmitList(list: List<NoteWithLabels>?) {
        adapterScope.launch {
            val items = when (list) {
                null -> listOf(DataItem.Header("other"))
                else -> if(list.any { (it.note.priority == 1) }) {

                            listOf(DataItem.Header("pinned")) +

                            list.filter {
                                it.note.priority == 1
                            }.map { DataItem.NoteItem(it) } +

                            listOf(DataItem.Header("other")) +

                            list.filter { it.note.priority == 0 }.map { DataItem.NoteItem(it) }
                } else{
                            list.filter { it.note.priority == 0 }.map { DataItem.NoteItem(it) }
                }
            }
            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    private fun submitListNotHeader(list: List<NoteWithLabels>?){

        adapterScope.launch {
            val items = when (list) {
                   null -> listOf(DataItem.Header("no match normalNotes"))
                   else -> list.filter { it.note.priority == 0 }.map { DataItem.NoteItem(it) }

            }
            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    private val noteRepository = NoteRepository(NoteDatabase.getInstance(application).noteDao)

    private val _allNotes = MutableLiveData<List<NoteWithLabels>>()
    private val allNotes : LiveData<List<NoteWithLabels>>
        get() = _allNotes

    fun initNotes(data : List<NoteWithLabels>){
        _allNotes.value = data
    }

    fun filterQuery(text: String) {
        var text = text
        var items = mutableListOf<NoteWithLabels>()
        if (text.isNotEmpty()) {
            text = text.lowercase()
            for (item in allNotes.value!!) {
                if (item.note.title.lowercase().contains(text) ||
                    item.note.content.lowercase().contains(text)) {
                    items.add(item)
                }
            }
        }
        submitListNotHeader(items)
        notifyDataSetChanged()
        Timber.i("allNotes ${items}")
    }

    private val labelRepository = LabelRepository(NoteDatabase.getInstance(application).labelDao)
    lateinit var allLabels: LiveData<List<LabelWithNotes>>

    fun initLabels(data: LiveData<List<LabelWithNotes>>){
        allLabels = data
    }

    fun filterLabel(text: String?,isSearching: Boolean) {
        var text = text

        var items = mutableListOf<NoteWithLabels>()

        for (labelWithNotes in allLabels.value!!) {
            if (labelWithNotes.label.labelName == text) {
                labelWithNotes.notes.forEach {
                    runBlocking {
                        withContext(Dispatchers.IO) {
                            val noteWithLabels = noteRepository.get(it.noteId)
                            if(noteWithLabels.note.state == 0)
                                items.add(noteWithLabels)
                        }
                    }
                }
            }
        }

        if (text==null){
            items.addAll(_allNotes.value!!)
        }

        if(isSearching) {
            _allNotes.value = items
        }
//        submitListNotHeader(items)
        addHeaderAndSubmitList(items)

//        notifyDataSetChanged()
    }

    fun filterReminders(){
        var items = mutableListOf<NoteWithLabels>()

        items.addAll(allNotes.value!!.filter {
            it.note.timeReminder!=0L
        })

        addHeaderAndSubmitList(items)

    }

    fun filterType(text: String){
        var items = mutableListOf<NoteWithLabels>()
        when(text){
            "Images" -> {
                for (noteWithLabels in allNotes.value!!) {
                    if (noteWithLabels.note.images.isNotEmpty()) {
                        items.add(noteWithLabels)
                    }
                }
            }

            "Lists" -> {
                for (noteWithLabels in allNotes.value!!) {
                    if (noteWithLabels.note.checkboxes.isNotEmpty()) {
                        items.add(noteWithLabels)
                    }
                }
            }

            "Voice" -> {
                for (noteWithLabels in allNotes.value!!) {
                    if (noteWithLabels.note.images.isNotEmpty()) {
                        items.add(noteWithLabels)
                    }
                }
            }

            else -> Toast.makeText(application,"wtf is this $text",Toast.LENGTH_SHORT).show()
        }
        Toast.makeText(application,"$text",Toast.LENGTH_SHORT).show()


        _allNotes.value = items
        submitListNotHeader(items)
        notifyDataSetChanged()
    }

    fun showNotesTrash(notesTrash: List<NoteWithLabels>){
        submitListNotHeader(notesTrash)
        notifyDataSetChanged()
    }

    fun showNotesArchive(notesArchive: List<NoteWithLabels>){
        addHeaderAndSubmitList(notesArchive)
        notifyDataSetChanged()
    }

    class OnClickListener(val clickListener : (note: NoteWithLabels, isSelected : Boolean) -> Unit){
        fun onClick(note : NoteWithLabels) = clickListener(note,false)
        fun onLongPress(note : NoteWithLabels) = clickListener(note,true)
    }

    sealed class DataItem {
        data class NoteItem(val note: NoteWithLabels): DataItem() {
            override val id = note.note.noteId
        }

        data class Header(val text : String): DataItem() {
            override val id = Int.MIN_VALUE
        }

        abstract val id: Int
    }

    class TextViewHolder private constructor
        (private val binding: HeaderBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(text : String){
                binding.text.text = text
            }

            companion object {
            fun from(parent: ViewGroup): TextViewHolder {


                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = HeaderBinding.inflate(layoutInflater, parent, false)
//                val view = layoutInflater.inflate(R.layout.header, parent, false)
                val layoutParams = StaggeredGridLayoutManager.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.isFullSpan = true
                binding.root.layoutParams = layoutParams
                return TextViewHolder(binding)
            }
        }
    }



}