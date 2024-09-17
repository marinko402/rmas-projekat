package com.example.rmasproject

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

@Composable
fun Register(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher za biranje slike
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Registracija")

                Spacer(modifier = Modifier.height(16.dp))

                // Polje za korisničko ime
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Korisničko ime") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Ime") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = surname,
                    onValueChange = { surname = it },
                    label = { Text("Prezime") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefon") },
                    modifier = Modifier.fillMaxWidth()
                )

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

                // Prikaz slike i dugme za biranje slike
                imageUri?.let {
                    Image(
                        painter = rememberImagePainter(data = it),
                        contentDescription = null,
                        modifier = Modifier
                            .height(100.dp)
                            .fillMaxWidth()
                            .clickable { imagePickerLauncher.launch("image/*") }
                    )
                } ?: Text(
                    text = "Dodajte sliku",
                    color = Color.Gray,
                    modifier = Modifier
                        .clickable { imagePickerLauncher.launch("image/*") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        registerUser(email, password) { isSuccess ->
                            if (isSuccess) {
                                val userId = FirebaseAuth.getInstance().currentUser?.uid
                                if (userId != null) {
                                    // Sačuvaj korisničke podatke
                                    saveUserData(userId, name, surname, email, phone, username, 0)

                                    // Postavi profilnu sliku
                                    imageUri?.let { uri ->
                                        uploadProfilePicture(userId, uri)
                                    }
                                    navController.navigate(Screens.Login.screen)
                                }
                            } else {
                                errorMessage = "Registracija nije uspela."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registruj se")
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = errorMessage ?: "", color = Color.Red)
                }

                TextButton(onClick = {
                    navController.navigate(Screens.Login.screen)
                }) {
                    Text("Već imate nalog? Ulogujte se")
                }
            }
        }
    )
}



val auth = FirebaseAuth.getInstance()
val db = FirebaseFirestore.getInstance()

fun registerUser(email: String, password: String, onComplete: (Boolean) -> Unit) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
}
fun saveUserData(userId: String, name: String, surname: String, email: String, phone: String, username: String, score: Int) {
    val user = hashMapOf(
        "name" to name,
        "surname" to surname,
        "email" to email,
        "phone" to phone,
        "username" to username,
        "score" to score
    )

    db.collection("users").document(userId)
        .set(user)
        .addOnSuccessListener {
            // Uspesno sacuvano
        }
        .addOnFailureListener {
            // Neuspesno sacuvano
        }
}
fun uploadProfilePicture(userId: String, imageUri: Uri) {
    val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/$userId.jpg")
    storageRef.putFile(imageUri)
        .addOnSuccessListener {
            // Uspešno postavljena slika
        }
        .addOnFailureListener {
            // Neuspešno postavljanje slike
        }
}