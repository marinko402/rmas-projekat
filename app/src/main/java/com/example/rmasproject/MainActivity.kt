package com.example.rmasproject

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.rmasproject.services.LocationTrackerService
import com.example.rmasproject.ui.theme.RMASProjectTheme
import com.google.maps.android.compose.GoogleMap

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RMASProjectTheme {
                Scaffold(modifier = Modifier.fillMaxSize(),
                bottomBar = {

                }) {
                    NavBar()
                    if(isUserLoggedIn()){
                            if (!areLocationPermissionsGranted()) {
                                requestLocationPermissions()
                                if (areLocationPermissionsGranted()) {
                                    startLocationService()
                                }
                            } else {
                                startLocationService()
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (ActivityCompat.checkSelfPermission(
                                        applicationContext,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) != PackageManager.PERMISSION_GRANTED) {
                                    requestPermissions(
                                        this,
                                        arrayOf(
                                            Manifest.permission.POST_NOTIFICATIONS,
                                        ),
                                        0
                                    )
                                }
                            }
                        }else{
                            stopLocationService()
                            //stopNearbyCheckService()
                        }
                }
            }
        }
    }
    private fun areLocationPermissionsGranted(): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocationGranted && coarseLocationGranted
    }

    private fun requestLocationPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissions(this, permissionsToRequest.toTypedArray(), 0)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    internal fun startLocationService() {
        Intent(applicationContext, LocationTrackerService::class.java).apply {
            action = LocationTrackerService.ACTION_START
            startService(this)
        }
    }

    internal fun stopLocationService() {
        //stopNearbyCheckService()
        Intent(applicationContext, LocationTrackerService::class.java).apply {
            action = LocationTrackerService.ACTION_STOP
            startService(this)
        }
    }
    /*@RequiresApi(Build.VERSION_CODES.O)
    internal fun startNearbyCheckService() {
        Intent(applicationContext, NearbyCheckService::class.java).apply {
            action = NearbyCheckService.ACTION_START
            startService(this)
        }
    }

    internal fun stopNearbyCheckService() {
        Intent(applicationContext, NearbyCheckService::class.java).apply {
            action = NearbyCheckService.ACTION_STOP
            startService(this)
        }
    }*/

}
@Composable
fun NavBar(){
    val navController = rememberNavController()
    val context = LocalContext.current.applicationContext
    val selected = remember{
        mutableStateOf(Icons.Default.Home)
    }
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route
    val showBottomBar = currentDestination != Screens.Login.screen && currentDestination != Screens.Register.screen




    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomAppBar(containerColor = Color.Green) {
                    IconButton(
                        onClick = {
                            selected.value = Icons.Default.Home
                            navController.navigate(Screens.Home.screen) {
                                popUpTo(0)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.size((26.dp)),
                            tint = if (selected.value == Icons.Default.Home) Color.White else Color.Black
                        )
                    }
                    IconButton(
                        onClick = {
                            selected.value = Icons.Default.Place
                            navController.navigate(Screens.Map.screen) {
                                popUpTo(0)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            modifier = Modifier.size((26.dp)),
                            tint = if (selected.value == Icons.Default.Place) Color.White else Color.Black
                        )
                    }
                    IconButton(
                        onClick = {
                            selected.value = Icons.Default.SportsBasketball
                            navController.navigate(Screens.Activities.screen) {
                                popUpTo(0)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.SportsBasketball,
                            contentDescription = null,
                            modifier = Modifier.size((26.dp)),
                            tint = if (selected.value == Icons.Default.SportsBasketball) Color.White else Color.Black
                        )
                    }
                    IconButton(
                        onClick = {
                            selected.value = Icons.Default.Groups
                            navController.navigate(Screens.BestPlayers.screen) {
                                popUpTo(0)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Groups,
                            contentDescription = null,
                            modifier = Modifier.size((26.dp)),
                            tint = if (selected.value == Icons.Default.Groups) Color.White else Color.Black
                        )
                    }
                    IconButton(
                        onClick = {
                            selected.value = Icons.Default.AccountCircle
                            navController.navigate(Screens.Profile.screen) {
                                popUpTo(0)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size((26.dp)),
                            tint = if (selected.value == Icons.Default.AccountCircle) Color.White else Color.Black
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(navController = navController,
                startDestination = if(isUserLoggedIn())
                                    Screens.Home.screen
                                    else
                                    Screens.Login.screen,
                modifier = Modifier.padding(paddingValues)) {
            composable(Screens.Home.screen) { Home() }
            composable(Screens.Profile.screen) { Profile(navController) }
            composable(Screens.Map.screen) { Map(navController) }
            composable(Screens.BestPlayers.screen) { BestPlayers() }
            composable(Screens.Login.screen) { Login(navController) }
            composable(Screens.Register.screen) { Register(navController) }
            composable(Screens.Activities.screen) { Activities(navController) }
            composable(route = "${Screens.AddActivity.screen}/{lat}/{lng}",
                arguments = listOf(
                    navArgument("lat") { type = NavType.FloatType },
                    navArgument("lng") { type = NavType.FloatType }

                )) { backStackEntry ->
                val lat = backStackEntry.arguments?.getFloat("lat")
                val lng = backStackEntry.arguments?.getFloat("lng")
                AddActivity(navController, lat!!.toDouble(), lng!!.toDouble())
            }
            composable(route = "${Screens.ActivityDetails.screen}/{activityId}",
                arguments = listOf(
                    navArgument("activityId"){type=NavType.StringType}
                )) { backStackEntry ->
                val activityId = backStackEntry.arguments?.getString("activityId")
                if(activityId != null && activityId.isNotEmpty())
                    ActivityDetails(navController, activityId.toString())
                else
                    Text("Nije moguće prikazati detalje o aktivnosti.")
            }
        }
    }
}

fun isUserLoggedIn(): Boolean {
    return auth.currentUser != null
}
