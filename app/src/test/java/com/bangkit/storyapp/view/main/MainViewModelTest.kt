package com.bangkit.storyapp.view.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.bangkit.storyapp.DataDummy
import com.bangkit.storyapp.MainDispatcherRule
import com.bangkit.storyapp.data.repository.AppRepository
import com.bangkit.storyapp.data.response.ListStoryItem
import com.bangkit.storyapp.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner


@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var repository: AppRepository

    @Test
    fun `when Get Stories Should Not Null and Return Correct Data`() = runTest {
        // Arrange: Prepare dummy data and mock repository response
        val dummyStories = DataDummy.generateDummyStoryResponse()
        val pagingData = StoryPagingSource.snapshot(dummyStories)
        val expectedData = MutableLiveData<PagingData<ListStoryItem>>()
        expectedData.value = pagingData

        Mockito.`when`(repository.getStories()).thenReturn(expectedData)

        val mainViewModel = MainViewModel(repository)

        // Act: Fetch stories from MainViewModel
        val actualData: PagingData<ListStoryItem> = mainViewModel.story.getOrAwaitValue()

        // Assert: Validate the fetched data
        assertNotNull(actualData)
        val differ = AsyncPagingDataDiffer(
            diffCallback = ListStoryItemDiffCallback(),
            updateCallback = NoopListUpdateCallback(),
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualData)

        assertEquals(dummyStories.size, differ.snapshot().size)
        assertEquals(dummyStories[0], differ.snapshot()[0])
    }

    @Test
    fun `when No Stories Should Return Empty Data`() = runTest {
        // Arrange: Prepare empty data and mock repository response
        val emptyStories = emptyList<ListStoryItem>()
        val pagingData = StoryPagingSource.snapshot(emptyStories)
        val expectedData = MutableLiveData<PagingData<ListStoryItem>>()
        expectedData.value = pagingData

        Mockito.`when`(repository.getStories()).thenReturn(expectedData)

        val mainViewModel = MainViewModel(repository)

        // Act: Fetch stories from MainViewModel
        val actualData: PagingData<ListStoryItem> = mainViewModel.story.getOrAwaitValue()

        // Assert: Validate the fetched data
        val differ = AsyncPagingDataDiffer(
            diffCallback = ListStoryItemDiffCallback(),
            updateCallback = NoopListUpdateCallback(),
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualData)

        assertEquals(0, differ.snapshot().size)
    }
}

// Helper class for testing PagingData
class StoryPagingSource : PagingSource<Int, ListStoryItem>() {
    companion object {
        fun snapshot(items: List<ListStoryItem>): PagingData<ListStoryItem> {
            return PagingData.from(items)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        return LoadResult.Page(emptyList(), prevKey = null, nextKey = null)
    }
}

// Callback for DiffUtil
class ListStoryItemDiffCallback : DiffUtil.ItemCallback<ListStoryItem>() {
    override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
        return oldItem == newItem
    }
}

// Noop update callback for testing
class NoopListUpdateCallback : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}