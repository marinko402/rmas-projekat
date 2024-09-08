package com.example.rmasproject

import android.provider.ContactsContract.Profile
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun Profile() {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid // Uzimamo ID trenutnog korisnika
    val db = FirebaseFirestore.getInstance()

    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }

    LaunchedEffect(userId) {
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        userData = document.data
                    }
                }
                .addOnFailureListener {
                    userData = null
                }
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (userData != null) {
                val data = userData!!
                Text(text = "Ime: ${data["name"]}")
                Text(text = "Prezime: ${data["surname"]}")
                Text(text = "Email: ${data["email"]}")
                Text(text = "Telefon: ${data["phone"]}")
                Text(text = "Score: ${data["score"]}")
            } else {
                Text(text = "Podaci nisu dostupni.")
            }
        }
    }
}


/*fun getUserData(userId: String, onResult: (Map<String, Any>?) -> Unit) {
    db.collection("users").document(userId).get()
        .addOnSuccessListener { document ->
            if (document != null) {
                onResult(document.data)
            } else {
                onResult(null)
            }
        }
        .addOnFailureListener {
            onResult(null)
        }
}*/