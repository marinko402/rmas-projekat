package com.example.rmasproject

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.rmasproject.models.Activity
import com.example.rmasproject.models.User
import com.example.rmasproject.notifications.notifyOtherPlayers
import com.example.rmasproject.ui.theme.primary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await

@Composable
fun ActivityDetails(navController: NavController, activityId: String) {
    val db = FirebaseFirestore.getInstance()

    var activity by remember { mutableStateOf<Activity?>(null) }
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isUserRegistered by remember { mutableStateOf(false) }
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""
    val context = LocalContext.current



    LaunchedEffect(activityId) {
        try {
            val activitySnapshot = db.collection("activities").document(activityId).get().await()
            activity = activitySnapshot.toObject(Activity::class.java)
            activity?.let { act ->
                isUserRegistered = act.players.contains(userId)
            }
            activity?.let { act ->
                val players = act.players
                val fetchedUsers = mutableListOf<User>()

                try {
                    val userDeferreds = players.map { playerId ->
                        async {
                            val userSnapshot = db.collection("users")
                                .document(playerId)
                                .get()
                                .await()

                            if (userSnapshot.exists()) {
                                userSnapshot.toObject(User::class.java)
                            } else {
                                null
                            }
                        }
                    }

                    val usersResults = userDeferreds.awaitAll().filterNotNull()

                    fetchedUsers.addAll(usersResults)
                    users = fetchedUsers

                } catch (e: Exception) {
                    Log.e("ActivityDetails", "Error fetching users: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("ActivityDetails", "Error fetching activity: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { innerPadding ->
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
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
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Top
                    ) {
                        activity?.let { act ->
                            Text(text = "Sport: ${act.sport}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Max broj igrača: ${act.playersCount}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Datum: ${act.date}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Vreme: ${act.time}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Lokacija: Lat: ${act.lat}, Lng: ${act.lng}")
                            Spacer(modifier = Modifier.height(16.dp))
                            if (users.isNotEmpty()) {
                                users.forEach { user ->
                                    Text(text = user.username)
                                }
                            } else {
                                Text(text = "Nema prijavljenih korisnika.")
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            if (isUserRegistered) {
                                Button(
                                    colors = ButtonDefaults.buttonColors(containerColor = primary),
                                    onClick = {
                                        unregisterFromActivity(activityId, userId, db) {
                                            navController.popBackStack()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Odjavi se sa aktivnosti")
                                }
                            } else {
                                Button(
                                    colors = ButtonDefaults.buttonColors(containerColor = primary),
                                    onClick = {
                                        registerForActivity(activityId, userId, db, context) {
                                            navController.popBackStack()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Prijavi se za aktivnost")
                                }
                            }

                        } ?: run {
                            Text(text = "Nema dostupnih podataka o aktivnosti.")
                        }
                    }
                }
            }
        }
    )
}

// Funkcija za prijavu na aktivnost
fun registerForActivity(activityId: String, userId: String, db: FirebaseFirestore, context: Context, onComplete: () -> Unit) {
    db.collection("activities").document(activityId).update("players", FieldValue.arrayUnion(userId))
        .addOnCompleteListener {
            onComplete()
            increaseUserScore()
            //notifyOtherPlayers(context, activityId, userId, db)
        }

}

// Funkcija za odjavu sa aktivnosti
fun unregisterFromActivity(activityId: String, userId: String, db: FirebaseFirestore, onComplete: () -> Unit) {
    db.collection("activities").document(activityId).update("players", FieldValue.arrayRemove(userId))
        .addOnCompleteListener {
            onComplete()
            decreaseUserScore()
        }

}

fun increaseUserScore() {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    if (currentUser != null) {
        val userId = currentUser.uid
        val userRef = db.collection("users").document(userId)

        userRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val currentScore = documentSnapshot.getLong("score") ?: 0
                userRef.update("score", currentScore + 100)
            }
        }.addOnFailureListener { exception ->
            // Obrada greške, na primer, ispisivanje poruke u log
            exception.printStackTrace()
        }
    }
}

fun decreaseUserScore() {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    if (currentUser != null) {
        val userId = currentUser.uid
        val userRef = db.collection("users").document(userId)

        userRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val currentScore = documentSnapshot.getLong("score") ?: 0
                userRef.update("score", currentScore - 100)
            }
        }.addOnFailureListener { exception ->
            // Obrada greške, na primer, ispisivanje poruke u log
            exception.printStackTrace()
        }
    }
}

