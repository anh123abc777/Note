package com.example.keep

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.net.toUri
import androidx.core.view.GravityCompat
import androidx.core.view.forEach
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.*
import com.example.keep.database.DataCheckboxes
import com.example.keep.database.Note
import com.example.keep.database.NoteDatabase
import com.example.keep.database.NoteRepository
import com.example.keep.databinding.ActivityMainBinding
import com.example.keep.databinding.DateTimePickerBinding
import com.example.keep.detail.DetailNoteFragment
import com.example.keep.label.LabelFragment
import com.example.keep.labelsetting.LabelSettingFragment
import com.example.keep.overview.NotesAdapter
import com.example.keep.overview.OverviewViewModel
import com.example.keep.overview.OverviewViewModelFactory
import com.example.keep.search.SearchFragment
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var viewModel: OverviewViewModel
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var adapter : NotesAdapter
    private lateinit var noteRepository: NoteRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        noteRepository = NoteRepository(NoteDatabase.getInstance(application).noteDao)

        val factory = OverviewViewModelFactory(this,application)
        viewModel = ViewModelProvider(this,factory).get(OverviewViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.listItem.apply {
            layoutManager = StaggeredGridLayoutManager(
                2, StaggeredGridLayoutManager.VERTICAL
            ).apply {
                gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
            }
            setHasFixedSize(true)
        }


        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == Activity.RESULT_OK){

                val data : String? = result.data?.dataString
                val contentResolver = application.applicationContext.contentResolver

                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                // Check for the freshest data.
                contentResolver.takePersistableUriPermission(data!!.toUri(), takeFlags)
                viewModel.addNote(data)
            }
        }

        observeNotesSelected()

        observeNavigateToDetail()

        setUpNotesAdapter()

        setItemClickBottomAppBar()

        setItemCLickContextualActionbar()

        setupOptionView()

        navigateToSearchViewListener()

        submitList()

        setUpToolbar()

        setUpMenuDrawer()

        observeTrashAndArchiveNotes()

        observeClearView()

        viewModel.emptyNoteDiscarded.observe(this){
            if(it){
                Snackbar.make(binding.root, "Empty note discarded", Snackbar.LENGTH_SHORT)
                    .show()
                viewModel.emptyNoteDiscarded.value = false
            }
        }

    }


    @SuppressLint("WrongConstant")
    private fun observeNavigateToDetail(){
        viewModel.noteNavigate.observe(this){
            if(it!=null) {
//                findNavController().navigate(
//                    OverviewFragmentDirections

                clearView()
                updateOrderNotes()

                binding.view.visibility = View.VISIBLE
                val fragment = DetailNoteFragment()
                val args = Bundle()
                args.putInt("noteId", it)

                fragment.arguments = args

                val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
//                ft.setCustomAnimations(R.anim.zoom_in,R.anim.zoom_out)
                ft.replace(R.id.view, fragment)
                binding.drawerLayout.setBackgroundColor(Color.WHITE)
                ft.addToBackStack(null)
                ft.commit()

                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.START)
                viewModel.doneNavigating()
            }

        }
    }



    @SuppressLint("WrongConstant")
    override fun onBackPressed() {

        when(supportFragmentManager.backStackEntryCount){
            0 -> super.onBackPressed()
            2 -> {
                supportFragmentManager.popBackStack()
            }
            else ->{
                supportFragmentManager.popBackStack()
                binding.view.visibility = View.GONE
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED,Gravity.START)
                viewModel.clearView()
            }
        }
    }

    private fun setUpNotesAdapter(){
        adapter = NotesAdapter(NotesAdapter.OnClickListener{note,isSelected ->
            viewModel.onClick(note,isSelected)
        },application)

        binding.listItem.adapter = adapter

    }

    /*****  submit list *****/

    private fun submitList(){

        viewModel.normalNotes.observe(this) {
            it?.let {
                if (viewModel.currentNotesInView.value== 0) {
                    adapter.addHeaderAndSubmitList(it)
                }
                adapter.initNotes(it)

            }
        }


//        viewModel.recyclerViewState.observe(requireActivity()){
//       viewModel.recyclerViewState.observe(requireActivity()){
//            binding.listItem.layoutManager!!.onRestoreInstanceState(viewModel.recyclerViewState.value)
//                        Toast.makeText(context,"save state",Toast.LENGTH_LONG).show()
//
//        }
//        }
    }

    /***** Toolbar *****/
    private fun setUpToolbar(){
        binding.toolbar.setNavigationOnClickListener {
            openDrawer()

        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId) {
                R.id.optionView -> viewModel.changeOptionView()

                R.id.empty_trash -> viewModel.emptyTrash()

                R.id.search_bar -> viewModel.navigateToSearchView(
                )
            }
            viewModel.optionView.observe(this){
                when(it){
                    OverviewViewModel.OptionView.GRIDVIEW ->menuItem.setIcon(R.drawable.ic_outline_view_agenda_24)
                    else ->  menuItem.setIcon(R.drawable.ic_baseline_grid_view_24)
                }
            }
            true
        }
    }

    private fun openDrawer(){
        binding.drawerLayout.openDrawer(binding.navigationView)
    }


    private fun observeClearView(){
        viewModel.needClearView.observe(this){
            if(it){
                clearView()
                viewModel.cleanedView()
            }
        }
    }


    private fun observeTrashAndArchiveNotes(){
        viewModel.archiveNotes.observe(this){
            it?.let {
                if (viewModel.currentNotesInView.value== ARCHIVE) {
                    adapter.addHeaderAndSubmitList(it)
                    adapter.initNotes(viewModel.normalNotes.value!!)
                }
            }
        }
        viewModel.trashNotes.observe(this){
            it?.let {

//                it.forEach { noteWithLabels ->
//                    if (noteWithLabels.note.timeEdited+7*24*60*60*1000>System.currentTimeMillis())
//                    {
//                        runBlocking {
//                            withContext(Dispatchers.IO){
//                                noteRepository.delete(noteWithLabels.note)
//                            }
//                        }
//                    }
//                }

                if (viewModel.currentNotesInView.value== TRASH) {
                    adapter.addHeaderAndSubmitList(it)
                    adapter.initNotes(viewModel.normalNotes.value!!)
                }
            }
        }
    }


    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun showDateTimePicker(notes : List<Note>) {
        val dialogViewBinding = DateTimePickerBinding.inflate(layoutInflater)
        val alertDialog = AlertDialog.Builder(this).create()

        dialogViewBinding.date.text = SimpleDateFormat("MMM dd").format(System.currentTimeMillis()).toString()
        dialogViewBinding.time.text = SimpleDateFormat("HH:mm").format(System.currentTimeMillis()).toString()


        val currentTime = Calendar.getInstance()
        var mYear = currentTime[Calendar.YEAR] // current year

        var mMonth = currentTime[Calendar.MONTH] // current month

        var mDay = currentTime[Calendar.DAY_OF_MONTH] // current day

        var hour = currentTime[Calendar.HOUR_OF_DAY]
        var minute = currentTime[Calendar.MINUTE]

        dialogViewBinding.time.setOnClickListener {

            val timePicker = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
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

            val datePickerDialog = DatePickerDialog(this,
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

        var timeReminder: Long
        dialogViewBinding.saveAction.setOnClickListener {

            val timeReminderString = mMonth.toString() +" "+ mDay.toString() +" "+  mYear.toString()+" " +
                    hour.toString()+ ":" + minute.toString()
            val formatter = SimpleDateFormat("MM dd yyyy HH:mm")
            val date = formatter.parse(timeReminderString)
            timeReminder = date.time
            Toast.makeText(this,"current " +
                    "${SimpleDateFormat("MMM dd yyyy HH:mm").format(System.currentTimeMillis())}," +
                    " time $date", Toast.LENGTH_LONG).show()
            notes.forEach {
                viewModel.sendNotification(it, timeReminder)
            }
            alertDialog.hide()
            viewModel.clearView()

        }

        dialogViewBinding.cancelAction.setOnClickListener {
            alertDialog.hide()
            viewModel.clearView()

        }

        if(notes.size==1 && notes[0].timeReminder!=0L) {
            dialogViewBinding.deleteAction.visibility = View.VISIBLE
            dialogViewBinding.deleteAction.setOnClickListener {
                viewModel.deleteReminder(notes[0])
                alertDialog.hide()
                viewModel.clearView()
            }
        }

    }

//    private fun deleteReminder(note: Note){
//        val notificationManager = NotificationManagerCompat.from(requireContext())
//        notificationManager.cancel(note.noteId)
//        viewModel.deleteReminder(note)
//    }

//    private fun sendNotification(timeReminder: Long){
//
//        val builder = NotificationCompat.Builder(requireContext(),"notifyId")
//            .setSmallIcon(R.drawable.ic_outline_notifications_24)
//            .setContentText("eo biet gi luon ")
//            .setContentTitle("this is title")
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//
//
//        createNotificationChannel()
//
//
//        viewModel.notesSelected.value!!.forEach {
//            with(NotificationManagerCompat.from(requireContext())) {
//
////                notify(it.note.noteId, builder.build())
//
//            }
//        }
//    }

//    private fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val name: CharSequence = "this is channel"
//            val description = "this is description"
//            val importance = NotificationManager.IMPORTANCE_DEFAULT
//            val channel = NotificationChannel("notifyId", name, importance)
//            channel.description = description
//
//
//            val notificationManager =
//                requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//            notificationManager.createNotificationChannel(channel)
//        }
//    }

    @SuppressLint("RestrictedApi")
    private fun setUpMenuDrawer(){

        setStateItemTouchWhenFilterNavigationBar()
        setFunctionForLabelItemNavigationBar()
        setFunctionForMenuItemNavigationBar()

    }

    private fun setStateItemTouchWhenFilterNavigationBar(){
        viewModel.isSearching.observe(this){
            if(it){
                viewModel.itemTouchHelper.attachToRecyclerView(null)
            }else
                viewModel.itemTouchHelper.attachToRecyclerView(binding.listItem)
        }
    }

    private fun setFunctionAddLabelItemNavigationBar(){

        val menu = binding.navigationView.menu
        menu.getItem(1).subMenu.add(1,Int.MIN_VALUE, Menu.CATEGORY_CONTAINER,"Create new label").also {
            it.setIcon(R.drawable.ic_baseline_add_24)
            it.setOnMenuItemClickListener {

                viewModel.clearView()

                closeDrawer()
/**
                findNavController().navigate(
                    OverviewFragmentDirections.actionOverviewFragmentToLabelSettingFragment())
**/
                binding.view.visibility = View.VISIBLE
                val fragment = LabelSettingFragment()
                val args = Bundle()

                fragment.arguments = args

                val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
//                ft.setCustomAnimations(R.anim.zoom_in,R.anim.zoom_out)
                ft.replace(R.id.view, fragment)
                binding.drawerLayout.setBackgroundColor(Color.WHITE)
                ft.addToBackStack(null)
                ft.commit()

                true
            }

        }
    }

    private fun setFunctionForLabelItemNavigationBar(){
        val menu = binding.navigationView.menu
        viewModel.labels.observeForever {
            if(it!=null) {

                menu.getItem(1).subMenu.clear()

                setFunctionAddLabelItemNavigationBar()

                viewModel.labels.value!!.forEach { labelWithNotes ->

                    menu.getItem(1).subMenu.add(0, labelWithNotes.label.labelId, 1, labelWithNotes.label.labelName).also { item ->
                        item.setIcon(R.drawable.ic_outline_label_24)
                        item.setOnMenuItemClickListener {

                            viewModel.clearView()

                            adapter.filterLabel(labelWithNotes.label.labelName,false)

                            viewModel.setUpLabelNavigate(listOf(labelWithNotes.label))

                            closeDrawer()

                            binding.bottomAppBar.visibility = View.VISIBLE

                            viewModel.setMenuItemForNormalNotes()

                            viewModel.startSearching()

                            true
                        }
                    }

                }
                adapter.initLabels(viewModel.labels)
            }
        }
    }

    private fun setFunctionForMenuItemNavigationBar(){
        binding.navigationView.setNavigationItemSelectedListener { menutItem ->
            viewModel.clearView()

            when (menutItem.itemId) {
                R.id.notes -> {
                    binding.bottomAppBar.visibility = View.VISIBLE
                    viewModel.setMenuItemForNormalNotes()
                    adapter.filterLabel(null,false)
                    viewModel.stopSearching()
                    Toast.makeText(this, "normalNotes click", Toast.LENGTH_SHORT).show()
                    viewModel.setUpLabelNavigate(null)
                }

                R.id.reminders -> {
                    adapter.filterReminders()
                    binding.bottomAppBar.visibility = View.VISIBLE
                    binding.floating.visibility = View.VISIBLE
                    viewModel.setMenuItemForNormalNotes()
                    viewModel.setUpLabelNavigate(null)

                }

                R.id.archive -> {
                    adapter.showNotesArchive(viewModel.archiveNotes.value!!)
                    viewModel.setMenuItemForArchiveNotes()
                    binding.bottomAppBar.visibility = View.GONE
                    binding.floating.visibility = View.GONE
                    viewModel.setUpLabelNavigate(null)

                }

                R.id.trash -> {
                    adapter.showNotesTrash(viewModel.trashNotes.value!!)
                    viewModel.setMenuItemForTrashNotes()
                    binding.bottomAppBar.visibility = View.GONE
                    binding.floating.visibility = View.GONE
                    viewModel.setUpLabelNavigate(null)
                }
                else -> Toast.makeText(
                    this,
                    "i don't know wtf is this",
                    Toast.LENGTH_SHORT
                ).show()
            }

            closeDrawer()
            true
        }
    }

    private fun closeDrawer(){
        binding.drawerLayout.closeDrawer(GravityCompat.START)

    }

    private fun navigateToSearchViewListener(){

        viewModel.navigateToSearch.observe(this){
            if(it){

                /**
                findNavController().navigate(
                    OverviewFragmentDirections
                    .actionOverviewFragmentToSearchFragment())
                **/

                binding.view.visibility = View.VISIBLE
                val fragment = SearchFragment()
                val args = Bundle()

                fragment.arguments = args

                val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
//                ft.setCustomAnimations(R.anim.zoom_in,R.anim.zoom_out)
                ft.replace(R.id.view, fragment)
                binding.drawerLayout.setBackgroundColor(Color.WHITE)
                ft.addToBackStack(null)
                ft.commit()

                viewModel.doneNavigateToSearchView()
            }
        }
    }

    private fun observeNotesSelected(){
        viewModel.notesSelected.observe(this){

            attachItemTouch()

            autoSetIconForItemPin()
        }
    }

    private fun attachItemTouch(){
        if(viewModel.notesSelected.value!!.size>1){
            viewModel.itemTouchHelper.attachToRecyclerView(null)
        }else {
            viewModel.itemTouchHelper.attachToRecyclerView(binding.listItem)
        }
    }

    private fun autoSetIconForItemPin(){
        val itemPin = binding.contextualActionBar.menu.findItem(R.id.pin)
        if(viewModel.checkPinOrUnpin()){
            itemPin.setIcon(R.drawable.ic_pinned)
        } else
            itemPin.setIcon(R.drawable.ic_pin)
    }

    /*******BottomAppBar********/
    private fun setItemClickBottomAppBar(){
        binding.floating.setOnClickListener {
            viewModel.addNote()
//            binding.listItem.layoutManager!!.removeAllViews()
//            binding.listItem.layoutManager!!.onRestoreInstanceState(viewModel.recyclerViewState)
//            Toast.makeText(context,"${viewModel.recyclerViewState}",Toast.LENGTH_LONG).show()
        }

        binding.bottomAppBar.setOnMenuItemClickListener(object : Toolbar.OnMenuItemClickListener,
            androidx.appcompat.widget.Toolbar.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                when(item!!.itemId){
                    R.id.image -> chooseImage()
                    R.id.checkbox -> createList()
                }
                return true
            }
        })
    }

    private fun createList(){
        viewModel.addNote(checkboxGroup=arrayListOf(DataCheckboxes(0,"")))
    }

    private fun chooseImage(){
        resultLauncher.launch(viewModel.getIntentLibrary())
    }


    /*********ContextualActionbar*********/

    @SuppressLint("RestrictedApi")
    private fun setItemCLickContextualActionbar(){

        binding.contextualActionBar.setNavigationOnClickListener {
            clearView()
        }

        setFunctionForMenuItemsContextualActionbar()
        setVisibilityMenuItemsInActionBar()

    }

    private val ARCHIVE = 1
    private val TRASH = -1


    /********set visibility menu items in ContextualBar and Toolbar *********/
    private fun setVisibilityMenuItemsInActionBar(){

        viewModel.currentNotesInView.observe(this){
            when(it){
                ARCHIVE -> {
                    setUpContextualBarWithArchiveNotes()
                    setUpToolbarWithArchiveNotes()
                }
                TRASH -> {
                    setUpContextualBarWithTrashNotes()
                    setUpToolbarWithTrashNotes()
                }
                else -> {
                    setUpContextualBarWithNormalNotes()
                    setUpToolbarWithNormalNotes()
                }
            }

        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUpToolbarWithArchiveNotes(){
        binding.hintSearch.text = "Archive"
        binding.hintSearch.isClickable = false
        binding.toolbar.menu.forEach { menuItem ->
            when(menuItem.itemId){
                R.id.empty_trash -> menuItem.isVisible = false
                else -> menuItem.isVisible = true
            }
        }
        val appBarLayoutParams  = CoordinatorLayout.LayoutParams(
            AppBarLayout.LayoutParams.MATCH_PARENT,
            AppBarLayout.LayoutParams.WRAP_CONTENT)
        appBarLayoutParams.setMargins(0,0,0,0)
        binding.appBarLayout.layoutParams = appBarLayoutParams
        binding.appBarLayout.setBackgroundResource(R.drawable.background_tool_bar)
        val toolbar = binding.toolbar
        val params = toolbar.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = 0
    }

    @SuppressLint("SetTextI18n")
    private fun setUpToolbarWithTrashNotes(){
        binding.hintSearch.text = "Trash"
        binding.hintSearch.isClickable = false
        binding.toolbar.menu.forEach { menuItem ->
            when (menuItem.itemId) {
                R.id.empty_trash -> menuItem.isVisible = true
                else -> menuItem.isVisible = false
            }
        }
        val appBarLayoutParams  = CoordinatorLayout.LayoutParams(
            AppBarLayout.LayoutParams.MATCH_PARENT,
            AppBarLayout.LayoutParams.WRAP_CONTENT)
        appBarLayoutParams.setMargins(0,0,0,0)
        binding.appBarLayout.layoutParams = appBarLayoutParams
        binding.appBarLayout.setBackgroundResource(R.drawable.background_tool_bar)
        val toolbar = binding.toolbar
        val params = toolbar.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = 0
    }

    @SuppressLint("SetTextI18n")
    private fun setUpToolbarWithNormalNotes(){
        binding.hintSearch.text = "Search your notes"
        binding.hintSearch.isClickable = true
        binding.toolbar.menu.forEach { menuItem ->
            when (menuItem.itemId) {
                R.id.optionView -> menuItem.isVisible = true
                else -> menuItem.isVisible = false
            }
        }
        binding.appBarLayout.setBackgroundResource(R.drawable.border)

        val appBarLayoutParams  = CoordinatorLayout.LayoutParams(
            AppBarLayout.LayoutParams.MATCH_PARENT,
            AppBarLayout.LayoutParams.WRAP_CONTENT)
        appBarLayoutParams.setMargins(16,16,16,16)
        binding.appBarLayout.layoutParams = appBarLayoutParams

        val toolbar = binding.toolbar
        val params = toolbar.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags =
            (AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                    or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS or AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP_MARGINS)
    }

    private fun setUpContextualBarWithArchiveNotes(){
        binding.contextualActionBar.menu.forEach {
            when(it.itemId){
                R.id.delete_forever -> it.isVisible = false
                R.id.restore -> it.isVisible = false
                R.id.storage -> it.isVisible = false
                else -> it.isVisible = true
            }
        }
    }

    private fun setUpContextualBarWithTrashNotes(){
        binding.contextualActionBar.menu.forEach {
            when(it.itemId){
                R.id.delete_forever -> it.isVisible = true
                R.id.restore -> it.isVisible = true
                else -> it.isVisible = false
            }
        }
    }

    private fun setUpContextualBarWithNormalNotes(){
        binding.contextualActionBar.menu.forEach {
            when(it.itemId){
                R.id.delete_forever -> it.isVisible = false
                R.id.restore -> it.isVisible = false
                R.id.unarchive -> it.isVisible = false
                else -> it.isVisible = true
            }
        }
    }

    private fun setFunctionForMenuItemsContextualActionbar(){
        binding.contextualActionBar.setOnMenuItemClickListener {  item ->
            when(item.itemId){
                R.id.pin -> {
                    addPin()
                }
                R.id.label -> {
                    addLabel()
                }

                R.id.notification -> {
//                    viewModel.showDateTimePicker( viewModel.notesSelected.value!!.map { it.note },layoutInflater)
                    showDateTimePicker( viewModel.notesSelected.value!!.map { it.note })

                }

                R.id.delete -> {
                    deleteNote()
                    Snackbar.make(binding.root, "Note moved to trash", Snackbar.LENGTH_LONG)
                        .setAction("Undo") {
                            viewModel.undoDeleteNotes()
                        }
                        .show()
                }

                R.id.copy -> {
                    makeACopyNote()
                }

                R.id.storage -> {
                    storageNotes()
                }

                R.id.unarchive ->{
                    unarchive()
                }

                R.id.restore ->{
                    restore()
                }

                R.id.delete_forever ->{
                    deleteForever()
                }

            }
            true
        }
    }

    private fun unarchive(){
        viewModel.unarchive()
        viewModel.clearView()
    }

    private fun restore(){
        viewModel.restore()
        viewModel.clearView()
    }

    private fun deleteForever(){
        viewModel.deleteForever()
        viewModel.clearView()
    }

    private fun storageNotes(){
        viewModel.storageNotes()
        viewModel.clearView()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addPin(){
        viewModel.addPin()
        Timber.i("success normalNotes ${viewModel.normalNotes.value}")
        viewModel.clearView()

        if (viewModel.currentNotesInView.value==ARCHIVE){
            adapter.showNotesArchive(viewModel.archiveNotes.value!!)
        }else
            adapter.addHeaderAndSubmitList(viewModel.normalNotes.value)

        adapter.notifyDataSetChanged()
        Timber.i("done add Pin")
    }


    private fun addLabel(){
        /**
        findNavController().navigate(
            OverviewFragmentDirections
                .actionOverviewFragmentToLabelFragment(
                    viewModel.notesSelected.value!!
                        .map { it.note.noteId}.toIntArray()))
        **/


        binding.view.visibility = View.VISIBLE
        val fragment = LabelFragment()
        val args = Bundle()

        args.putIntArray("noteIdsToLabel", viewModel.notesSelected.value!!.map { it.note.noteId }.toIntArray())
        fragment.arguments = args

        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
//                ft.setCustomAnimations(R.anim.zoom_in,R.anim.zoom_out)
        ft.replace(R.id.view, fragment)
        binding.drawerLayout.setBackgroundColor(Color.WHITE)
        ft.addToBackStack(null)
        ft.commit()
        viewModel.clearView()
    }

    private fun deleteNote(){
        viewModel.deleteNotes()
        viewModel.clearView()
    }

    private fun makeACopyNote(){
        viewModel.makeACopy()
        viewModel.clearView()
    }

    private fun clearView(){
        viewModel.clearSelected()
        for(pos in 0 until binding.listItem.adapter!!.itemCount) {
            binding.listItem.findViewHolderForAdapterPosition(pos)
                ?.let {it ->
                    viewModel.callback.clearView(binding.listItem, it)
                }
        }
    }

    private fun setupOptionView(){
        viewModel.optionView.observe(this){

            if(it==OverviewViewModel.OptionView.GRIDVIEW) {

                val layoutManager = StaggeredGridLayoutManager(
                    2, OrientationHelper.VERTICAL
                ).apply {
                    gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
                    invalidateSpanAssignments()
                }

                binding.listItem.setHasFixedSize(true)
                binding.listItem.layoutManager = layoutManager

            }
            else {
                binding.listItem.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

            }

        }

    }

    override fun onPause() {
        updateOrderNotes()
        clearView()
//        var array = IntArray(2)
//        array = (binding.listItem.layoutManager as StaggeredGridLayoutManager).findFirstCompletelyVisibleItemPositions(array)

        viewModel.recyclerViewState = binding.listItem.layoutManager?.onSaveInstanceState()

        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("destroy")
    }


//        if(viewModel.recyclerViewState!=null) {
//            binding.listItem.layoutManager!!.onRestoreInstanceState(viewModel.recyclerViewState)
//            Toast.makeText(this,"restore ${viewModel.recyclerViewState}", Toast.LENGTH_SHORT).show()
//        }



    private fun updateOrderNotes(){
        viewModel.normalNotes.value!!.forEachIndexed { index, noteWithLabels ->
            runBlocking {
                withContext(Dispatchers.IO){
                    noteWithLabels.note.position = index
                    noteRepository.update(noteWithLabels.note)
                }
            }
        }

        viewModel.archiveNotes.value!!.forEach { noteWithLabels ->
            runBlocking {
                withContext(Dispatchers.IO){
                    noteRepository.update(noteWithLabels.note)
                }
            }
        }
    }

    //    private fun updateOrderNotes(){
//        val numOfNotePin = viewModel.normalNotes.value!!.filter { it.note.priority==1 }.size
//        var node = -1
//        if(numOfNotePin>0){
//            node = numOfNotePin+1
//        }
//        Timber.i("node ${node}")
//
//        for(pos in 0 until adapter!!.itemCount) {
//
//                    runBlocking {
//                        withContext(Dispatchers.IO){
//                            if (binding.listItem.findViewHolderForAdapterPosition(pos) is NotesAdapter.ViewHolder) {
//                                val note = (binding.listItem.findViewHolderForAdapterPosition(pos)
//                                        as NotesAdapter.ViewHolder).binding.noteWithLabels!!.note
//                                if(node!=-1){
//                                   if(pos<node){
//                                       note.position = pos-1
//                                   }
//                                    else{
//                                        note.position = pos-2
//                                   }
//                                } else{


//                                    note.position = pos
//                                }
//                                noteRepository.update(note)
//                                Timber.i("success update ${note}")
//
//                            }
//                        }
//                    }
//
//        }
//        Timber.i("done update order Notes")
//
//    }

}