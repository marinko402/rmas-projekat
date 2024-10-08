package com.example.rmasproject

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.rmasproject.ui.theme.primary
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await

@Composable
fun Activities(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var activities by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var selectedSport by remember { mutableStateOf("Svi sportovi") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    val sports = listOf("Svi sportovi", "Fudbal", "Košarka", "Tenis", "Odbojka")

    LaunchedEffect(selectedSport) {
        val result = db.collection("activities").get().await()
        val fetchedActivities = result.documents.map { document ->
            val data = document.data ?: emptyMap<String, Any>()
            data + ("id" to document.id)
        }

        val now = Date()
        val filteredActivities = fetchedActivities.filter { act ->
            val dateStr = act["date"] as? String
            val timeStr = act["time"] as? String
            val dateTimeStr = "$dateStr $timeStr"
            val activityDate = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).parse(dateTimeStr)

            activityDate?.after(now) == true &&
                    (selectedSport == "Svi sportovi" || act["sport"] == selectedSport)
        }.sortedBy { act ->
            val dateStr = act["date"] as? String
            val timeStr = act["time"] as? String
            val dateTimeStr = "$dateStr $timeStr"
            SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).parse(dateTimeStr)?.time
        }

        activities = filteredActivities
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { innerPadding ->
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
                ) {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = primary),
                        onClick = { dropdownExpanded = !dropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = selectedSport)
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        sports.forEach { sport ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedSport = sport
                                    dropdownExpanded = false
                                },
                                text = { Text(text = sport) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(activities) { activity ->
                            val activityId = activity["id"] as? String
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                //Text(text = "ID: $activityId")
                                Text(text = "Sport: ${activity["sport"]}")
                                Text(text = "Datum: ${activity["date"]}")
                                Text(text = "Vreme: ${activity["time"]}")
                                Text(text = "Minimalni score: ${activity["minScore"]}")
                                Text(text = "Max igraca: ${activity["playersCount"]}")
                                Text(text = "Broj prijavljenih: ${activity["players"]?.let { (it as List<*>).size } ?: 0}")

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    colors = ButtonDefaults.buttonColors(containerColor = primary),
                                    onClick = {
                                        navController.navigate("${Screens.ActivityDetails.screen}/$activityId")
                                    }
                                ) {
                                    Text(text = "Detalji")
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    )
}



