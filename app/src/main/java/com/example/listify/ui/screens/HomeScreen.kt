package com.example.listify.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.listify.GroceryItem
import com.example.listify.GroceryViewModel
import com.example.listify.ui.theme.ListifyGreen
import com.example.listify.ui.theme.White
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navController: NavController,
    vm: GroceryViewModel = viewModel()
) {
    val groceryList = vm.items

    var searchQuery by remember { mutableStateOf("") }

    val filteredList = groceryList.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
    }

    // Observe returned item from Add/Edit
    val savedStateHandle = navController.currentBackStackEntry!!.savedStateHandle
    val returnedItem = savedStateHandle
        .getLiveData<GroceryItem>("item")
        .observeAsState()
        .value

    LaunchedEffect(returnedItem) {
        returnedItem?.let {
            vm.handleReturnedItem(it)
            savedStateHandle.remove<GroceryItem>("item")
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
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

            // Header
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
                    color = White,
                    modifier = Modifier.padding(20.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            // Search bar
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
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Gray.copy(alpha = 0.7f),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search items…", color = Color.Gray) },
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

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->

                    val dismissState = rememberSwipeToDismissBoxState()

                    // Handle delete WHEN swipe settles
                    LaunchedEffect(dismissState.currentValue) {
                        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                            vm.deleteWithUndo(item)
                            val result = snackbarHostState.showSnackbar(
                                message = "Item deleted",
                                actionLabel = "UNDO"
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                vm.undoDelete()
                            }
                        }
                    }

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {

                            val isSwipingLeft =
                                dismissState.targetValue == SwipeToDismissBoxValue.EndToStart &&
                                        dismissState.progress > 0f &&
                                        dismissState.currentValue == SwipeToDismissBoxValue.Settled

                            if (isSwipingLeft) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFFFF4444))
                                        .padding(end = 24.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(Icons.Default.Delete, null, tint = White)
                                }
                            }
                        }
                    ) {
                        GroceryItemCard(
                            item = item,
                            onToggle = {
                                vm.update(item.copy(isBought = !item.isBought))
                            },
                            onDeleteRequest = {
                                vm.deleteWithUndo(item)
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Item deleted",
                                        actionLabel = "UNDO"
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        vm.undoDelete()
                                    }
                                }
                            },
                            onEdit = {
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
                )
            )

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(item.name, fontSize = 17.sp)
                Spacer(Modifier.height(2.dp))
                Text("Qty: ${item.quantity}", fontSize = 13.sp, color = Color.Gray)
            }

            IconButton(
                onClick = onDeleteRequest,
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
