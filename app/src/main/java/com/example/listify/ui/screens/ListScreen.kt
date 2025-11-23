package com.example.listify.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.listify.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(
    navController: NavController,
    vm: GroceryViewModel = viewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    val currentListId by vm.currentListId.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<ShoppingList?>(null) }
    var showDeleteDialog by remember { mutableStateOf<ShoppingList?>(null) }

    Scaffold(
        containerColor = colorScheme.surfaceContainerLowest,
        topBar = {
            Surface(
                color = colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colorScheme.onSurface
                        )
                    }

                    Text(
                        "My Lists",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create List")
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "TAP TO SWITCH",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(vm.lists) { list ->
                ListCard(
                    list = list,
                    isActive = list.id == currentListId,
                    itemCount = vm.getListItemCount(list.id),
                    onClick = {
                        vm.switchList(list.id)
                        navController.popBackStack()
                    },
                    onEdit = { showEditDialog = list },
                    onDelete = { showDeleteDialog = list },
                    colorScheme = colorScheme
                )
            }

            item {
                Spacer(Modifier.height(80.dp))
            }
        }
    }

    // Create List Dialog
    if (showCreateDialog) {
        ListDialog(
            title = "Create New List",
            initialName = "",
            initialIcon = ListIcons.categories.first().icons.first(),
            initialColor = ListColors.colors.first().first,
            onDismiss = { showCreateDialog = false },
            onSave = { name, icon, color ->
                val newList = ShoppingList(
                    id = (vm.lists.maxOfOrNull { it.id } ?: 0) + 1,
                    name = name,
                    icon = icon,
                    colorHex = color
                )
                vm.addList(newList)
                showCreateDialog = false
            }
        )
    }

    // Edit List Dialog
    showEditDialog?.let { list ->
        ListDialog(
            title = "Edit List",
            initialName = list.name,
            initialIcon = list.icon,
            initialColor = list.colorHex,
            onDismiss = { showEditDialog = null },
            onSave = { name, icon, color ->
                val updatedList = list.copy(
                    name = name,
                    icon = icon,
                    colorHex = color
                )
                vm.updateList(updatedList)
                showEditDialog = null
            }
        )
    }

    // Delete Confirmation Dialog
    showDeleteDialog?.let { list ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = colorScheme.error
                )
            },
            title = { Text("Delete List?") },
            text = {
                Text("Are you sure you want to delete \"${list.name}\"? All items in this list will be permanently removed.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.deleteList(list.id)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ListCard(
    list: ShoppingList,
    isActive: Boolean,
    itemCount: Int,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    colorScheme: ColorScheme
) {
    Surface(
        color = if (isActive) list.getColor().copy(alpha = 0.15f) else colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = if (isActive) 4.dp else 2.dp,
        shadowElevation = if (isActive) 6.dp else 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(
                if (isActive) {
                    Modifier.border(
                        width = 2.dp,
                        color = list.getColor(),
                        shape = RoundedCornerShape(20.dp)
                    )
                } else Modifier
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with colored background
            Surface(
                color = list.getColor().copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(64.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        list.icon,
                        fontSize = 32.sp
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            // List info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        list.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface
                    )

                    if (isActive) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = list.getColor(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "ACTIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    "$itemCount items to buy",
                    fontSize = 14.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }

            // Action buttons
            Row {
                // Edit button
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = colorScheme.primary
                    )
                }

                // Delete button (only show if not active)
                if (!isActive) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = colorScheme.error.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ListDialog(
    title: String,
    initialName: String,
    initialIcon: String,
    initialColor: String,
    onDismiss: () -> Unit,
    onSave: (name: String, icon: String, color: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedIcon by remember { mutableStateOf(initialIcon) }
    var selectedColor by remember { mutableStateOf(initialColor) }
    val colorScheme = MaterialTheme.colorScheme

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                item {
                    Text(
                        title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface
                    )

                    Spacer(Modifier.height(20.dp))

                    // Name input
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("List Name") },
                        placeholder = { Text("e.g., Weekly Shopping") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(Modifier.height(20.dp))

                    // Icon selector by category
                    Text(
                        "Choose Icon",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface
                    )

                    Spacer(Modifier.height(12.dp))
                }

                // Show categories
                items(ListIcons.categories) { category ->
                    Column(modifier = Modifier.padding(bottom = 16.dp)) {
                        Text(
                            category.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(category.icons) { icon ->
                                Surface(
                                    color = if (icon == selectedIcon)
                                        colorScheme.primaryContainer
                                    else
                                        colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clickable { selectedIcon = icon }
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(icon, fontSize = 24.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(8.dp))

                    // Color selector
                    Text(
                        "Choose Color",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface
                    )

                    Spacer(Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ListColors.colors) { (hex, _) ->
                            val color = try {
                                Color(android.graphics.Color.parseColor(hex))
                            } catch (e: Exception) {
                                colorScheme.primary
                            }

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .then(
                                        if (hex == selectedColor) {
                                            Modifier.border(
                                                width = 3.dp,
                                                color = colorScheme.onSurface,
                                                shape = CircleShape
                                            )
                                        } else Modifier
                                    )
                                    .clickable { selectedColor = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (hex == selectedColor) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }

                        Spacer(Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    onSave(name, selectedIcon, selectedColor)
                                }
                            },
                            enabled = name.isNotBlank(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}