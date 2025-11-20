package com.example.listify.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.listify.GroceryItem
import com.example.listify.GroceryViewModel
import com.example.listify.ui.screens.AddItemScreen
import com.example.listify.ui.screens.EditItemScreen
import com.example.listify.ui.screens.HomeScreen
import com.example.listify.ui.screens.SplashScreen

@Composable
fun AppNavGraph(navController: NavHostController) {

    // ⭐ Create ONE shared ViewModel for ALL screens
    val sharedViewModel: GroceryViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {

        composable("splash") {
            SplashScreen(navController)
        }

        composable("home") {
            HomeScreen(navController, vm = sharedViewModel)
        }

        composable("add") {
            AddItemScreen(navController, vm = sharedViewModel)
        }

        composable("edit") { backStackEntry ->

            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("home")
            }

            val existingItem =
                parentEntry.savedStateHandle.get<GroceryItem>("editItem")

            EditItemScreen(
                navController = navController,
                existingItem = existingItem,
                vm = sharedViewModel
            )
        }
    }
}
