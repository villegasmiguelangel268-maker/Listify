package com.example.listify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

data class GroceryItem(
    val id: Int,
    val name: String,
    val quantity: Int,
    val category: String = "",
    var isBought: Boolean = false
)

@Composable
fun HomeScreen(navController: NavController) {
    // Using mutableStateListOf for live state updates
    val groceryList = remember {
        mutableStateListOf(
            GroceryItem(1, "Apples", 5),
            GroceryItem(2, "Bread", 2),
            GroceryItem(3, "Milk", 1),
            GroceryItem(4, "Eggs", 12)
        )
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
                colors = TopAppBarDefaults.smallTopAppBarColors(
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
                        onDelete = {
                            groceryList.remove(item)
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
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                            Icons.Default.CheckBox
                        else
                            Icons.Default.CheckBoxOutlineBlank,
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
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Item",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
