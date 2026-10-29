package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.ui.EventViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BatchSelectionTest {

    private lateinit var viewModel: EventViewModel

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        viewModel = EventViewModel(app)
    }

    @Test
    fun testInitialSelectionState() {
        assertFalse("Selection mode should be inactive by default", viewModel.isSelectionMode.value)
        assertTrue("Selected IDs should be empty by default", viewModel.selectedItemIds.value.isEmpty())
    }

    @Test
    fun testEnterSelectionMode() {
        viewModel.enterSelectionMode(42L)
        assertTrue("Selection mode should be active", viewModel.isSelectionMode.value)
        assertTrue("Initial ID should be selected", viewModel.selectedItemIds.value.contains(42L))
        assertEquals(1, viewModel.selectedItemIds.value.size)
    }

    @Test
    fun testToggleSelection() {
        viewModel.enterSelectionMode()
        assertTrue(viewModel.isSelectionMode.value)
        assertTrue(viewModel.selectedItemIds.value.isEmpty())

        viewModel.toggleSelect(101L)
        assertTrue(viewModel.selectedItemIds.value.contains(101L))

        viewModel.toggleSelect(102L)
        assertTrue(viewModel.selectedItemIds.value.contains(101L))
        assertTrue(viewModel.selectedItemIds.value.contains(102L))
        assertEquals(2, viewModel.selectedItemIds.value.size)

        viewModel.toggleSelect(101L)
        assertFalse(viewModel.selectedItemIds.value.contains(101L))
        assertTrue(viewModel.selectedItemIds.value.contains(102L))
        assertEquals(1, viewModel.selectedItemIds.value.size)
    }

    @Test
    fun testSelectAllAndDeselectAll() {
        viewModel.enterSelectionMode()
        val allIds = listOf(1L, 2L, 3L, 4L)
        viewModel.selectAll(allIds)

        assertEquals(4, viewModel.selectedItemIds.value.size)
        assertTrue(viewModel.selectedItemIds.value.containsAll(allIds))

        viewModel.deselectAll()
        assertTrue(viewModel.selectedItemIds.value.isEmpty())
    }

    @Test
    fun testExitSelectionMode() {
        viewModel.enterSelectionMode(99L)
        assertTrue(viewModel.isSelectionMode.value)
        assertEquals(1, viewModel.selectedItemIds.value.size)

        viewModel.exitSelectionMode()
        assertFalse(viewModel.isSelectionMode.value)
        assertTrue(viewModel.selectedItemIds.value.isEmpty())
    }
}
