package com.example.rmasproject.models

import androidx.lifecycle.ViewModel

open class ActivityModel :ViewModel()
{

}
data class Activity(
    val createdBy : String = "",
    val date : String = "",
    val lat : Number = 0,
    val lng : Number = 0,
    val minScore : Number = 0,
    val players : List<String> = emptyList(),
    val playerCount : Number = 0,
    val sport : String = "",
    val time : String = ""
)