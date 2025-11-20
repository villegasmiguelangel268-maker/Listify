package com.example.listify

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class GroceryViewModel(
    private val repo: GroceryRepository = InMemoryGroceryRepository()
) : ViewModel() {

    private val _items = mutableStateListOf<GroceryItem>().apply {
        addAll(repo.items)
    }
    val items: List<GroceryItem> get() = _items

    // For undo delete
    private var lastDeletedItem: GroceryItem? = null

    fun add(item: GroceryItem) {
        repo.add(item)
        _items.add(item)
    }

    fun update(item: GroceryItem) {
        repo.update(item)
        val index = _items.indexOfFirst { it.id == item.id }
        if (index >= 0) _items[index] = item
    }

    fun delete(item: GroceryItem) {
        repo.delete(item)
        _items.removeIf { it.id == item.id }
    }

    fun deleteWithUndo(item: GroceryItem) {
        lastDeletedItem = item
        delete(item)
    }

    fun undoDelete() {
        lastDeletedItem?.let {
            add(it)
            lastDeletedItem = null
        }
    }

    fun handleReturnedItem(item: GroceryItem) {
        val index = _items.indexOfFirst { it.id == item.id }
        if (index == -1) add(item) else update(item)
    }
}
