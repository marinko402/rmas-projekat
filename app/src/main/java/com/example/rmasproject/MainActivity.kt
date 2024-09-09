package com.example.rmasproject

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.Stars
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rmasproject.ui.theme.RMASProjectTheme
import com.google.maps.android.compose.GoogleMap
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RMASProjectTheme {
                Scaffold(modifier = Modifier.fillMaxSize(),
                bottomBar = {

                }) { innerPadding ->
                    GoogleMap(modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding))
                    NavBar()
                }
            }
        }
    }
}
@Composable
fun NavBar(){
    val navController = rememberNavController()
    val context = LocalContext.current.applicationContext
    val selected = remember{
        mutableStateOf(Icons.Default.Home)
    }

    Scaffold(
        bottomBar = {
            BottomAppBar(containerColor = Color.Green){
                IconButton(
                    onClick = { selected.value = Icons.Default.Home
                              navController.navigate(Screens.Home.screen){
                                  popUpTo(0)
                              }
                              },
                    modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size((26.dp)),
                        tint = if(selected.value == Icons.Default.Home) Color.White else Color . Black)
                }
                IconButton(
                    onClick = { selected.value = Icons.Default.Place
                        navController.navigate(Screens.Map.screen){
                            popUpTo(0)
                        }
                    },
                    modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size((26.dp)),
                        tint = if(selected.value == Icons.Default.Place) Color.White else Color . Black)
                }
                IconButton(
                    onClick = { selected.value = Icons.Default.SportsBasketball
                        navController.navigate(Screens.Activities.screen){
                            popUpTo(0)
                        }
                    },
                    modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.SportsBasketball, contentDescription = null, modifier = Modifier.size((26.dp)),
                        tint = if(selected.value == Icons.Default.SportsBasketball) Color.White else Color . Black)
                }
                IconButton(
                    onClick = { selected.value = Icons.Default.Groups
                        navController.navigate(Screens.BestPlayers.screen){
                            popUpTo(0)
                        }
                    },
                    modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size((26.dp)),
                        tint = if(selected.value == Icons.Default.Groups) Color.White else Color . Black)
                }
                IconButton(
                    onClick = {
                        selected.value = Icons.Default.AccountCircle
                        if (isUserLoggedIn()) {
                            // Ako je korisnik ulogovan, navigiraj na profil
                            navController.navigate(Screens.Profile.screen) {
                                popUpTo(0)
                            }
                        } else {
                            // Ako nije ulogovan, navigiraj na login
                            navController.navigate(Screens.Login.screen) {
                                popUpTo(0)
                            }
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
    ) { paddingValues ->
        NavHost(navController = navController,
                startDestination = Screens.Home.screen,
                modifier = Modifier.padding(paddingValues)){
            composable(Screens.Home.screen){Home()}
            composable(Screens.Profile.screen){Profile(navController)}
            composable(Screens.Map.screen){Map()}
            composable(Screens.BestPlayers.screen){BestPlayers()}
            composable(Screens.Login.screen) { Login(navController) }
            composable(Screens.Register.screen) { Register(navController) }
            composable(Screens.Activities.screen){ Activities() }
        }

    }
}

//val auth = FirebaseAuth.getInstance()
fun isUserLoggedIn(): Boolean {
    return auth.currentUser != null
}