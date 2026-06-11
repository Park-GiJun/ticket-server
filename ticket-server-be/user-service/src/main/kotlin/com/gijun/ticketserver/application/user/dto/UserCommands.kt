package com.gijun.ticketserver.application.user.dto

data class RegisterUserCommand(
    val email: String,
    val rawPassword: String,
    val name: String,
)

data class LoginCommand(
    val email: String,
    val rawPassword: String,
)

data class RequestPasswordResetCommand(
    val email: String,
)

data class ResetPasswordCommand(
    val token: String,
    val newRawPassword: String,
)
