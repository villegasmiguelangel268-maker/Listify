package com.example.listify

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryUI(
    val label: String,
    val color: Color,
    val icon: ImageVector
)

val CATEGORY_UI_MAP = mapOf(
    "Fruits" to CategoryUI("Fruits", Color(0xFFE57373), Icons.Default.LocalGroceryStore),
    "Vegetables" to CategoryUI("Vegetables", Color(0xFF81C784), Icons.Default.Eco),
    "Meat" to CategoryUI("Meat", Color(0xFFD32F2F), Icons.Default.Restaurant),
    "Seafood" to CategoryUI("Seafood", Color(0xFF0288D1), Icons.Default.Water),
    "Snacks" to CategoryUI("Snacks", Color(0xFFFFA726), Icons.Default.Fastfood),
    "Drinks" to CategoryUI("Drinks", Color(0xFF42A5F5), Icons.Default.LocalDrink),
    "Frozen" to CategoryUI("Frozen", Color(0xFF00BCD4), Icons.Default.AcUnit),
    "Household" to CategoryUI("Household", Color(0xFF8D6E63), Icons.Default.Home),
    "Others" to CategoryUI("Others", Color(0xFF9E9E9E), Icons.Default.Category)
)
