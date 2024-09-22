package com.example.rmasproject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun Login(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Login")

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        // Logika za login
                        loginUser(email, password) { isSuccess ->
                            if (isSuccess) {
                                // Navigacija na profil stranicu nakon uspešnog logina
                                navController.navigate(Screens.Profile.screen) {
                                    popUpTo(Screens.Login.screen) { inclusive = true }
                                }
                            } else {
                                // Ako login nije uspešan, postavi poruku o grešci
                                errorMessage = "Neuspešna prijava. Proverite email i lozinku."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Login")
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = errorMessage ?: "", color = androidx.compose.ui.graphics.Color.Red)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = {
                    // Navigacija na registraciju
                    navController.navigate(Screens.Register.screen)
                }) {
                    Text("Nemate nalog? Registrujte se")
                }
            }
        }
    )
}



/*fun loginUser(email: String, password: String, onComplete: (Boolean) -> Unit) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
}*/
fun loginUser(identifier: String, password: String, onComplete: (Boolean) -> Unit) {
    if (identifier.contains("@")) {
        // Ako je uneti podatak e-mail, direktno pozovi signIn
        auth.signInWithEmailAndPassword(identifier, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            }
    } else {
        // Ako je uneti podatak username, prvo nađi e-mail
        FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("username", identifier)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val email = documents.documents[0].getString("email")
                    if (!email.isNullOrEmpty()) {
                        // Sada koristi nađeni e-mail za login
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    onComplete(true)
                                } else {
                                    onComplete(false)
                                }
                            }
                    } else {
                        onComplete(false)
                    }
                } else {
                    onComplete(false)
                }
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }
}
