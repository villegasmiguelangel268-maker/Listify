package com.example.listify.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.listify.*
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.detectTapGestures

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemScreen(
    navController: NavController,
    vm: GroceryViewModel = viewModel()
) {
    // ⭐ FIX: Use currentBackStackEntry instead of previousBackStackEntry
    val editItem = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<GroceryItem>("editItem")
        ?: navController.currentBackStackEntry
            ?.savedStateHandle
            ?.get<GroceryItem>("editItem")

    // Handle null and avoid crash
    if (editItem == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    var name by remember { mutableStateOf(editItem.name) }
    var quantity by remember { mutableStateOf(editItem.quantity.toString()) }
    var price by remember { mutableStateOf(
        if (editItem.price > 0)
            PriceFormatter.formatWithoutSymbol(editItem.price)
        else
            ""
    ) }
    var selectedCategory by remember { mutableStateOf(editItem.category) }
    var customCategory by remember { mutableStateOf("") }
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
                    IconButton(onClick = {
                        // ⭐ FIX: Remove from CURRENT entry, not previous
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.remove<GroceryItem>("editItem")

                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.onSurface
                        )
                    }

                    Text(
                        "Edit Item",
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

            // ITEM NAME
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

            // QUANTITY
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

                    // DECREASE BUTTON
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

                    // DISPLAY QUANTITY
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

                    // INCREASE BUTTON
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

            // PRICE
            Column {
                Text(
                    "Price (Optional)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            price = newValue
                        }
                    },
                    placeholder = { Text("0.00") },
                    leadingIcon = {
                        Text(
                            "₱",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.outline.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )

                val priceValue = price.toDoubleOrNull() ?: 0.0
                val qtyValue = quantity.toIntOrNull() ?: 1
                if (priceValue > 0 && qtyValue > 1) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Total: ${PriceFormatter.format(priceValue * qtyValue)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.primary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // CATEGORY
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
                            .menuAnchor()
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

            // SAVE BUTTON
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val finalCategory =
                            if (selectedCategory == "Others") customCategory.trim()
                            else selectedCategory

                        val updatedItem = editItem.copy(
                            name = name.trim(),
                            quantity = quantity.toIntOrNull() ?: 1,
                            category = finalCategory,
                            price = price.toDoubleOrNull() ?: 0.0
                        )

                        vm.update(updatedItem)

                        // ⭐ FIX: Remove from CURRENT entry
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.remove<GroceryItem>("editItem")

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
                    "Save Changes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
