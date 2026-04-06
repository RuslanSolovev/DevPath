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
import com.google.firebase.firestore.ktx.firestore

@Composable
fun AuthScreen(onSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isSignUpMode by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isSignUpMode) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Имя") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

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

        if (isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = {
                if (isSignUpMode) {
                    signUp(email, password, name, onSuccess) { isLoading = it }
                } else {
                    signIn(email, password, onSuccess) { isLoading = it }
                }
            },
            enabled = !isLoading
        ) {
            if (isLoading) {
                Text("Подождите...")
            } else {
                Text(if (isSignUpMode) "Зарегистрироваться" else "Войти")
            }
        }

        TextButton(
            onClick = {
                isSignUpMode = !isSignUpMode
                if (!isSignUpMode) {
                    name = ""
                }
            }
        ) {
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
    name: String,
    onSuccess: () -> Unit,
    onLoadingChange: (Boolean) -> Unit
) {
    val cleanEmail = email.trim()
    val cleanPassword = password.trim()
    val cleanName = name.trim()

    if (cleanEmail.isBlank() || cleanPassword.isBlank()) {
        Log.e("Auth", "❌ Email или пароль пустые")
        onLoadingChange(false)
        return
    }

    if (cleanName.isBlank()) {
        Log.e("Auth", "❌ Имя не может быть пустым")
        onLoadingChange(false)
        return
    }

    onLoadingChange(true)
    Log.d("Auth", "Регистрация нового пользователя: $cleanEmail, имя: $cleanName")

    val auth = Firebase.auth
    auth.createUserWithEmailAndPassword(cleanEmail, cleanPassword)
        .addOnSuccessListener { result ->
            Log.d("Auth", "✅ Регистрация успешна!")

            val userId = result.user?.uid ?: ""
            val userEmail = result.user?.email ?: cleanEmail

            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(cleanName)
                .build()
            result.user?.updateProfile(profileUpdates)

            val db = Firebase.firestore
            val userProfile = hashMapOf(
                "userId" to userId,
                "name" to cleanName,
                "nameLowercase" to cleanName.lowercase(),
                "email" to userEmail,
                "emailLowercase" to userEmail.lowercase(),
                "lastSeen" to com.google.firebase.Timestamp.now(),
                "lastActiveInApp" to com.google.firebase.Timestamp.now(),
                "createdAt" to com.google.firebase.Timestamp.now()
            )

            db.collection("users").document(userId)
                .set(userProfile)
                .addOnSuccessListener {
                    Log.d("Auth", "✅ Профиль пользователя создан в Firestore")
                    onLoadingChange(false)
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e("Auth", "❌ Ошибка создания профиля: ${e.message}")
                    onLoadingChange(false)
                    onSuccess()
                }
        }
        .addOnFailureListener { exception ->
            Log.e("Auth", "❌ Ошибка регистрации: ${exception.message}")
            onLoadingChange(false)
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
        onLoadingChange(false)
        return
    }

    onLoadingChange(true)
    Log.d("Auth", "Отправляем запрос на вход: $email")

    val auth = Firebase.auth

    auth.signInWithEmailAndPassword(email, password)
        .addOnSuccessListener { result ->
            Log.d("Auth", "✅ Успешный вход!")

            val userId = result.user?.uid ?: ""
            val userEmail = result.user?.email ?: email

            val db = Firebase.firestore
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (!document.exists()) {
                        val userName = result.user?.displayName ?: userEmail.split("@").first()
                        val userProfile = hashMapOf(
                            "userId" to userId,
                            "name" to userName,
                            "nameLowercase" to userName.lowercase(),
                            "email" to userEmail,
                            "emailLowercase" to userEmail.lowercase(),
                            "lastSeen" to com.google.firebase.Timestamp.now(),
                            "lastActiveInApp" to com.google.firebase.Timestamp.now(),
                            "createdAt" to com.google.firebase.Timestamp.now()
                        )
                        db.collection("users").document(userId).set(userProfile)
                            .addOnSuccessListener {
                                Log.d("Auth", "✅ Профиль создан")
                                onLoadingChange(false)
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                Log.e("Auth", "❌ Ошибка: ${e.message}")
                                onLoadingChange(false)
                                onSuccess()
                            }
                    } else {
                        db.collection("users").document(userId)
                            .update(
                                "lastSeen", com.google.firebase.Timestamp.now(),
                                "lastActiveInApp", com.google.firebase.Timestamp.now()
                            )
                            .addOnSuccessListener {
                                onLoadingChange(false)
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                Log.e("Auth", "❌ Ошибка: ${e.message}")
                                onLoadingChange(false)
                                onSuccess()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Auth", "❌ Ошибка: ${e.message}")
                    onLoadingChange(false)
                    onSuccess()
                }
        }
        .addOnFailureListener { exception ->
            Log.e("Auth", "❌ Ошибка входа: ${exception.message}")
            onLoadingChange(false)
        }
}