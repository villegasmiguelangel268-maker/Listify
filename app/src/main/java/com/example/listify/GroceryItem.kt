package com.example.listify

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import androidx.compose.ui.graphics.Color

// Existing GroceryItem (keep as is)
@Parcelize
data class GroceryItem(
    val id: Int,
    val name: String,
    val quantity: Int,
    val category: String,
    val isBought: Boolean = false,
    val listId: Int = 0  // NEW: Associate item with a list
) : Parcelable

// NEW: Shopping List Model
@Parcelize
data class ShoppingList(
    val id: Int,
    val name: String,
    val colorHex: String = "#4CAF50",  // Default green
    val icon: String = "🛒",  // Emoji icon
    val createdAt: Long = System.currentTimeMillis(),
    val itemCount: Int = 0
) : Parcelable {
    fun getColor(): Color {
        return try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            Color(0xFF4CAF50)  // Fallback green
        }
    }
}

// Predefined list colors
object ListColors {
    val colors = listOf(
        "#4CAF50" to "Green",
        "#2196F3" to "Blue",
        "#FF9800" to "Orange",
        "#E91E63" to "Pink",
        "#9C27B0" to "Purple",
        "#F44336" to "Red",
        "#00BCD4" to "Cyan",
        "#FFC107" to "Amber",
        "#795548" to "Brown",
        "#607D8B" to "Blue Grey"
    )
}

// Predefined list icons
// Predefined list icons with categories
object ListIcons {
    data class IconCategory(
        val name: String,
        val icons: List<String>,
        val description: String
    )

    val categories = listOf(
        IconCategory(
            "General",
            listOf("🛒", "🛍️", "📋", "✅"),
            "For any type of shopping"
        ),
        IconCategory(
            "Fresh Foods",
            listOf("🥬", "🥕", "🍎", "🍇", "🍅", "🥦"),
            "Produce & fresh items"
        ),
        IconCategory(
            "Proteins",
            listOf("🥩", "🍗", "🐟", "🥚"),
            "Meat, seafood & eggs"
        ),
        IconCategory(
            "Dairy",
            listOf("🥛", "🧀", "🧈"),
            "Milk, cheese & butter"
        ),
        IconCategory(
            "Bakery",
            listOf("🥖", "🍞", "🥐", "🧁"),
            "Bread & baked goods"
        ),
        IconCategory(
            "Special",
            listOf("🎉", "💰", "⭐", "🔥"),
            "Party, budget, priority lists"
        )
    )
}