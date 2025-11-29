package com.example.listify

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// ============================================
// Category UI Data Class
// ============================================
data class CategoryUI(
    val icon: ImageVector,
    val color: Color
)

// ============================================
// Category List
// ============================================
val CATEGORY_LIST = listOf(
    "Fruits",
    "Vegetables",
    "Meat",
    "Seafood",
    "Snacks",
    "Drinks",
    "Frozen",
    "Household",
    "Others"
)

// ============================================
// Category UI Mapping
// ============================================
val CATEGORY_UI_MAP = mapOf(
    "Fruits" to CategoryUI(
        icon = Icons.Default.LocalFlorist,
        color = Color(0xFFE57373)
    ),
    "Vegetables" to CategoryUI(
        icon = Icons.Default.Eco,
        color = Color(0xFF81C784)
    ),
    "Meat" to CategoryUI(
        icon = Icons.Default.Restaurant,
        color = Color(0xFFD32F2F)
    ),
    "Seafood" to CategoryUI(
        icon = Icons.Default.WaterDrop,
        color = Color(0xFF0288D1)
    ),
    "Snacks" to CategoryUI(
        icon = Icons.Default.Fastfood,
        color = Color(0xFFFFA726)
    ),
    "Drinks" to CategoryUI(
        icon = Icons.Default.LocalDrink,
        color = Color(0xFF42A5F5)
    ),
    "Frozen" to CategoryUI(
        icon = Icons.Default.AcUnit,
        color = Color(0xFF00BCD4)
    ),
    "Household" to CategoryUI(
        icon = Icons.Default.Home,
        color = Color(0xFF8D6E63)
    ),
    "Others" to CategoryUI(
        icon = Icons.Default.Category,
        color = Color(0xFF9E9E9E)
    )
)