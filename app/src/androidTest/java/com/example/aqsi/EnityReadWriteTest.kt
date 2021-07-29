package com.example.aqsi

import androidx.room.Room
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.aqsi.db.AppDatabase
import com.example.aqsi.db.RouteSheetDao
import com.example.aqsi.db.RouteSheetEntity
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class EnityReadWriteTest {
    private lateinit var todoDao: RouteSheetDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java).build()
        todoDao = db.routeSheetDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeUserAndReadInList() {
        val todo: RouteSheetEntity = RouteSheetEntity("id", "123", "20.07.2021", 1)
        todoDao.insert(todo)
        val todoItem = todoDao.getById(todo.id)
        assertThat(todoItem, equalTo(todo))
    }
}