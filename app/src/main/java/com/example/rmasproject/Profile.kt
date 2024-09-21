package com.example.rmasproject

import android.net.Uri
import android.provider.ContactsContract.Profile
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.storage.FirebaseStorage


@Composable
fun Profile(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    val db = FirebaseFirestore.getInstance()

    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var imgUri by remember { mutableStateOf<Uri?>(null) }

    // Preuzimamo podatke o korisniku
    LaunchedEffect(userId) {
        if (userId != null) {
            db.collection("users").document(userId).addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    userData = snapshot.data
                }
            }
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (userData != null) {
                // Prikaz profilne slike
                val profileImageUrl = userData!!["profileImageUrl"] as? String

                val painter = rememberImagePainter(
                    data = profileImageUrl ?: R.drawable.ic_placeholder,
                    builder = {
                        crossfade(true)
                        placeholder(R.drawable.ic_placeholder)
                    }
                )

                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .clickable {
                            // Show the Image Picker
                            imgUri = Uri.EMPTY // Reset the URI before opening the picker
                        },
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Username: ${userData!!["username"]}")
                Text(text = "Ime: ${userData!!["name"]}")
                Text(text = "Prezime: ${userData!!["surname"]}")
                Text(text = "Email: ${userData!!["email"]}")
                Text(text = "Telefon: ${userData!!["phone"]}")
                Text(text = "Score: ${userData!!["score"]}")

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        logoutUser {
                            navController.navigate(Screens.Login.screen) {
                                popUpTo(Screens.Profile.screen) { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Odjavi se")
                }
                Button(
                    onClick = {
                        deleteUserAccount {
                            navController.navigate(Screens.Login.screen) {
                                popUpTo(Screens.Profile.screen) { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Obrisi moj nalog")
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Integrate the ImagePicker here
                ImagePicker(
                    navController = navController,
                    modifier = Modifier,
                )

                // If an image was picked, upload it to Firebase
                imgUri?.let { imgUri ->
                        // Upload slike u Firebase Storage
                        uploadImageToFirebaseStorage(imgUri) { imageUrl ->
                            // Ažuriraj profilnu sliku korisnika u Firestore-u
                            updateUserProfileImage(userId, imageUrl)
                        }
                    }
            } else {
                Text(text = "Podaci nisu dostupni.")
                TextButton(onClick = {
                    navController.navigate(Screens.Login.screen)
                }) {
                    Text("Login")
                }
            }
        }
    }
}


fun logoutUser(onComplete: () -> Unit) {
    FirebaseAuth.getInstance().signOut()
    onComplete()
}
@Composable
fun selectImageFromGallery(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    // Launcher za pokretanje galerije
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Upload slike u Firebase Storage
            uploadImageToFirebaseStorage(uri) { imageUrl ->
                // Ažuriraj profilnu sliku korisnika u Firestore-u
                updateUserProfileImage(userId, imageUrl)
            }
        }
    }

    // Pokrećemo galeriju
    LaunchedEffect(Unit) {
        launcher.launch("image/*")
    }
}

fun uploadImageToFirebaseStorage(uri: Uri, onComplete: (String) -> Unit) {
    val storageRef = FirebaseStorage.getInstance().reference
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    val profileImagesRef = storageRef.child("profileImages/$userId.jpg")

    profileImagesRef.putFile(uri)
        .addOnSuccessListener {
            // Dobijamo URL slike
            profileImagesRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                onComplete(downloadUrl.toString())
            }
        }
        .addOnFailureListener {
            // Upravljaj greškama
        }
}

fun updateUserProfileImage(userId: String?, imageUrl: String) {
    val db = FirebaseFirestore.getInstance()

    if (userId != null) {
        db.collection("users").document(userId)
            .update("profileImageUrl", imageUrl)
            .addOnSuccessListener {
                // Uspešno ažuriran URL profilne slike
            }
            .addOnFailureListener {
                // Upravljaj greškama
            }
    }
}
fun deleteUserAccount(onComplete: (Boolean) -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser

    if (user != null) {
        // Prvo obriši podatke iz Firestore-a (ako je potrebno)
        FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .delete()
            .addOnSuccessListener {
                // Nakon što obrišeš podatke, obriši nalog iz Authentication
                user.delete()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onComplete(true) // Uspješno obrisan nalog
                        } else {
                            onComplete(false) // Greška prilikom brisanja naloga
                        }
                    }
            }
            .addOnFailureListener {
                onComplete(false) // Greška prilikom brisanja podataka iz Firestore-a
            }
    } else {
        onComplete(false) // Korisnik nije ulogovan
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