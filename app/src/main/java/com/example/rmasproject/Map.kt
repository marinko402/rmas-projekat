package com.example.rmasproject

import android.app.ActivityManager
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.rmasproject.models.Activity
import com.example.rmasproject.models.ActivityGet
import com.example.rmasproject.services.LocationTrackerService
import com.example.rmasproject.ui.theme.primary
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun Map(navController: NavController) {
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var showAddActivityButton by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var activities by remember { mutableStateOf(emptyList<ActivityGet>()) }
    val sports = listOf("Svi sportovi", "Fudbal", "Košarka", "Tenis", "Odbojka")
    val calendar = Calendar.getInstance()
    var startdate by remember { mutableStateOf("") }
    var enddate by remember { mutableStateOf("") }

    fun aktivnosti(
        sport: String? = null
    )
    {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

        FirebaseFirestore.getInstance().collection("activities")
            .get()
            .addOnSuccessListener { snapshot ->
                var fetchedActivities = snapshot.documents.mapNotNull { document ->
                    val activity = document.toObject(ActivityGet::class.java)
                    activity?.let {
                        val activityDateTime = "${it.date} ${it.time}"
                        val activityDate = dateFormat.parse(activityDateTime)

                        if (activityDate != null && activityDate.after(currentDate)) {
                            it.id = document.id
                            it
                        } else {
                            null
                        }
                    }
                }
                if (!sport.isNullOrEmpty()) {
                    fetchedActivities.filter { it.sport == sport }
                }
                activities = fetchedActivities
            }
    }

    LaunchedEffect(Unit) {
        aktivnosti()
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

    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedSport by remember { mutableStateOf("") }
    var selectedCreator by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var minScore by remember { mutableStateOf(0) }
    var maxDistance by remember { mutableStateOf(0f) }
    var showOnlyRegistered by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }



    fun applyFilters(
        sport: String, creator: String, date: String, minScore: Int, maxDistance: Float, onlyRegistered: Boolean
    ) {
        FirebaseFirestore.getInstance().collection("activities")
            .get()
            .addOnSuccessListener { snapshot ->
                var acts = snapshot.documents.mapNotNull { document ->
                    document.toObject(ActivityGet::class.java)?.apply {
                        id = document.id
                    }
                }

                if (sport.isNotEmpty()) {
                    acts = acts.filter { it.sport == sport }
                }
                if (creator.isNotEmpty()) {
                    acts = acts.filter { it.createdBy == creator }
                }
                if (minScore > 0) {
                    acts = acts.filter { it.minScore <= minScore.toLong() }
                }
                if (onlyRegistered) {
                    val currentUser = FirebaseAuth.getInstance().currentUser?.uid
                    if (currentUser != null) {
                        acts = acts.filter { it.players.contains(currentUser) }
                    }
                }

                if (date.isNotEmpty()) {
                    acts = acts.filter { it.date == date }
                }
                if (maxDistance > 0 && currentLocation != null) {
                    acts = acts.filter { activity ->
                        val activityLocation = LatLng(activity.lat.toDouble(), activity.lng.toDouble())
                        val distance = calculateDistance(currentLocation, activityLocation)
                        distance <= maxDistance
                    }
                }
                val currentDate = Date()
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())  // Pretpostavimo da su datumi u ovom formatu

                acts = acts.filter {
                    val activityDate = dateFormat.parse(it.date)  // Pretvori String u Date
                    activityDate != null && activityDate.after(currentDate)  // Zadrži aktivnosti koje su nakon trenutnog datuma
                }
                activities = acts
            }
            .addOnFailureListener { exception ->
            }
    }



    fun resetFilters() {
        selectedSport = ""
        selectedCreator = ""
        selectedDate = ""
        minScore = 0
        maxDistance = 0f
        showOnlyRegistered = false
        applyFilters("", "", "", 0, 0f, false)
    }

    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Filter Activities") },
            text = {
                Column {
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

                    OutlinedTextField(
                        value = selectedCreator,
                        onValueChange = { selectedCreator = it },
                        label = { Text("Activity Creator") }
                    )

                    OutlinedButton(
                        onClick = {
                            val datePicker = DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    startdate = "$day.${month + 1}.$year"
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            )
                            datePicker.show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (startdate.isEmpty()) "Izaberi datum" else startdate)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.DateRange, contentDescription = "Izaberi datum")
                    }

                    OutlinedTextField(
                        value = minScore.toString(),
                        onValueChange = { minScore = it.toInt()},
                        label = { Text("Min Score") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = maxDistance.toString(),
                        onValueChange = { maxDistance = it.toFloat()},
                        label = { Text("Max Distance (km)") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )

                    Row {
                        Checkbox(
                            checked = showOnlyRegistered,
                            onCheckedChange = { showOnlyRegistered = it }
                        )
                        Text("Show only activities I'm registered for")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    applyFilters(
                    sport = selectedSport,
                    creator = selectedCreator,
                    date = startdate,
                    minScore = minScore,
                    maxDistance = maxDistance,
                    onlyRegistered = showOnlyRegistered
                )
                        showFilterDialog = false
            }) {
            Text("Apply Filters")
        }
            },
            dismissButton = {
                Button(onClick = {
                    resetFilters()
                    showFilterDialog = false
                }) {
                    Text("Reset Filters")
                }
            }
        )
    }
    LaunchedEffect(activities){

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
                    Marker(
                        state = rememberMarkerState(position = position),
                        title = activity.sport,
                        snippet = "Kliknite za detalje",
                        icon = BitmapDescriptorFactory.fromBitmap(getMarkerIcon(activity.sport, context)),
                        onClick = {
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
                colors = ButtonDefaults.buttonColors(containerColor = primary),
                onClick = { showFilterDialog = true },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Text("Filter")
            }
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = primary),
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
        "Fudbal" -> BitmapFactory.decodeResource(context.resources,R.drawable.fudbal_marker)
        "Košarka" -> BitmapFactory.decodeResource(context.resources,R.drawable.kosarka_marker)
        "Tenis" -> BitmapFactory.decodeResource(context.resources,R.drawable.tenis_marker)
        "Odbojka" -> BitmapFactory.decodeResource(context.resources,R.drawable.odbojka_marker)
        else -> BitmapFactory.decodeResource(context.resources,R.drawable.default_marker)
    }

    return Bitmap.createScaledBitmap(iconRes,200,200,false)
}



fun calculateDistance(location1: LatLng?, location2: LatLng): Float {
    if (location1 == null) return Float.MAX_VALUE
    val results = FloatArray(1)
    android.location.Location.distanceBetween(
        location1.latitude, location1.longitude, location2.latitude, location2.longitude, results
    )
    return results[0] / 1000
}

