package com.example.rmasproject.models

import androidx.lifecycle.ViewModel

open class UserModel : ViewModel()
{

}
data class User(
    val name : String = "",
    val surname : String = "",
    val username : String = "",
    val phone : String = "",
    val profileImageUrl : String = "",
    val email : String = "",
    val score : Number = 0
)