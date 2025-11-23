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

    // ========== LISTS MANAGEMENT ==========
    private val _lists = mutableStateListOf<ShoppingList>()
    val lists: List<ShoppingList> = _lists

    private val _currentListId = MutableStateFlow(0)
    val currentListId: StateFlow<Int> = _currentListId

    // ========== ITEMS MANAGEMENT ==========
    private val _allItems = mutableStateListOf<GroceryItem>()

    // Only show items for the current list
    val items: List<GroceryItem>
        get() = _allItems.filter { it.listId == _currentListId.value }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> get() = _isRefreshing

    private var lastDeletedItem: GroceryItem? = null

    init {
        // Load items from repository
        _allItems.addAll(repo.items)

        // Create default list if none exists
        if (_lists.isEmpty()) {
            val defaultList = ShoppingList(
                id = 0,
                name = "My Grocery List",
                colorHex = "#4CAF50",
                icon = "🛒"
            )
            _lists.add(defaultList)
            updateListItemCount(defaultList.id)
        }
    }

    // ========== LIST OPERATIONS ==========

    fun addList(list: ShoppingList) {
        _lists.add(list)
        updateListItemCount(list.id)
    }

    fun updateList(list: ShoppingList) {
        val index = _lists.indexOfFirst { it.id == list.id }
        if (index != -1) {
            _lists[index] = list
        }
    }

    fun deleteList(listId: Int) {
        // Don't delete if it's the only list
        if (_lists.size <= 1) return

        // Remove the list
        _lists.removeAll { it.id == listId }

        // Remove all items in that list
        _allItems.removeAll { it.listId == listId }

        // Also remove from repository
        val itemsToDelete = repo.items.filter { it.listId == listId }
        itemsToDelete.forEach { repo.delete(it) }

        // Switch to first available list if deleting current list
        if (_currentListId.value == listId) {
            _currentListId.value = _lists.firstOrNull()?.id ?: 0
        }
    }

    fun switchList(listId: Int) {
        _currentListId.value = listId
    }

    fun getCurrentList(): ShoppingList? {
        return _lists.firstOrNull { it.id == _currentListId.value }
    }

    fun getListItemCount(listId: Int): Int {
        return _allItems.count { it.listId == listId && !it.isBought }
    }

    private fun updateListItemCount(listId: Int = _currentListId.value) {
        val list = _lists.firstOrNull { it.id == listId }
        if (list != null) {
            val count = getListItemCount(listId)
            val updatedList = list.copy(itemCount = count)
            updateList(updatedList)
        }
    }

    // ========== ITEM OPERATIONS ==========

    fun add(item: GroceryItem) {
        // Ensure item has current list ID
        val itemWithList = item.copy(listId = _currentListId.value)
        repo.add(itemWithList)
        _allItems.add(itemWithList)
        updateListItemCount()
    }

    fun update(item: GroceryItem) {
        repo.update(item)
        val index = _allItems.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            _allItems[index] = item
            updateListItemCount(item.listId)
        }
    }

    fun delete(item: GroceryItem) {
        repo.delete(item)
        _allItems.removeAll { it.id == item.id }
        updateListItemCount(item.listId)
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
        val index = _allItems.indexOfFirst { it.id == item.id }
        if (index == -1) add(item) else update(item)
    }

    fun clearCompleted() {
        // Get items to delete
        val completedItems = _allItems.filter { it.listId == _currentListId.value && it.isBought }

        // Remove from repository
        completedItems.forEach { repo.delete(it) }

        // Remove from local list
        _allItems.removeAll { it.listId == _currentListId.value && it.isBought }

        updateListItemCount()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true

            delay(800)

            // Reload items from repository
            _allItems.clear()
            _allItems.addAll(repo.items)

            _isRefreshing.value = false
        }
    }
}