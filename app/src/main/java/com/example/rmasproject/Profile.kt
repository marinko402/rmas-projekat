package com.example.rmasproject

import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract.Profile
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.rmasproject.ui.theme.primary
import com.google.firebase.storage.FirebaseStorage
import java.io.File


@Composable
fun Profile(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    val db = FirebaseFirestore.getInstance()

    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }
    val tempUri = remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    var showBottomSheet by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            uploadImageToFirebaseStorage(it) { imageUrl ->
                updateUserProfileImage(userId, imageUrl)
            }
        }
    }

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
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                if (userData != null) {
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
                            .clip(CircleShape),
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
                        colors = ButtonDefaults.buttonColors(containerColor = primary),
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
                        colors = ButtonDefaults.buttonColors(containerColor = primary),
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

                    ImagePicker(
                        navController = navController,
                        modifier = Modifier,
                    )
                    if (showBottomSheet) {
                    }
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = primary),
                        onClick = {
                            if (!showBottomSheet)
                                showBottomSheet = true

                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Odaberi sliku iz galerije")
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
}



/*@Composable
fun selectImageFromGallery(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            uploadImageToFirebaseStorage(uri) { imageUrl ->
                updateUserProfileImage(userId, imageUrl)
            }
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch("image")
    }
}*/
fun uploadImageToFirebaseStorage(uri: Uri, onComplete: (String) -> Unit) {
    val storageRef = FirebaseStorage.getInstance().reference
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    val profileImagesRef = storageRef.child("profileImages/$userId.jpg")

    profileImagesRef.putFile(uri)
        .addOnSuccessListener {
            profileImagesRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                onComplete(downloadUrl.toString())
            }
        }
        .addOnFailureListener {
        }
}

fun updateUserProfileImage(userId: String?, imageUrl: String) {
    val db = FirebaseFirestore.getInstance()

    if (userId != null) {
        db.collection("users").document(userId)
            .update("profileImageUrl", imageUrl)
            .addOnSuccessListener {
            }
            .addOnFailureListener {
            }
    }
}
fun deleteUserAccount(onComplete: (Boolean) -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser

    if (user != null) {
        FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .delete()
            .addOnSuccessListener {
                user.delete()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onComplete(true)
                        } else {
                            onComplete(false)
                        }
                    }
            }
            .addOnFailureListener {
                onComplete(false)
            }
    } else {
        onComplete(false)
    }
}
fun logoutUser(onComplete: () -> Unit) {
    FirebaseAuth.getInstance().signOut()
    onComplete()
}
