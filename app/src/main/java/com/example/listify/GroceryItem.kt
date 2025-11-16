package com.example.listify

import java.io.Serializable

data class GroceryItem(
    val id: Int = 0,
    val name: String,
    val quantity: Int,
    val category: String = "",
    val isBought: Boolean = false
) : Serializable
