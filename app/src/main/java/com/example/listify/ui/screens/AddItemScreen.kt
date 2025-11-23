package com.example.listify.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.listify.CATEGORY_LIST
import com.example.listify.CATEGORY_UI_MAP
import com.example.listify.GroceryItem
import com.example.listify.GroceryViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MenuAnchorType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    navController: NavController,
    vm: GroceryViewModel = viewModel()
) {
    var name by rememberSaveable { mutableStateOf("") }
    var quantity by rememberSaveable { mutableStateOf("1") }
    var selectedCategory by rememberSaveable { mutableStateOf("") }
    var customCategory by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()

    fun startRepeating(action: () -> Unit, stopFlag: MutableState<Boolean>) {
        scope.launch {
            delay(300)
            while (stopFlag.value) {
                action()
                delay(70)
            }
        }
    }

    Scaffold(
        containerColor = colors.surfaceContainerLowest,
        topBar = {
            Surface(
                color = colors.surface,
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.onSurface
                        )
                    }

                    Text(
                        "Add New Item",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    ) { padding ->

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Item Name Section
            Column {
                Text(
                    "Item Name",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("e.g., Milk, Eggs, Bread...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.outline.copy(alpha = 0.5f)
                    )
                )
            }

            // Quantity Section
            Column {
                Text(
                    "Quantity",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val decHold = remember { mutableStateOf(false) }
                    val incHold = remember { mutableStateOf(false) }

                    // Decrease button
                    Surface(
                        color = colors.primaryContainer,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(56.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        decHold.value = true
                                        startRepeating({
                                            val q = quantity.toIntOrNull() ?: 1
                                            if (q > 1) quantity = (q - 1).toString()
                                        }, decHold)
                                        tryAwaitRelease()
                                        decHold.value = false
                                    },
                                    onTap = {
                                        val q = quantity.toIntOrNull() ?: 1
                                        if (q > 1) quantity = (q - 1).toString()
                                    }
                                )
                            }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Decrease",
                                tint = colors.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Quantity Display
                    Surface(
                        color = colors.surface,
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .width(100.dp)
                            .height(56.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                quantity,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                        }
                    }

                    // Increase button
                    Surface(
                        color = colors.primary,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(56.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        incHold.value = true
                                        startRepeating({
                                            val q = quantity.toIntOrNull() ?: 1
                                            quantity = (q + 1).toString()
                                        }, incHold)
                                        tryAwaitRelease()
                                        incHold.value = false
                                    },
                                    onTap = {
                                        val q = quantity.toIntOrNull() ?: 1
                                        quantity = (q + 1).toString()
                                    }
                                )
                            }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Increase",
                                tint = colors.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Category Section
            Column {
                Text(
                    "Category",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = if (selectedCategory == "Others") customCategory else selectedCategory,
                        onValueChange = { if (selectedCategory == "Others") customCategory = it },
                        readOnly = selectedCategory != "Others",
                        placeholder = { Text("Select a category") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                        },
                        leadingIcon = {
                            if (selectedCategory.isNotEmpty() && selectedCategory != "Others") {
                                val categoryUI = CATEGORY_UI_MAP[selectedCategory]
                                categoryUI?.let {
                                    Icon(
                                        it.icon,
                                        contentDescription = null,
                                        tint = it.color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .menuAnchor(
                                type = MenuAnchorType.PrimaryNotEditable,
                                enabled = true
                            )
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outline.copy(alpha = 0.5f)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(colors.surface)
                    ) {
                        CATEGORY_LIST.forEach { cat ->
                            val categoryUI = CATEGORY_UI_MAP[cat]
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        categoryUI?.let {
                                            Icon(
                                                it.icon,
                                                contentDescription = null,
                                                tint = it.color,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(Modifier.width(12.dp))
                                        }
                                        Text(cat)
                                    }
                                },
                                onClick = {
                                    selectedCategory = cat
                                    if (cat != "Others") customCategory = ""
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Custom category input
                AnimatedVisibility(
                    visible = selectedCategory == "Others",
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    OutlinedTextField(
                        value = customCategory,
                        onValueChange = { customCategory = it },
                        placeholder = { Text("Enter custom category") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outline.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Save Button
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val finalCategory =
                            if (selectedCategory == "Others") customCategory.trim()
                            else selectedCategory

                        val newItem = GroceryItem(
                            id = (0..Int.MAX_VALUE).random(),
                            name = name.trim(),
                            quantity = quantity.toIntOrNull() ?: 1,
                            category = finalCategory,
                            isBought = false
                        )

                        vm.add(newItem)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = name.isNotBlank() && selectedCategory.isNotEmpty()
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Add Item",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}