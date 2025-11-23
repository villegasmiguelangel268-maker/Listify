package com.example.listify.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.listify.GroceryItem
import com.example.listify.GroceryViewModel
import com.example.listify.CATEGORY_UI_MAP
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    vm: GroceryViewModel = viewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }
    var pullOffset by remember { mutableStateOf(0f) }
    val listState = rememberLazyListState()

    val filteredList = vm.items.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
    }

    // Separate bought and unbought items
    val unboughtItems = filteredList.filter { !it.isBought }
    val boughtItems = filteredList.filter { it.isBought }

    val snackbarHostState = remember { SnackbarHostState() }

    val refreshThreshold = 150f
    val pullProgress = (pullOffset / refreshThreshold).coerceIn(0f, 1f)
    val shouldRefresh = pullOffset >= refreshThreshold

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = colorScheme.inverseSurface,
                        contentColor = colorScheme.inverseOnSurface,
                        actionColor = colorScheme.primary,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            )
        },
        containerColor = colorScheme.surfaceContainerLowest,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.getBackStackEntry("home")
                        .savedStateHandle["editItem"] = null
                    navController.navigate("add")
                },
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Item",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isRefreshing) {
                        if (!isRefreshing) {
                            detectVerticalDragGestures(
                                onDragEnd = {
                                    if (shouldRefresh && !isRefreshing) {
                                        isRefreshing = true
                                        scope.launch {
                                            vm.refresh()
                                            delay(1000)
                                            isRefreshing = false
                                            pullOffset = 0f
                                        }
                                    } else {
                                        pullOffset = 0f
                                    }
                                },
                                onVerticalDrag = { _, dragAmount ->
                                    val firstVisibleIndex = listState.firstVisibleItemIndex
                                    val firstVisibleOffset = listState.firstVisibleItemScrollOffset

                                    if (firstVisibleIndex == 0 && firstVisibleOffset == 0 && dragAmount > 0) {
                                        pullOffset = (pullOffset + dragAmount * 0.5f).coerceIn(0f, 250f)
                                    }
                                }
                            )
                        }
                    }
            ) {

                // Modern Header with List Selector
                Surface(
                    color = colorScheme.primary,
                    shadowElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 16.dp, top = 20.dp, bottom = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    navController.navigate("lists")
                                }
                            ) {
                                val currentList = vm.getCurrentList()
                                Text(
                                    currentList?.icon ?: "🛒",
                                    fontSize = 28.sp
                                )

                                Spacer(Modifier.width(12.dp))

                                Column {
                                    Text(
                                        currentList?.name ?: "Grocery List",
                                        color = colorScheme.onPrimary,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        "Tap to switch lists",
                                        color = colorScheme.onPrimary.copy(alpha = 0.7f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            Text(
                                "${unboughtItems.size} items to buy",
                                color = colorScheme.onPrimary.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        IconButton(
                            onClick = { navController.navigate("lists") }
                        ) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "View Lists",
                                tint = colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                // Search bar with modern design
                Surface(
                    color = colorScheme.surface,
                    shape = RoundedCornerShape(28.dp),
                    tonalElevation = 1.dp,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )

                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    "Search your groceries...",
                                    color = colorScheme.onSurfaceVariant
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                cursorColor = colorScheme.primary,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            )
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // List with sections
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationY = if (isRefreshing) 0f else pullOffset
                        }
                ) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Active Items Section
                        if (unboughtItems.isNotEmpty()) {
                            item {
                                Text(
                                    "TO BUY",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.primary,
                                    letterSpacing = 1.2.sp,
                                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                                )
                            }

                            items(unboughtItems, key = { it.id }) { item ->
                                ModernGroceryCard(
                                    item = item,
                                    colorScheme = colorScheme,
                                    scope = scope,
                                    snackbarHostState = snackbarHostState,
                                    vm = vm,
                                    navController = navController
                                )
                            }
                        }

                        // Completed Items Section
                        if (boughtItems.isNotEmpty()) {
                            item {
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "COMPLETED",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.2.sp,
                                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                                )
                            }

                            items(boughtItems, key = { it.id }) { item ->
                                ModernGroceryCard(
                                    item = item,
                                    colorScheme = colorScheme,
                                    scope = scope,
                                    snackbarHostState = snackbarHostState,
                                    vm = vm,
                                    navController = navController
                                )
                            }
                        }

                        // Empty state
                        if (filteredList.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 64.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "🛒",
                                        fontSize = 64.sp
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        "No items yet",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Tap the + button to add your first item",
                                        fontSize = 14.sp,
                                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }

                        // Bottom padding
                        item {
                            Spacer(Modifier.height(80.dp))
                        }
                    }
                }
            }

            // Pull to refresh indicator
            if (pullOffset > 0f || isRefreshing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .align(Alignment.TopCenter),
                    contentAlignment = Alignment.Center
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                    } else {
                        CircularProgressIndicator(
                            progress = { pullProgress },
                            modifier = Modifier
                                .size(32.dp)
                                .rotate(pullProgress * 360f),
                            color = colorScheme.primary,
                            strokeWidth = 3.dp,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernGroceryCard(
    item: GroceryItem,
    colorScheme: ColorScheme,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState,
    vm: GroceryViewModel,
    navController: NavController
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                vm.deleteWithUndo(item)

                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "Item deleted",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        vm.undoDelete()
                    }
                }
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colorScheme.errorContainer)
                        .padding(end = 24.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) {
        val categoryUI = CATEGORY_UI_MAP[item.category] ?: CATEGORY_UI_MAP["Others"]!!
        val scale by animateFloatAsState(
            targetValue = if (item.isBought) 0.98f else 1f,
            animationSpec = spring(stiffness = Spring.StiffnessLow)
        )

        Surface(
            color = if (item.isBought)
                colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            tonalElevation = if (item.isBought) 0.dp else 2.dp,
            shadowElevation = if (item.isBought) 0.dp else 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .clickable {
                    navController.getBackStackEntry("home")
                        .savedStateHandle["editItem"] = item
                    navController.navigate("edit")
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Custom Checkbox
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            if (item.isBought)
                                colorScheme.primary
                            else
                                colorScheme.surfaceVariant
                        )
                        .clickable {
                            vm.update(item.copy(isBought = !item.isBought))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (item.isBought) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        item.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (item.isBought)
                            colorScheme.onSurfaceVariant
                        else
                            colorScheme.onSurface,
                        textDecoration = if (item.isBought)
                            TextDecoration.LineThrough
                        else
                            null
                    )

                    Spacer(Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = categoryUI.color.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    categoryUI.icon,
                                    contentDescription = null,
                                    tint = categoryUI.color,
                                    modifier = Modifier.size(14.dp)
                                )

                                Spacer(Modifier.width(4.dp))

                                Text(
                                    text = item.category,
                                    color = categoryUI.color,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Text(
                            text = "Qty: ${item.quantity}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                IconButton(
                    onClick = {
                        vm.deleteWithUndo(item)
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Item deleted",
                                actionLabel = "Undo",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                vm.undoDelete()
                            }
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}