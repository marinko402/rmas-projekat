package com.example.rmasproject

import android.provider.ContactsContract.Profile
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Profile(){
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier= Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            Text(text = "Profile")
        }
    }
}