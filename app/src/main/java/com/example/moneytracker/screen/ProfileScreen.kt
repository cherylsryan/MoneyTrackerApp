package com.example.moneytracker.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneytracker.repository.UserRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { UserRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    val isLoggedIn by UserRepository.isLoggedIn.collectAsState()
    var email by rememberSaveable { mutableStateOf(repository.getCurrentUserEmail() ?: "") }
    var name by rememberSaveable { mutableStateOf(repository.getCurrentUserName() ?: "") }
    var password by rememberSaveable { mutableStateOf("") }
    
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isSignUpMode by rememberSaveable { mutableStateOf(false) }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isEditingProfile by rememberSaveable { mutableStateOf(false) }

    val backgroundColor = Color(0xFFF6EFC9)
    val buttonColor = Color(0xFF1A237E)

    fun handleAuth() {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Email and password are required"
            return
        }
        if (isSignUpMode && password.length < 6) {
            errorMessage = "Password must be at least 6 characters"
            return
        }
        
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            val result = if (isSignUpMode) {
                repository.signUp(email, password)
            } else {
                repository.signIn(email, password)
            }
            
            result.onSuccess {
                isLoading = false
                password = ""
                name = repository.getCurrentUserName() ?: ""
                email = repository.getCurrentUserEmail() ?: ""
            }.onFailure {
                errorMessage = it.message ?: "Authentication failed"
                isLoading = false
            }
        }
    }

    fun handleUpdateProfile() {
        if (name.isBlank()) {
            errorMessage = "Name cannot be empty"
            return
        }
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            repository.updateProfile(name).onSuccess {
                isEditingProfile = false
                isLoading = false
            }.onFailure {
                errorMessage = it.message ?: "Update failed"
                isLoading = false
            }
        }
    }

    fun handleLogout() {
        coroutineScope.launch {
            repository.signOut().onSuccess {
                email = ""
                name = ""
                isEditingProfile = false
            }
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { 
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isLoggedIn) "Profile" else if (isSignUpMode) "Sign Up" else "Login", 
                            fontSize = 22.sp, 
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A237E)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                actions = {
                    Spacer(modifier = Modifier.size(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        if (!isLoggedIn) {
                            Text(
                                text = if (isSignUpMode) "Create an account" else "Welcome Back",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                            Text(
                                text = if (isSignUpMode) "Start your saving journey today" else "Login to manage your finances",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email Address") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF9F9F9),
                                    unfocusedContainerColor = Color(0xFFF9F9F9),
                                    focusedBorderColor = Color(0xFF1A237E),
                                    unfocusedBorderColor = Color(0xFFEEEEEE)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle Password Visibility"
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF9F9F9),
                                    unfocusedContainerColor = Color(0xFFF9F9F9),
                                    focusedBorderColor = Color(0xFF1A237E),
                                    unfocusedBorderColor = Color(0xFFEEEEEE)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            errorMessage?.let {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(text = it, color = Color.Red, fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Button(
                                onClick = { handleAuth() },
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = buttonColor,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(52.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Text(if (isSignUpMode) "Sign Up" else "Login", fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            TextButton(
                                onClick = { isSignUpMode = !isSignUpMode; errorMessage = null },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    if (isSignUpMode) "Already have an account? Login" else "Don't have an account? Sign Up",
                                    color = Color(0xFF1A237E),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Text(
                                text = "Profile Information", 
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Full Name") },
                                singleLine = true,
                                readOnly = !isEditingProfile,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = if (isEditingProfile) Color(0xFFF9F9F9) else Color.White,
                                    unfocusedContainerColor = if (isEditingProfile) Color(0xFFF9F9F9) else Color.White,
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = email,
                                onValueChange = { },
                                label = { Text("Account Email") },
                                singleLine = true,
                                readOnly = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF5F5F5),
                                    unfocusedContainerColor = Color(0xFFF5F5F5),
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            errorMessage?.let {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(text = it, color = Color.Red, fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            if (isEditingProfile) {
                                Button(
                                    onClick = { handleUpdateProfile() },
                                    enabled = !isLoading,
                                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor, contentColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().height(52.dp)
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Text("Save Changes", fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = { isEditingProfile = false; errorMessage = null },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().height(52.dp)
                                ) {
                                    Text("Cancel", color = Color.Black)
                                }
                            } else {
                                Button(
                                    onClick = { isEditingProfile = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor, contentColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().height(52.dp)
                                ) {
                                    Text("Edit Profile", fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = { handleLogout() },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().height(52.dp)
                                ) {
                                    Text("Logout", color = Color.Red, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
