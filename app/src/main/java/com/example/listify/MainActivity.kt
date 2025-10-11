package com.example.listify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.listify.navigation.AppNavGraph
import com.example.listify.ui.theme.ListifyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ListifyTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    // ✅ Set up Navigation
                    val navController = rememberNavController()
                    AppNavGraph(navController)
                }
            }
        }
    }
}
