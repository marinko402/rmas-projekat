package com.example.rmasproject

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.maps.android.compose.GoogleMap

@Composable
fun Map(){
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        GoogleMap(modifier = Modifier.fillMaxSize().padding(innerPadding))
    }
}