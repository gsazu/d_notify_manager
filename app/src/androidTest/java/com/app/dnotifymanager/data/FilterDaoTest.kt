package com.app.dnotifymanager.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FilterDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var filterDao: FilterDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        filterDao = database.filterDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetFilter() = runBlocking {
        val filter = FilterEntity(keyword = "test", tune = "tune.mp3")
        filterDao.insert(filter)

        val allFilters = filterDao.getAllFilters().first()
        assertEquals(allFilters[0].keyword, "test")
    }

    @Test
    fun deleteFilter() = runBlocking {
        val filter = FilterEntity(id = 1, keyword = "test", tune = "tune.mp3")
        filterDao.insert(filter)
        filterDao.delete(filter)
        val allFilters = filterDao.getAllFilters().first()
        assertTrue(allFilters.isEmpty())
    }
    
    @Test
    fun getAllFilters_empty() = runBlocking {
        val allFilters = filterDao.getAllFilters().first()
        assertTrue(allFilters.isEmpty())
    }
}
