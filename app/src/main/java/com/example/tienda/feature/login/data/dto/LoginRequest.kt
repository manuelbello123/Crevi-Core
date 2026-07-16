package com.example.tienda.feature.login.data.dto

import kotlinx.serialization.Serializable

/** Body de POST /auth/login → {"usuario","password"}. */
@Serializable
data class LoginRequest(
    val usuario: String,
    val password: String,
)
