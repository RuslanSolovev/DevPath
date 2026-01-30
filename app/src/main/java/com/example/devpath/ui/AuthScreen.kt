package com.example.devpath.ui

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import android.util.Log
import androidx.compose.ui.Alignment

@Composable
fun AuthScreen(onSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUpMode by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { newValue ->
                email = newValue.trim()
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading){
            CircularProgressIndicator()

            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = {
                if (isSignUpMode) {
                    signUp(email, password, onSuccess) { isLoading = it }
                } else {
                    signIn(email, password, onSuccess) { isLoading = it }
                }
            },
            enabled = !isLoading

        ) {
            if (isLoading) {
                Text("Подождите...")
            } else {
                Text(if (isSignUpMode) " Зарегистрироваться" else "Войти")

            }

        }
            TextButton(
                onClick = { isSignUpMode = !isSignUpMode }) {
                Text(
                    if (isSignUpMode) "Уже есть аккаунт? Войти"
                    else "Нет аккаунта? Зарегистрироваться"
                )
            }
        }
    }


private fun signUp(
    email: String,
    password: String,
    onSuccess: () -> Unit,
    onLoadingChange: (Boolean) -> Unit
) {
    val cleanEmail = email.trim()
    val cleanPassword = password.trim()
    if (cleanEmail.isBlank() || cleanPassword.isBlank()) {
        return
    }

    onLoadingChange(true) // ← ДОБАВЬ ЭТО
    Log.d("Auth", "Регистрация нового пользователя: $cleanEmail")

    val auth = Firebase.auth
    auth.createUserWithEmailAndPassword(cleanEmail, cleanPassword)
        .addOnSuccessListener {
            Log.d("Auth", "✅ Регистрация успешна!")
            onLoadingChange(false) // ← ДОБАВЬ ЭТО
            onSuccess()
        }
        .addOnFailureListener { exception ->
            Log.e("Auth", "❌ Ошибка регистрации: ${exception.message}")
            onLoadingChange(false) // ← ДОБАВЬ ЭТО
        }
}

private fun signIn(
    email: String,
    password: String,
    onSuccess: () -> Unit,
    onLoadingChange: (Boolean) -> Unit
) {

    if (email.isBlank() || password.isBlank()) {
        Log.e("Auth", "❌ Email или пароль пустые")
        return
    }

    onLoadingChange(true)

    Log.d("Auth", "Отправляем запрос на вход: $email")

    val auth = Firebase.auth

    auth.signInWithEmailAndPassword(email, password)
        .addOnSuccessListener {
            Log.d("Auth", "✅ Успешный вход!")
            onLoadingChange(false)
            onSuccess()
        }
        .addOnFailureListener { exception ->
            Log.e("Auth", "❌ Ошибка входа: ${exception.message}")
            onLoadingChange(false)
        }
}