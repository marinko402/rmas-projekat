package com.example.rmasproject

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.rmasproject.models.Activity
import com.example.rmasproject.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await

@Composable
fun ActivityDetails(navController: NavController, activityId: String) {
    val db = FirebaseFirestore.getInstance()

    // State za aktivnost i korisnike
    var activity by remember { mutableStateOf<Activity?>(null) }
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isUserRegistered by remember { mutableStateOf(false) }
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""


    LaunchedEffect(activityId) {
        // Fetch activity details
        try {
            val activitySnapshot = db.collection("activities").document(activityId).get().await()
            activity = activitySnapshot.toObject(Activity::class.java)
            activity?.let { act ->
                // Check if the user is registered in the activity
                isUserRegistered = act.players.contains(userId)
            }
            // Ako je aktivnost pronađena, fetch-ujemo podatke o prijavljenim igračima
            activity?.let { act ->
                val players = act.players // Lista ID-jeva korisnika
                val fetchedUsers = mutableListOf<User>()

                try {
                    // Dohvati sve korisnike koji su prijavljeni paralelno
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

                    // Čekaj sve zahteve i filtriraj uspešno dobijene korisnike
                    val usersResults = userDeferreds.awaitAll().filterNotNull()

                    // Dodaj do liste korisnika
                    fetchedUsers.addAll(usersResults)
                    users = fetchedUsers // Postavi korisnike za prikaz

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
                // Loading state
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Top
                ) {
                    activity?.let { act ->
                        // Prikaz podataka o aktivnosti
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
                                onClick = {
                                    unregisterFromActivity(activityId, userId, db) {
                                        navController.popBackStack() // Vrati se na prethodni ekran
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Odjavi se sa aktivnosti")
                            }
                        } else {
                            Button(
                                onClick = {
                                    registerForActivity(activityId, userId, db) {
                                        navController.popBackStack() // Vrati se na prethodni ekran
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Prijavi se za aktivnost")
                            }
                        }

                    } ?: run {
                        // Ako aktivnost nije pronađena
                        Text(text = "Nema dostupnih podataka o aktivnosti.")
                    }
                }
            }
        }
    )
}

// Funkcija za prijavu na aktivnost
fun registerForActivity(activityId: String, userId: String, db: FirebaseFirestore, onComplete: () -> Unit) {
    db.collection("activities").document(activityId).update("players", FieldValue.arrayUnion(userId))
        .addOnCompleteListener {
            onComplete()
        }
}

// Funkcija za odjavu sa aktivnosti
fun unregisterFromActivity(activityId: String, userId: String, db: FirebaseFirestore, onComplete: () -> Unit) {
    db.collection("activities").document(activityId).update("players", FieldValue.arrayRemove(userId))
        .addOnCompleteListener {
            onComplete()
        }
}
