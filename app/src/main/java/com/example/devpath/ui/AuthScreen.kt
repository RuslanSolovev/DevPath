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
    var name by remember { mutableStateOf("") }  // ✅ Добавляем поле для имени
    var isSignUpMode by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ✅ Поле для имени (только при регистрации)
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
                // Очищаем поля при смене режима
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

            // ✅ Обновляем displayName в Firebase Auth
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(cleanName)
                .build()
            result.user?.updateProfile(profileUpdates)
                ?.addOnSuccessListener {
                    Log.d("Auth", "✅ Имя обновлено в Firebase Auth")
                }
                ?.addOnFailureListener { e ->
                    Log.e("Auth", "❌ Ошибка обновления имени: ${e.message}")
                }

            // ✅ Создаём профиль в Firestore
            val db = Firebase.firestore
            val userProfile = hashMapOf(
                "userId" to userId,
                "name" to cleanName,
                "email" to userEmail,
                "online" to true,
                "lastSeen" to com.google.firebase.Timestamp.now(),
                "createdAt" to com.google.firebase.Timestamp.now()
            )

            db.collection("users").document(userId)
                .set(userProfile)
                .addOnSuccessListener {
                    Log.d("Auth", "✅ Профиль пользователя создан в Firestore: имя=$cleanName")
                    onLoadingChange(false)
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e("Auth", "❌ Ошибка создания профиля в Firestore: ${e.message}")
                    // Всё равно считаем регистрацию успешной
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

            // ✅ Проверяем, есть ли профиль пользователя в Firestore
            val db = Firebase.firestore
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (!document.exists()) {
                        // Если профиля нет, создаём (для старых пользователей)
                        val userName = result.user?.displayName ?: userEmail.split("@").first()
                        val userProfile = hashMapOf(
                            "userId" to userId,
                            "name" to userName,
                            "email" to userEmail,
                            "online" to true,
                            "lastSeen" to com.google.firebase.Timestamp.now(),
                            "createdAt" to com.google.firebase.Timestamp.now()
                        )
                        db.collection("users").document(userId).set(userProfile)
                            .addOnSuccessListener {
                                Log.d("Auth", "✅ Профиль пользователя создан при входе")
                            }
                            .addOnFailureListener { e ->
                                Log.e("Auth", "❌ Ошибка создания профиля: ${e.message}")
                            }
                    } else {
                        // ✅ Обновляем статус online
                        db.collection("users").document(userId)
                            .update(
                                "online", true,
                                "lastSeen", com.google.firebase.Timestamp.now()
                            )
                            .addOnFailureListener { e ->
                                Log.e("Auth", "❌ Ошибка обновления статуса: ${e.message}")
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Auth", "❌ Ошибка проверки профиля: ${e.message}")
                }

            onLoadingChange(false)
            onSuccess()
        }
        .addOnFailureListener { exception ->
            Log.e("Auth", "❌ Ошибка входа: ${exception.message}")
            onLoadingChange(false)
        }
}