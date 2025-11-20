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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.listify.GroceryItem
import com.example.listify.GroceryViewModel
import kotlinx.coroutines.launch

@Suppress("CoroutineCreationDuringComposition")
@Composable
fun HomeScreen(
    navController: NavController,
    vm: GroceryViewModel = viewModel()
) {
    val groceryList = vm.items
    val colorScheme = MaterialTheme.colorScheme

    var searchQuery by remember { mutableStateOf("") }

    val filteredList = groceryList.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
    }

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
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary
            ) { Icon(Icons.Default.Add, contentDescription = "Add Item") }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(colorScheme.background)
        ) {
            // HEADER
            Surface(
                color = colorScheme.primary,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Text(
                    "Grocery List",
                    fontSize = 24.sp,
                    color = colorScheme.onPrimary,
                    modifier = Modifier.padding(20.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            // SEARCH BAR
            Surface(
                color = colorScheme.surface,
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
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.width(12.dp))

                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Search items…",
                                color = colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = colorScheme.surface,
                            focusedContainerColor = colorScheme.surface,
                            unfocusedIndicatorColor = colorScheme.surface,
                            focusedIndicatorColor = colorScheme.surface,
                            cursorColor = colorScheme.primary
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // LIST
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->

                    val dismissState = rememberSwipeToDismissBoxState()

                    if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                        vm.deleteWithUndo(item)
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                "Item deleted", "UNDO"
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
                            if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(colorScheme.error)
                                        .padding(end = 24.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = colorScheme.onError
                                    )
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
                                        "Item deleted", "UNDO"
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
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        color = colorScheme.surface,
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
                    checkedColor = colorScheme.primary,
                    uncheckedColor = colorScheme.primary,
                    checkmarkColor = colorScheme.onPrimary
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = item.name,
                    fontSize = 17.sp,
                    color = colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Qty: ${item.quantity}",
                    fontSize = 13.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            IconButton(onClick = onDeleteRequest) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = colorScheme.error
                )
            }
        }
    }
}
