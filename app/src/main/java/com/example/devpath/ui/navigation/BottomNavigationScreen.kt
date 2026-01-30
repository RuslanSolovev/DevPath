package com.example.devpath.ui.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationScreen(
    navController: NavHostController = rememberNavController(),
    onSignOut: () -> Unit
    ){

}