package com.example.rmasproject

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.rmasproject.models.Activity
import com.example.rmasproject.models.ActivityGet
import com.example.rmasproject.services.LocationTrackerService
import com.example.rmasproject.utils.DefaultLocationClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import java.util.Date
import java.util.Locale

@Composable
fun Map(navController: NavController) {
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var showAddActivityButton by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var activities by remember { mutableStateOf(emptyList<ActivityGet>()) }

    LaunchedEffect(Unit) {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

        FirebaseFirestore.getInstance().collection("activities")
            .get()
            .addOnSuccessListener { snapshot ->
                val fetchedActivities = snapshot.documents.mapNotNull { document ->
                    val activity = document.toObject(ActivityGet::class.java)
                    activity?.let {
                        // Spoji date i time iz baze
                        val activityDateTime = "${it.date} ${it.time}"
                        val activityDate = dateFormat.parse(activityDateTime)

                        if (activityDate != null && activityDate.after(currentDate)) {
                            it.id = document.id  // Postavi ID iz dokumenta
                            it // vrati aktivnost samo ako nije počela
                        } else {
                            null // vrati null ako je aktivnost već počela
                        }
                    }
                }
                activities = fetchedActivities
            }
    }

    var currentLocation by remember {
        mutableStateOf<LatLng?>(null)
    }
    val isLocationServiceRunning by remember {
        mutableStateOf(
            isServiceRunning(context, LocationTrackerService::class.java)
        )
    }

    val defaultLocation = LatLng(43.321445, 21.896104)
    val cameraPositionState = rememberCameraPositionState()

    var userLocation by remember {
        mutableStateOf(LatLng(0.0, 0.0))
    }
    if (isLocationServiceRunning) {
        val locationClient = remember {
            DefaultLocationClient(context, LocationServices.getFusedLocationProviderClient(context))
        }

        LaunchedEffect(Unit) {
            locationClient.getLocationUpdates(10000L).collect { location ->
                    currentLocation = LatLng(location.latitude, location.longitude)
            }
        }
    }

    LaunchedEffect(currentLocation) {
        userLocation = currentLocation ?: defaultLocation
        cameraPositionState.position = CameraPosition.fromLatLngZoom(userLocation, 16f)
    }
    val properties by remember {
        mutableStateOf(
            MapProperties(
            isMyLocationEnabled = isLocationServiceRunning
        )
        )
    }


    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = properties,
                uiSettings = MapUiSettings(),
                onMapLongClick = { latLng ->
                    selectedLocation = latLng
                    showAddActivityButton = true
                    val lat = selectedLocation?.latitude?.toFloat() ?: 0f
                    val lng = selectedLocation?.longitude?.toFloat() ?: 0f
                    navController.navigate("${Screens.AddActivity.screen}/$lat/$lng")
                }
            ) {
                selectedLocation?.let {
                    Marker(
                        state = rememberMarkerState(position = it),
                        title = "Odabrana lokacija",
                        snippet = "Lokacija za novu aktivnost"
                    )
                }
                activities.forEach { activity ->
                    val position = LatLng(activity.lat.toDouble(), activity.lng.toDouble())
                    var markerColor = getMarkerIcon(activity.sport, context)


                    Marker(
                        state = rememberMarkerState(position = position),
                        title = activity.sport,
                        snippet = "Kliknite za detalje",
                        icon = BitmapDescriptorFactory.fromBitmap(markerColor),
                        onClick = {
                            // Navigate to ActivityDetails with activity ID
                            navController.navigate("${Screens.ActivityDetails.screen}/${activity.id}")
                            true
                        }
                    )
                }
                selectedLocation?.let {
                    Marker(
                        state = rememberMarkerState(position = it),
                        title = "Odabrana lokacija",
                        snippet = "Lokacija za novu aktivnost"
                    )
                }
            }
            Button(
                onClick = {
                    val lat = currentLocation?.latitude?.toFloat() ?: 0f
                    val lng = currentLocation?.longitude?.toFloat() ?: 0f
                    navController.navigate("${Screens.AddActivity.screen}/$lat/$lng")
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text("Dodaj aktivnost na svojoj lokaciji")
            }
        }
    }
}
fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Int.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}
fun getMarkerIcon(sport: String, context: Context): Bitmap {
    val iconRes = when (sport) {
        "Fudbal" -> BitmapFactory.decodeResource(context.resources,R.drawable.fudbal_marker)  // Custom icon for football
        "Košarka" -> BitmapFactory.decodeResource(context.resources,R.drawable.kosarka_marker)  // Custom icon for basketball
        "Tenis" -> BitmapFactory.decodeResource(context.resources,R.drawable.tenis_marker)  // Custom icon for tennis
        "Odbojka" -> BitmapFactory.decodeResource(context.resources,R.drawable.odbojka_marker)  // Custom icon for volleyball
        else -> BitmapFactory.decodeResource(context.resources,R.drawable.default_marker)  // Default icon if sport is not recognized
    }

    return Bitmap.createScaledBitmap(iconRes,200,200,false)
}
