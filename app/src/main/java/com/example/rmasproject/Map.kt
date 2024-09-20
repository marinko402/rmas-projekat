package com.example.rmasproject

import android.app.ActivityManager
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.rmasproject.services.LocationTrackerService
import com.example.rmasproject.utils.DefaultLocationClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun Map(navController: NavController) {
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var showAddActivityButton by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
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
            }

            /*if (showAddActivityButton) {
                Button(
                    onClick = {
                        val lat = selectedLocation?.latitude?.toFloat() ?: 0f
                        val lng = selectedLocation?.longitude?.toFloat() ?: 0f
                        navController.navigate("${Screens.AddActivity.screen}/$lat/$lng")
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Dodaj aktivnost")
                }
            }*/
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
