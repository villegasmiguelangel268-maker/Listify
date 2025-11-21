package com.example.listify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.listify.CATEGORY_LIST
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

    // Category dropdown state
    var selectedCategory by rememberSaveable { mutableStateOf("") }
    var customCategory by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val colors = MaterialTheme.colorScheme

    // Coroutine scope for long press
    val scope = rememberCoroutineScope()

    fun startRepeating(action: () -> Unit, stopFlag: MutableState<Boolean>) {
        scope.launch {
            delay(300) // delay before auto-repeat
            while (stopFlag.value) {
                action()
                delay(70) // repeat speed
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Item") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Item name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // ------------------------------------------------------------
            // ⭐ Quantity Stepper with LONG PRESS SUPPORT
            // ------------------------------------------------------------
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {

                val decHold = remember { mutableStateOf(false) }
                val incHold = remember { mutableStateOf(false) }

                // Decrease button
                Box(
                    modifier = Modifier
                        .size(40.dp)
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
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Decrease",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter(Char::isDigit) },
                    singleLine = true,
                    modifier = Modifier.width(80.dp)
                )

                // Increase button
                Box(
                    modifier = Modifier
                        .size(40.dp)
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
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Increase",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ------------------------------------------------------------
            // ⭐ Category Dropdown (with custom category for "Others")
            // ------------------------------------------------------------
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {

                OutlinedTextField(
                    value = if (selectedCategory == "Others") customCategory else selectedCategory,
                    onValueChange = { if (selectedCategory == "Others") customCategory = it },
                    readOnly = selectedCategory != "Others",
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor(
                            type = MenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        )
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    CATEGORY_LIST.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                selectedCategory = cat
                                if (cat != "Others") customCategory = ""
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

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
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}
