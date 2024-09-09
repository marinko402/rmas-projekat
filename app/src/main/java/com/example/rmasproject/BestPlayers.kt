package com.example.rmasproject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun BestPlayers() {
    var players by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    getBestPlayers { result ->
        players = result
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(text = "Best Players")

                Spacer(modifier = Modifier.height(16.dp))

                // Tabela sa rangiranim igračima
                if (players.isNotEmpty()) {
                    BestPlayersTable(players)
                } else {
                    Text(text = "Nema dostupnih igrača.")
                }
            }
        }
    )
}

@Composable
fun BestPlayersTable(players: List<Map<String, Any>>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Rang", modifier = Modifier.weight(1f))
            Text(text = "Ime", modifier = Modifier.weight(2f))
            Text(text = "Score", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Lista igrača
        players.forEachIndexed { index, player ->
            val name = player["name"] as? String ?: "Nepoznato"
            val score = player["score"] as? Long ?: 0

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = (index + 1).toString(), modifier = Modifier.weight(1f))
                Text(text = name, modifier = Modifier.weight(2f))
                Text(text = score.toString(), modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}


fun getBestPlayers(onResult: (List<Map<String, Any>>) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    db.collection("users")
        .orderBy("score", com.google.firebase.firestore.Query.Direction.DESCENDING)
        .get()
        .addOnSuccessListener { result ->
            val players = result.documents.map { document ->
                document.data ?: emptyMap<String, Any>()
            }
            onResult(players)
        }
        .addOnFailureListener {
            onResult(emptyList())
        }
}