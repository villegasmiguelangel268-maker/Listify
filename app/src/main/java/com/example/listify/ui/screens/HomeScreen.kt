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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.listify.GroceryItem
import com.example.listify.ui.theme.ListifyGreen
import com.example.listify.ui.theme.White

@Composable
fun HomeScreen(navController: NavController) {

    val groceryList = remember { mutableStateListOf<GroceryItem>() }

    var showDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<GroceryItem?>(null) }

    // Listen for new item
    navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<GroceryItem>("item")
        ?.observeForever { newItem ->

            val index = groceryList.indexOfFirst { it.id == newItem.id }

            if (index == -1) groceryList.add(newItem)
            else groceryList[index] = newItem
        }

    // Delete Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Delete Item") },
            text = { Text("Are you sure you want to delete this item?") },
            confirmButton = {
                TextButton(onClick = {
                    itemToDelete?.let { groceryList.remove(it) }
                    showDialog = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add") },
                containerColor = ListifyGreen,
                contentColor = White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            // 🌿 MODERN SMALLER GREEN HEADER
            Surface(
                color = ListifyGreen,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(bottom = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Grocery List",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
            }


            Spacer(modifier = Modifier.height(12.dp))

            // LIST AREA
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding()
            ) {
                items(groceryList, key = { it.id }) { item ->
                    GroceryItemCard(
                        item = item,
                        onToggle = {
                            val updated = item.copy(isBought = !item.isBought)
                            groceryList[groceryList.indexOf(item)] = updated
                        },
                        onDeleteRequest = {
                            itemToDelete = item
                            showDialog = true
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
    onDeleteRequest: () -> Unit,
    onEdit: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = White,
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Checkbox(
                checked = item.isBought,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = ListifyGreen,
                    uncheckedColor = ListifyGreen
                )
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (item.category.isNotEmpty()) {
                    Text(
                        text = item.category,
                        fontSize = 12.sp,
                        color = ListifyGreen
                    )
                }

                Text(
                    text = "Qty: ${item.quantity}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = { onDeleteRequest() },
                modifier = Modifier.size(32.dp)  // smaller delete button
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
