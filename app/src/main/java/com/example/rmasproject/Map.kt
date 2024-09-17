package com.example.rmasproject

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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun Map(navController: NavController) {
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var showAddActivityButton by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(43.3209, 21.8958),13f)
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
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
