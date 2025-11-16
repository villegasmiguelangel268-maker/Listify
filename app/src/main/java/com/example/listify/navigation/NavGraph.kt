package com.example.listify.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.listify.ui.screens.*
import com.example.listify.GroceryItem

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {

        composable("splash") { SplashScreen(navController) }

        composable("home") { HomeScreen(navController) }

        composable("add") {

            val editItem =
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<GroceryItem>("editItem")

            AddEditItemScreen(
                navController = navController,
                existingItem = editItem
            )
        }
    }
}
