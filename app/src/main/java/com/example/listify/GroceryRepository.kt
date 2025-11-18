package com.example.listify

/**
 * Simple repository interface and a basic in-memory implementation.
 * Later, replace this with Room.
 */

interface GroceryRepository {
    val items: List<GroceryItem>
    fun add(item: GroceryItem)
    fun update(item: GroceryItem)
    fun delete(item: GroceryItem)
}

class InMemoryGroceryRepository : GroceryRepository {

    private val _items = mutableListOf<GroceryItem>()
    override val items: List<GroceryItem> = _items

    override fun add(item: GroceryItem) {
        _items.add(item)
    }

    override fun update(item: GroceryItem) {
        val index = _items.indexOfFirst { it.id == item.id }
        if (index >= 0) _items[index] = item
    }

    override fun delete(item: GroceryItem) {
        _items.removeIf { it.id == item.id }
    }
}
