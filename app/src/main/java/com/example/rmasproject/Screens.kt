package com.example.rmasproject

sealed class Screens (val screen:String){
    data object Home:Screens("home")
    data object Profile:Screens("profile")
    data object Map:Screens("map")
    data object Login:Screens("login")
    data object Register:Screens("register")
    data object BestPlayers:Screens("bestplayers")
}