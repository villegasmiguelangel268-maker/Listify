package com.example.listify.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.listify.GroceryItem
import com.example.listify.ui.screens.AddItemScreen
import com.example.listify.ui.screens.EditItemScreen
import com.example.listify.ui.screens.HomeScreen
import com.example.listify.ui.screens.SplashScreen

@Composable
fun AppNavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {

        composable("splash") {
            SplashScreen(navController)
        }

        composable("home") {
            HomeScreen(navController)
        }

        composable("add") {
            AddItemScreen(navController)
        }

        composable("edit") { backStackEntry ->

            // Remember the HOME backStackEntry to avoid recomposition warnings
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("home")
            }

            // Safely fetch "editItem"
            val existingItem =
                parentEntry.savedStateHandle.get<Any>("editItem") as? GroceryItem

            EditItemScreen(
                navController = navController,
                existingItem = existingItem
            )
        }
    }
}
