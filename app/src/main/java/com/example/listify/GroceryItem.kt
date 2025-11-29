    package com.example.listify

    import android.os.Parcelable
    import androidx.compose.ui.graphics.Color
    import androidx.core.graphics.toColorInt
    import kotlinx.parcelize.Parcelize

    // ============================================
    // GroceryItem Data Class
    // ============================================
    @Parcelize
    data class GroceryItem(
        val id: Int,
        val name: String,
        val quantity: Int = 1,
        val category: String,
        val isBought: Boolean = false,
        val listId: Int = 0,
        val price: Double = 0.0
    ) : Parcelable {
        fun getTotalPrice(): Double = price * quantity
    }

    // ============================================
    // ShoppingList Data Class
    // ============================================
    @Parcelize
    data class ShoppingList(
        val id: Int,
        val name: String,
        val colorHex: String = "#4CAF50",
        val icon: String = "🛒",
        val createdAt: Long = System.currentTimeMillis(),
        val itemCount: Int = 0,
        val budget: Double = 0.0
    ) : Parcelable {
        fun getColor(): Color {
            return try {
                Color(colorHex.toColorInt())
            } catch (_: IllegalArgumentException) {
                Color(0xFF4CAF50)
            }
        }
    }

    // ============================================
    // Budget Status Enum
    // ============================================
    enum class BudgetStatus {
        NO_BUDGET,
        UNDER_BUDGET,
        NEAR_BUDGET,
        OVER_BUDGET
    }

    // ============================================
    // Price Formatter Utility
    // ============================================
    object PriceFormatter {
        fun format(price: Double): String {
            return if (price == 0.0) {
                "₱0.00"
            } else {
                "₱%.2f".format(price)
            }
        }

        fun formatWithoutSymbol(price: Double): String {
            return "%.2f".format(price)
        }
    }

    // ============================================
    // List Colors
    // ============================================
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

    // ============================================
    // List Icons with Categories
    // ============================================
    object ListIcons {
        data class IconCategory(
            val name: String,
            val icons: List<String>,
            val description: String = ""
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