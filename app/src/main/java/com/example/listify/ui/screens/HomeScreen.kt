package com.example.listify.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.listify.GroceryItem
import com.example.listify.GroceryViewModel
import com.example.listify.ui.theme.ListifyGreen
import com.example.listify.ui.theme.White

@Composable
fun HomeScreen(
    navController: NavController,
    vm: GroceryViewModel = viewModel()
) {

    val groceryList = vm.items

    var showDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<GroceryItem?>(null) }

    // 🔍 Search state
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = groceryList.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
    }

    // ---- Observe returned ADD/EDIT item ----
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    val returnedItem = savedStateHandle
        ?.getLiveData<GroceryItem>("item")
        ?.observeAsState()
        ?.value

    LaunchedEffect(returnedItem) {
        returnedItem?.let {
            vm.handleReturnedItem(it)
            savedStateHandle?.remove<GroceryItem>("item")
        }
    }

    // ---- Delete confirmation dialog ----
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Delete Item") },
            text = { Text("Are you sure you want to delete this item?") },
            confirmButton = {
                TextButton(onClick = {
                    itemToDelete?.let { vm.delete(it) }
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
                onClick = {

                    // Clear edit state before adding
                    navController.getBackStackEntry("home")
                        .savedStateHandle["editItem"] = null

                    navController.navigate("add")
                },
                containerColor = ListifyGreen,
                contentColor = White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // 🌿 Header
            Surface(
                color = ListifyGreen,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Text(
                    text = "Grocery List",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = White,
                    modifier = Modifier.padding(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 🔍 Search Bar
            Surface(
                color = Color(0xFFF6F6F6),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 2.dp,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxSize()
                ) {

                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Gray.copy(alpha = 0.7f),
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text("Search items…", color = Color.Gray.copy(alpha = 0.6f))
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            cursorColor = ListifyGreen
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 📝 Items List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->

                    GroceryItemCard(
                        item = item,
                        onToggle = {
                            val updated = item.copy(isBought = !item.isBought)
                            vm.update(updated)
                        },
                        onDeleteRequest = {
                            itemToDelete = item
                            showDialog = true
                        },
                        onEdit = {

                            // ⭐ Save item TO HOME route, NOT edit route!
                            navController.getBackStackEntry("home")
                                .savedStateHandle["editItem"] = item

                            navController.navigate("edit")
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
        color = White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Checkbox(
                checked = item.isBought,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = ListifyGreen,
                    uncheckedColor = ListifyGreen,
                    checkmarkColor = White
                ),
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = item.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {

                    if (item.category.isNotEmpty()) {
                        Text(
                            text = item.category,
                            fontSize = 13.sp,
                            color = ListifyGreen
                        )

                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Text(
                        text = "Qty: ${item.quantity}",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            IconButton(
                onClick = { onDeleteRequest() },
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
