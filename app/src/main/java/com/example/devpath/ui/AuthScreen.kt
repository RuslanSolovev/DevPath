package com.example.devpath.ui

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.util.Log
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import com.example.devpath.data.repository.YdbRepository
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.regex.Pattern

@Composable
fun AuthScreen(
    ydbRepository: YdbRepository,
    onSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isSignUpMode by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    fun isValidEmail(email: String): Boolean {
        return Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        ).matcher(email).matches()
    }

    LaunchedEffect(isSignUpMode) {
        errorMessage = null
        emailError = false
        passwordError = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "DevPath",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isSignUpMode) "Регистрация" else "Вход",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Сообщение об ошибке
        errorMessage?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "❌", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Поле имени (только для регистрации)
        if (isSignUpMode) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; errorMessage = null },
                label = { Text("Имя") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = name.isBlank() && errorMessage != null
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Поле email
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it.trim()
                emailError = false
                errorMessage = null
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = emailError,
            supportingText = if (emailError) {
                { Text("Введите корректный email", color = MaterialTheme.colorScheme.error) }
            } else null
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Поле пароля
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = false
                errorMessage = null
            },
            label = { Text("Пароль") },
            modifier = Modifier.fillMaxWidth(),
            isError = passwordError,
            supportingText = if (passwordError) {
                { Text("Минимум 6 символов", color = MaterialTheme.colorScheme.error) }
            } else null
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Индикатор загрузки
        if (isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Кнопка действия
        Button(
            onClick = {
                errorMessage = null
                emailError = false
                passwordError = false

                // Валидация
                var hasError = false
                if (!isValidEmail(email)) {
                    emailError = true
                    errorMessage = "Введите корректный email"
                    hasError = true
                }
                if (password.length < 6) {
                    passwordError = true
                    if (!hasError) errorMessage = "Пароль должен содержать минимум 6 символов"
                    hasError = true
                }
                if (isSignUpMode && name.isBlank()) {
                    if (!hasError) errorMessage = "Введите имя"
                    hasError = true
                }
                if (hasError) return@Button

                isLoading = true

                coroutineScope.launch {
                    try {
                        if (isSignUpMode) {
                            // === РЕГИСТРАЦИЯ ===
                            Log.d("Auth", "Регистрация: email=$email, name=$name")

                            val existingUser = ydbRepository.findUserByEmail(email)
                            if (existingUser != null) {
                                errorMessage = "Пользователь с таким email уже существует"
                                isLoading = false
                                return@launch
                            }

                            val userId = UUID.randomUUID().toString()
                            val success = ydbRepository.createUser(
                                userId = userId,
                                name = name.trim(),
                                email = email,
                                password = password
                            )

                            if (!success) {
                                errorMessage = "Не удалось создать аккаунт. Попробуйте позже."
                                isLoading = false
                                return@launch
                            }

                            // Сохранение сессии
                            context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
                                .edit().apply {
                                    putString("user_id", userId)
                                    putString("user_name", name.trim())
                                    putString("user_email", email)
                                    putString("user_avatar", "")
                                    putBoolean("is_logged_in", true)
                                    apply()
                                }

                            Log.d("Auth", "✅ Регистрация успешна!")
                            isLoading = false
                            onSuccess(userId)

                        } else {
                            // === ВХОД ===
                            Log.d("Auth", "Вход: email=$email")

                            val userId = ydbRepository.authenticateUser(email, password)

                            if (userId == null) {
                                errorMessage = "Неверный email или пароль"
                                isLoading = false
                                return@launch
                            }

                            val user = ydbRepository.getUser(userId)
                            val userName = user?.optJSONObject("name")?.optString("S") ?: "Пользователь"
                            val userEmail = user?.optJSONObject("email")?.optString("S") ?: email
                            val userAvatar = user?.optJSONObject("avatar_url")?.optString("S") ?: ""

                            // Обновление last_seen
                            ydbRepository.updateLastSeen(userId)

                            // Сохранение сессии
                            context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
                                .edit().apply {
                                    putString("user_id", userId)
                                    putString("user_name", userName)
                                    putString("user_email", userEmail)
                                    putString("user_avatar", userAvatar)
                                    putBoolean("is_logged_in", true)
                                    apply()
                                }

                            Log.d("Auth", "✅ Вход выполнен!")
                            isLoading = false
                            onSuccess(userId)
                        }
                    } catch (e: Exception) {
                        Log.e("Auth", "❌ Ошибка: ${e.message}", e)
                        errorMessage = "Ошибка соединения. Проверьте интернет."
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Подождите...")
            } else {
                Text(
                    if (isSignUpMode) "Зарегистрироваться" else "Войти",
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Переключатель режимов
        TextButton(
            onClick = {
                isSignUpMode = !isSignUpMode
                if (!isSignUpMode) name = ""
                errorMessage = null
                emailError = false
                passwordError = false
            }
        ) {
            Text(
                if (isSignUpMode) "Уже есть аккаунт? Войти"
                else "Нет аккаунта? Зарегистрироваться",
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isSignUpMode)
                "Регистрируясь, вы принимаете условия использования"
            else
                "Войдите, чтобы продолжить обучение",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}