package com.example.keep

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.keep.database.Label
import com.example.keep.database.LabelRepository
import com.example.keep.database.NoteDatabase
import com.example.keep.database.NoteRepository
import com.example.keep.labelsetting.LabelSettingViewModel
import com.example.keep.labelsetting.LabelSettingViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.jvm.Throws

@RunWith(AndroidJUnit4::class)
class LabelsSettingViewModelTest {

    private lateinit var labelRepository: LabelRepository
    private lateinit var db : NoteDatabase
    private lateinit var viewModel: LabelSettingViewModel

    @Before
    fun createDb(){
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val application = ApplicationProvider.getApplicationContext<Application>()

        db = Room.inMemoryDatabaseBuilder(context, NoteDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        labelRepository = LabelRepository(db.labelDao)
        runBlocking {
            launch(Dispatchers.IO) {
                labelRepository.insert(Label("Inspiration"))
                labelRepository.insert(Label("aaaaaaa"))
                labelRepository.insert(Label("bbbbbb"))

            }
        }
        val factory = LabelSettingViewModelFactory(application)
        viewModel = LabelSettingViewModel(application)
    }

    @After
    @Throws(IOException::class)
    fun closeDb(){
        db.close()
    }

    @Test
    fun testRemoveLabel(){
        viewModel.removeLabel("bbbbbb")

        Assert.assertEquals("Inspiration", viewModel.allLabels)

    }
}