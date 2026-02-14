package com.example.fintechapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionDto(
    @SerialName("user_id")
    val userId: String,
    val type: String,
    val amount: Double,
    @SerialName("created_at")
    val createdAt: String? = null // Handled by server
)
