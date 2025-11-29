package com.example.listify.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.ripple.rememberRipple
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
import com.example.listify.*
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

    val currentListId by vm.currentListId.collectAsState()

    val filteredList = vm.items.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
    }

    // Separate bought and unbought
    val unboughtItems = filteredList.filter { !it.isBought }
    val boughtItems = filteredList.filter { it.isBought }

    // Price calculations
    val totalCost = vm.getTotalCost(currentListId)
    val totalUnbought = vm.getTotalCostUnbought(currentListId)
    val currentList = vm.getCurrentList()
    val budgetStatus = vm.getBudgetStatus(currentListId)

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

                // HEADER
                Surface(
                    color = colorScheme.primary,
                    shadowElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 16.dp, top = 20.dp, bottom = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { navController.navigate("lists") }
                            ) {
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

                            IconButton(onClick = { navController.navigate("lists") }) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = "View Lists",
                                    tint = colorScheme.onPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Price summary card
                        Surface(
                            color = colorScheme.onPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            "${unboughtItems.size} items to buy",
                                            color = colorScheme.onPrimary.copy(alpha = 0.8f),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            PriceFormatter.format(totalUnbought),
                                            color = colorScheme.onPrimary,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (currentList?.budget != null && currentList.budget > 0) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                "Budget",
                                                color = colorScheme.onPrimary.copy(alpha = 0.8f),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                PriceFormatter.format(currentList.budget),
                                                color = when (budgetStatus) {
                                                    BudgetStatus.OVER_BUDGET -> colorScheme.error
                                                    BudgetStatus.NEAR_BUDGET -> Color(0xFFFFC107)
                                                    else -> colorScheme.onPrimary
                                                },
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                if (currentList?.budget != null && currentList.budget > 0) {
                                    Spacer(Modifier.height(12.dp))

                                    LinearProgressIndicator(
                                        progress = { (totalCost / currentList.budget).toFloat().coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = when (budgetStatus) {
                                            BudgetStatus.OVER_BUDGET -> colorScheme.error
                                            BudgetStatus.NEAR_BUDGET -> Color(0xFFFFC107)
                                            else -> colorScheme.onPrimary
                                        },
                                        trackColor = colorScheme.onPrimary.copy(alpha = 0.2f)
                                    )

                                    Spacer(Modifier.height(4.dp))

                                    val remaining = vm.getRemainingBudget(currentListId)
                                    Text(
                                        if (remaining >= 0)
                                            "${PriceFormatter.format(remaining)} left"
                                        else
                                            "${PriceFormatter.format(kotlin.math.abs(remaining))} over budget",
                                        color = colorScheme.onPrimary.copy(alpha = 0.7f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // SEARCH BAR
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

                // LIST CONTENT
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { translationY = if (isRefreshing) 0f else pullOffset }
                ) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // UNBOUGHT
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

                        // BOUGHT
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

                        // EMPTY STATE
                        if (filteredList.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 64.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🛒", fontSize = 64.sp)
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

                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }

            // Refresh Indicator
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
            } else false
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
                        .padding(end = 20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Delete",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.error
                        )
                    }
                }
            }
        }
    ) {
        val categoryUI = CATEGORY_UI_MAP[item.category] ?: CATEGORY_UI_MAP["Others"]!!
        val scale by animateFloatAsState(
            targetValue = if (item.isBought) 0.97f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        Surface(
            color = if (item.isBought)
                colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else
                colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            tonalElevation = if (item.isBought) 0.dp else 1.dp,
            shadowElevation = if (item.isBought) 0.dp else 3.dp,
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .clickable {
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("editItem", item)
                    navController.navigate("edit")
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Modern Checkbox with Animation
                val checkboxColor by animateColorAsState(
                    targetValue = if (item.isBought)
                        colorScheme.primary
                    else
                        colorScheme.surfaceVariant,
                    animationSpec = tween(durationMillis = 200)
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(checkboxColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(bounded = true, radius = 20.dp)
                        ) {
                            vm.update(item.copy(isBought = !item.isBought))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Animated Check Icon
                    if (item.isBought) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                // Content Column
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Item Name and Price Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            item.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (item.isBought)
                                colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            else
                                colorScheme.onSurface,
                            textDecoration = if (item.isBought)
                                TextDecoration.LineThrough
                            else null,
                            maxLines = 1,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (item.price > 0) {
                            Spacer(Modifier.width(8.dp))

                            Surface(
                                color = if (item.isBought)
                                    colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                else
                                    colorScheme.primaryContainer.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    PriceFormatter.format(item.getTotalPrice()),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isBought)
                                        colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    else
                                        colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Category and Details Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Modern Category Badge
                        Surface(
                            color = categoryUI.color.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    categoryUI.icon,
                                    contentDescription = null,
                                    tint = categoryUI.color,
                                    modifier = Modifier.size(13.dp)
                                )

                                Spacer(Modifier.width(5.dp))

                                Text(
                                    text = item.category,
                                    color = categoryUI.color,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        // Quantity Badge
                        Surface(
                            color = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "× ${item.quantity}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        // Unit Price (if multiple quantity)
                        if (item.price > 0 && item.quantity > 1) {
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "${PriceFormatter.format(item.price)}/ea",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Spacer(Modifier.width(8.dp))

                // Delete Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(colorScheme.errorContainer.copy(alpha = 0.3f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(bounded = true, radius = 20.dp)
                        ) {
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
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}