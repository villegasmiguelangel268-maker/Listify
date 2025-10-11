package com.example.listify.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.io.Serializable

// ✅ Data model
data class GroceryItem(
    val id: Int,
    val name: String,
    val quantity: Int,
    val category: String = "",
    var isBought: Boolean = false
) : Serializable

/**
 * ✅ Proper saver for SnapshotStateList<GroceryItem>
 * (function name now follows Kotlin naming convention)
 */
fun groceryItemListSaver() = listSaver<SnapshotStateList<GroceryItem>, Map<String, Any>>(
    save = { list ->
        list.map { item ->
            mapOf(
                "id" to item.id,
                "name" to item.name,
                "quantity" to item.quantity,
                "category" to item.category,
                "isBought" to item.isBought
            )
        }
    },
    restore = { saved ->
        mutableStateListOf<GroceryItem>().apply {
            saved.forEach { map ->
                add(
                    GroceryItem(
                        id = map["id"] as Int,
                        name = map["name"] as String,
                        quantity = map["quantity"] as Int,
                        category = map["category"] as String,
                        isBought = map["isBought"] as Boolean
                    )
                )
            }
        }
    }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    // ✅ Remember list across rotation
    val groceryList: SnapshotStateList<GroceryItem> =
        rememberSaveable(saver = groceryItemListSaver()) {
            mutableStateListOf()
        }

    // ✅ Handle new or edited items
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(Unit) {
        savedStateHandle?.getLiveData<GroceryItem>("item")?.observeForever { item ->
            val existingIndex = groceryList.indexOfFirst { it.id == item.id }
            if (existingIndex >= 0) {
                groceryList[existingIndex] = item
            } else {
                groceryList.add(item)
            }
            savedStateHandle.remove<GroceryItem>("item")
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("+", color = MaterialTheme.colorScheme.onPrimary, fontSize = 24.sp)
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Grocery List",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (groceryList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No items yet. Tap + to add.",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                contentPadding = padding,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(groceryList, key = { it.id }) { item ->
                    GroceryItemCard(
                        item = item,
                        onToggleBought = {
                            val index = groceryList.indexOf(item)
                            if (index >= 0) {
                                groceryList[index] =
                                    groceryList[index].copy(isBought = !item.isBought)
                            }
                        },
                        onDelete = { groceryList.remove(item) },
                        onEdit = {
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("editItem", item)
                            navController.navigate("add")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GroceryItemCard(
    item: GroceryItem,
    onToggleBought: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isBought)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleBought) {
                    Icon(
                        imageVector = if (item.isBought)
                            Icons.Filled.CheckBox
                        else
                            Icons.Filled.CheckBoxOutlineBlank,
                        contentDescription = "Mark as bought",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = item.name,
                        fontSize = 18.sp,
                        fontWeight = if (item.isBought) FontWeight.Light else FontWeight.Medium
                    )
                    Text(
                        text = "Qty: ${item.quantity} ${if (item.category.isNotEmpty()) " | ${item.category}" else ""}",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete Item",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
