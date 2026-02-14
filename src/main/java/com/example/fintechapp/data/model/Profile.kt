package com.example.fintechapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    @SerialName("user_id")
    val userId: String,
    @SerialName("full_name")
    val fullName: String,
    val email: String
)
