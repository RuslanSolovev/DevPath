package com.example.devpath.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


@Composable
fun HomeScreen(
    onSignOut: () -> Unit,
    onLessonsClick: () -> Unit )
{
   Column(
       modifier = Modifier
           .fillMaxSize()
           .padding(24.dp),
       verticalArrangement = Arrangement.Center,
       horizontalAlignment = Alignment.CenterHorizontally
   ){
       var user = Firebase.auth.currentUser
       Text("Привет, ${user?.email ?: "Пользователь" }")

       Spacer(modifier = Modifier.height(24.dp))

       Button(onClick = onLessonsClick) {
           Text("Начать обучение")
       }

       Spacer(modifier = Modifier.height(24.dp))

       Button(onClick = onSignOut) {
           Text("Выйти")
       }
   }

}