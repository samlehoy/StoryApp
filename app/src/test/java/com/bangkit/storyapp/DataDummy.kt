package com.bangkit.storyapp


import com.bangkit.storyapp.data.response.ListStoryItem

object DataDummy {

    fun generateDummyStoryResponse(): List<ListStoryItem> {
        val items: MutableList<ListStoryItem> = arrayListOf()
        for (i in 0..100) {
            val story = ListStoryItem(
                "id + $i",
                "photo + $i",
                "createdAt + $i",
                "name $i",
                "desc $i",
                0.0,
                0.0
            )
            items.add(story)
        }
        return items
    }
}