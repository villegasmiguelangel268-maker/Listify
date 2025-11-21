package com.example.listify.ui.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.listify.CATEGORY_LIST
import com.example.listify.GroceryItem
import com.example.listify.GroceryViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material3.MenuAnchorType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemScreen(
    navController: NavController,
    existingItem: GroceryItem,   // ✅ Not nullable
    vm: GroceryViewModel = viewModel()
) {
    var name by rememberSaveable(existingItem.id) { mutableStateOf(existingItem.name) }
    var quantity by rememberSaveable(existingItem.id) {
        mutableStateOf(existingItem.quantity.toString())
    }

    // ⭐ Category selection
    var selectedCategory by rememberSaveable(existingItem.id) {
        mutableStateOf(
            if (existingItem.category in CATEGORY_LIST) existingItem.category else "Others"
        )
    }

    var customCategory by rememberSaveable(existingItem.id) {
        mutableStateOf(
            if (existingItem.category !in CATEGORY_LIST) existingItem.category else ""
        )
    }

    var expanded by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme

    // ⭐ For long-press steppers
    val scope = rememberCoroutineScope()
    fun startRepeating(action: () -> Unit, flag: MutableState<Boolean>) {
        scope.launch {
            delay(300)   // delay before auto-repeat starts
            while (flag.value) {
                action()
                delay(70)  // repeat speed
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Item") },
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {

            // NAME FIELD
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Item name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // ⭐ QUANTITY STEPPER WITH LONG PRESS SUPPORT
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {

                val decHold = remember { mutableStateOf(false) }
                val incHold = remember { mutableStateOf(false) }

                // Decrease button with long press
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
                    Icon(Icons.Default.Remove, "Decrease", Modifier.align(Alignment.Center))
                }

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter(Char::isDigit) },
                    singleLine = true,
                    modifier = Modifier.width(80.dp)
                )

                // Increase button with long press
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
                    Icon(Icons.Default.Add, "Increase", Modifier.align(Alignment.Center))
                }
            }

            Spacer(Modifier.height(12.dp))

            // ⭐ CATEGORY DROPDOWN (Editable only when "Others")
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {

                OutlinedTextField(
                    value = if (selectedCategory == "Others") customCategory else selectedCategory,
                    onValueChange = {
                        if (selectedCategory == "Others") customCategory = it
                    },
                    readOnly = selectedCategory != "Others",
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
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

            // SAVE BUTTON
            Button(
                onClick = {
                    val finalCategory =
                        if (selectedCategory == "Others") customCategory.trim() else selectedCategory

                    val updated = existingItem.copy(
                        name = name.trim(),
                        quantity = quantity.toIntOrNull() ?: existingItem.quantity,
                        category = finalCategory
                    )

                    vm.update(updated)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save changes")
            }
        }
    }
}
