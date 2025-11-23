package com.example.listify

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GroceryViewModel(
    private val repo: GroceryRepository = InMemoryGroceryRepository()
) : ViewModel() {

    private val _items = mutableStateListOf<GroceryItem>().apply {
        addAll(repo.items)
    }
    val items: List<GroceryItem> get() = _items

    // ⭐ REQUIRED FOR SwipeRefresh
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> get() = _isRefreshing

    // Store last deleted item for UNDO action
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

    /** ⭐ Pull-to-refresh handler */
    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true

            delay(800) // simulate loading from database or network

            _items.clear()
            _items.addAll(repo.items)

            _isRefreshing.value = false
        }
    }
}
