package com.example.rmasproject

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.rmasproject.models.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun ActivityDetails(navController: NavController, activityId: String) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""
    val db = FirebaseFirestore.getInstance()

    var activity by remember { mutableStateOf<Activity>(Activity()) }
    var isUserRegistered by remember { mutableStateOf(false) }
    var userScore by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        // Fetching activity details
        try {
            db.collection("activities")
                .document(activityId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    activity = querySnapshot.toObject(Activity::class.java)!!
                }
                .addOnFailureListener { exception ->
                }
                .await()
        } catch (e: Exception) {
            Log.e("Error in getPlaces", e.message.toString())
        }

    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center
            ) {
                activity?.let { act ->
                    /*val maxPlayers = act["playersCount"].toString().toInt()
                    val minScore = act["minScore"].toString().toInt()
                    val players = act["players"] as? List<String> ?: listOf()

                    Text(text = "Detalji aktivnosti")
                    Spacer(modifier = Modifier.height(16.dp))

                    // Prikaz informacija o aktivnosti
                    Text("Sport: ${act["sport"]}")
                    Text("Datum: ${act["date"]}")
                    Text("Vreme: ${act["time"]}")
                    Text("Minimalni score: $minScore")
                    Text("Broj prijavljenih: ${players.size} / $maxPlayers")
                    players.forEach(){ player ->
                        Text(text = "${player.toString()}")
                    }*/


                    Spacer(modifier = Modifier.height(16.dp))

                    // Ako je korisnik već prijavljen, prikaži dugme za odjavu
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
                        // Provera uslova za prijavu
                        /*if (players.size < maxPlayers && userScore >= minScore) {
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
                        } else {
                            // Prikaz obaveštenja da se ne može prijaviti
                            if (userScore < minScore) {
                                Text("Vaš score je ${userScore}, minimalni score je $minScore. Ne možete se prijaviti.")
                            } else {
                                Text("Aktivnost je popunjena. Ne možete se prijaviti.")
                            }
                        }*/
                    }
                } ?: run {
                    // Prikaz loading teksta dok se podaci učitavaju
                    Text("Učitavanje aktivnosti...")
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
