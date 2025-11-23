package com.example.listify.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.listify.GroceryViewModel
import com.example.listify.ui.screens.AddItemScreen
import com.example.listify.ui.screens.EditItemScreen
import com.example.listify.ui.screens.HomeScreen
import com.example.listify.ui.screens.ListsScreen
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

        composable("lists") {
            ListsScreen(navController, vm = sharedViewModel)
        }

        composable("add") {
            AddItemScreen(navController, vm = sharedViewModel)
        }

        composable("edit") {
            EditItemScreen(
                navController = navController,
                vm = sharedViewModel
            )
        }
    }
}