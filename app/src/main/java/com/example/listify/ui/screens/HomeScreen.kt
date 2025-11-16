package com.example.listify.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp   // ✅ FIXED — REQUIRED FOR fontSize
import androidx.navigation.NavController
import com.example.listify.GroceryItem

@Composable
fun HomeScreen(navController: NavController) {

    val groceryList = remember { mutableStateListOf<GroceryItem>() }

    // Listen for new item
    navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<GroceryItem>("item")
        ?.observeForever { newItem ->

            val index = groceryList.indexOfFirst { it.id == newItem.id }

            if (index == -1) {
                groceryList.add(newItem)
            } else {
                groceryList[index] = newItem
            }
        }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Text(
                text = "Grocery List",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(groceryList) { item ->
                    GroceryItemCard(
                        item = item,
                        onToggle = {
                            val updated = item.copy(isBought = !item.isBought)
                            groceryList[groceryList.indexOf(item)] = updated
                        },
                        onDelete = {
                            groceryList.remove(item)
                        },
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
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Checkbox(
                checked = item.isBought,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(text = item.name, fontSize = 18.sp)
                Text(text = "Qty: ${item.quantity}", fontSize = 14.sp)
                Text(
                    text = item.category,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = { onDelete() }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}
