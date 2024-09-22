package com.example.rmasproject

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@Composable
fun AddActivity(navController: NavController, lat: Double, lng: Double) {
    var playersCount by remember { mutableStateOf("") }
    var minScore by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var selectedSport by remember { mutableStateOf("") }
    var showSportDialog by remember { mutableStateOf(false) }

    val sports = listOf("Fudbal", "Košarka", "Tenis", "Odbojka")
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Dodaj novu aktivnost")

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = playersCount,
                    onValueChange = { playersCount = it },
                    label = { Text("Broj igrača") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = minScore,
                    onValueChange = { minScore = it },
                    label = { Text("Minimalni score") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Date Picker TextField (clickable on the whole text field)
                OutlinedButton(
                    onClick = {
                        val datePicker = DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                date = "$day.${month + 1}.$year"
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                        datePicker.show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (date.isEmpty()) "Izaberi datum" else date)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.DateRange, contentDescription = "Izaberi datum")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Time Picker TextField (clickable on the whole text field)
                OutlinedButton(
                    onClick = {
                        val timePicker = TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                time = String.format("%02d:%02d", hour, minute)
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        )
                        timePicker.show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (time.isEmpty()) "Izaberi vreme" else time)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Schedule, contentDescription = "Izaberi vreme")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sport Picker TextField (clickable on the whole text field)
                OutlinedButton(
                    onClick = {
                        showSportDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (selectedSport.isEmpty()) "Izaberi sport" else selectedSport)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Izaberi sport")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        increaseUserScore()
                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                        val activity = hashMapOf(
                            "sport" to selectedSport,
                            "playersCount" to playersCount.toInt(),
                            "minScore" to minScore.toInt(),
                            "date" to date,
                            "time" to time,
                            "lat" to lat,
                            "lng" to lng,
                            "createdBy" to userId,
                            "players" to listOf(userId)
                        )
                        FirebaseFirestore.getInstance().collection("activities").add(activity)
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Dodaj aktivnost")
                }

                // Sport Picker Dialog
                if (showSportDialog) {
                    AlertDialog(
                        onDismissRequest = { showSportDialog = false },
                        confirmButton = {
                            TextButton(onClick = { showSportDialog = false }) {
                                Text("Zatvori")
                            }
                        },
                        title = { Text("Izaberite sport") },
                        text = {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                sports.forEach { sport ->
                                    Button(
                                        onClick = {
                                            selectedSport = sport
                                            showSportDialog = false
                                        },
                                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                                    ) {
                                        Text(sport)
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    )
}
