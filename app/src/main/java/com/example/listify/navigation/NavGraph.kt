package com.example.listify.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.listify.ui.screens.*

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "splash" // ✅ Make sure splash is the start
    ) {
        composable("splash") { SplashScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("add") {
            // ✅ Retrieve editItem from the *previous* screen
            val editItem = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<GroceryItem>("editItem")

            AddEditItemScreen(
                navController = navController,
                existingItem = editItem
            )
        }


    }
}
