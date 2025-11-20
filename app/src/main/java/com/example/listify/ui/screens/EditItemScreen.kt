package com.example.listify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.listify.GroceryItem
import com.example.listify.GroceryViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemScreen(
    navController: NavController,
    existingItem: GroceryItem?,
    vm: GroceryViewModel = viewModel()
) {
    // Initialize fields based on existing item
    var name by rememberSaveable(existingItem?.id) { mutableStateOf(existingItem?.name ?: "") }
    var category by rememberSaveable(existingItem?.id) { mutableStateOf(existingItem?.category ?: "") }
    var quantity by rememberSaveable(existingItem?.id) {
        mutableStateOf(existingItem?.quantity?.toString() ?: "1")
    }

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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    titleContentColor = colors.onSurface
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
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

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it.filter(Char::isDigit) },
                label = { Text("Quantity") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val updated = existingItem?.copy(
                        name = name.trim(),
                        category = category.trim(),
                        quantity = quantity.toIntOrNull() ?: existingItem.quantity
                    )

                    if (updated != null) {
                        vm.update(updated)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save changes")
            }
        }
    }
}
