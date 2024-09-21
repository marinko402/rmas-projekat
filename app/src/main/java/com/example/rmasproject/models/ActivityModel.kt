package com.example.rmasproject.models

import androidx.lifecycle.ViewModel

open class ActivityModel :ViewModel()
{

}
data class Activity(
    val createdBy : String = "",
    val date : String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val minScore: Long = 0,
    val players : List<String> = emptyList(),
    val playersCount : Long = 0,
    val sport : String = "",
    val time : String = ""
)