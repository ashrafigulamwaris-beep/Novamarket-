package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onBackClick: () -> Unit,
    onLogin: (email: String, pass: String) -> Unit,
    onRegister: (name: String, email: String, phone: String, pass: String) -> Unit,
    authError: String?,
    modifier: Modifier = Modifier
) {
    var isRegisterMode by remember { mutableStateOf(false) }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isRegisterMode) "Create NovaMarket Account" else "Sign In to NovaMarket", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(OffWhite)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Brand Logo & Heading
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(NavyDark, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, tint = AmberGold, modifier = Modifier.size(34.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text("NovaMarket India", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NavyDark)
            Text("Everything You Need, One Destination.", fontSize = 12.sp, color = TextSecondary)

            Spacer(modifier = Modifier.height(20.dp))

            // Auth Mode Toggle
            TabRow(
                selectedTabIndex = if (isRegisterMode) 1 else 0,
                containerColor = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Tab(
                    selected = !isRegisterMode,
                    onClick = { isRegisterMode = false; validationError = null },
                    text = { Text("Sign In", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = isRegisterMode,
                    onClick = { isRegisterMode = true; validationError = null },
                    text = { Text("Register", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (isRegisterMode) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) phone = it },
                            label = { Text("Mobile Number") },
                            prefix = { Text("+91 ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    val errorToShow = validationError ?: authError
                    if (errorToShow != null) {
                        Text(
                            text = errorToShow,
                            color = CrimsonRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (isRegisterMode) {
                                if (fullName.isBlank() || email.isBlank() || phone.length < 10 || password.length < 6) {
                                    validationError = "Please fill all details (Password min 6 chars)."
                                } else {
                                    validationError = null
                                    onRegister(fullName, email, phone, password)
                                }
                            } else {
                                if (email.isBlank() || password.isBlank()) {
                                    validationError = "Please enter your email and password."
                                } else {
                                    validationError = null
                                    onLogin(email, password)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("auth_submit_btn")
                    ) {
                        Text(if (isRegisterMode) "Create Account" else "Sign In", fontWeight = FontWeight.Bold)
                    }

                    // Demo credentials hint
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(color = OffWhite, shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Demo Accounts Available:", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = TextSecondary)
                            Text("Customer: customer@example.com / customer123", fontSize = 10.sp, color = TextMuted)
                            Text("Admin: admin@novamarket.in / admin123", fontSize = 10.sp, color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}
