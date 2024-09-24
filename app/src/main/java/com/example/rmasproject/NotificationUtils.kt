package com.example.rmasproject.notifications

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

// Funkcija za slanje notifikacija ostalim korisnicima
fun notifyOtherPlayers(context: Context, activityId: String, userId: String, db: FirebaseFirestore) {
    // Prvo dohvati sve korisnike koji su već registrovani za aktivnost (osim trenutnog korisnika)
    db.collection("activities").document(activityId).get().addOnSuccessListener { document ->
        val players = document.get("players") as? List<String> ?: emptyList()
        val otherPlayers = players.filter { it != userId }

        // Dohvati FCM tokene ostalih igrača
        db.collection("users").whereIn("userId", otherPlayers).get()
            .addOnSuccessListener { documents ->
                val tokens = documents.mapNotNull { it.getString("fcmToken") }

                // Pošalji notifikaciju svakom igraču
                tokens.forEach { token ->
                    sendNotification(context, token, "Novi igrač", "Novi igrač se prijavio na aktivnost!")
                }
            }
    }
}

// Funkcija za slanje jedne notifikacije pomoću FCM REST API-ja
fun sendNotification(context: Context, token: String, title: String, message: String) {
    val url = "https://fcm.googleapis.com/fcm/send"
    val json = """
        {
            "to": "$token",
            "notification": {
                "title": "$title",
                "body": "$message",
                "click_action": "FLUTTER_NOTIFICATION_CLICK"
            }
        }
    """.trimIndent()

    // Posalji HTTP POST zahtev pomoću Volleya ili drugog mrežnog biblioteka
    val request = object : StringRequest(Request.Method.POST, url, {
        // Uspešno poslata notifikacija
    }, {
        // Greška prilikom slanja
    }) {
        override fun getHeaders(): MutableMap<String, String> {
            return mutableMapOf(
                "Authorization" to "key=YOUR_SERVER_KEY",  // Zameni sa tvojim server ključem iz Firebase-a
                "Content-Type" to "application/json"
            )
        }

        override fun getBody(): ByteArray {
            return json.toByteArray(Charsets.UTF_8)
        }
    }

    // Dodaj zahtev u request queue
    Volley.newRequestQueue(context).add(request)
}
