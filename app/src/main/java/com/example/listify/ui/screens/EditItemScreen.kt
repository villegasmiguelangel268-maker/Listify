package com.example.listify.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.listify.CATEGORY_LIST
import com.example.listify.GroceryItem
import com.example.listify.GroceryViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemScreen(
    navController: NavController,
    existingItem: GroceryItem,   // ✅ NO LONGER NULLABLE
    vm: GroceryViewModel = viewModel()
) {
    var name by rememberSaveable(existingItem.id) { mutableStateOf(existingItem.name) }
    var quantity by rememberSaveable(existingItem.id) { mutableStateOf(existingItem.quantity.toString()) }

    // ⭐ Category selection logic
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

            // ⭐ Quantity Stepper
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {

                IconButton(onClick = {
                    val q = quantity.toIntOrNull() ?: 1
                    if (q > 1) quantity = (q - 1).toString()
                }) {
                    Icon(Icons.Default.Remove, "Decrease")
                }

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter(Char::isDigit) },
                    singleLine = true,
                    modifier = Modifier.width(80.dp)
                )

                IconButton(onClick = {
                    val q = quantity.toIntOrNull() ?: 1
                    quantity = (q + 1).toString()
                }) {
                    Icon(Icons.Default.Add, "Increase")
                }
            }

            Spacer(Modifier.height(12.dp))

            // ⭐ Category Dropdown
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

            Button(
                onClick = {
                    val finalCategory =
                        if (selectedCategory == "Others") customCategory.trim()
                        else selectedCategory

                    val updated = existingItem.copy(
                        name = name.trim(),
                        quantity = quantity.toIntOrNull() ?: existingItem.quantity,
                        category = finalCategory
                    )

                    vm.update(updated)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save changes") }
        }
    }
}
