package com.gijun.ticketserver.infrastructure.adapter.`in`.user.web.dto

import com.gijun.ticketserver.application.user.dto.LoginCommand
import com.gijun.ticketserver.application.user.dto.RegisterUserCommand
import com.gijun.ticketserver.application.user.dto.RequestPasswordResetCommand
import com.gijun.ticketserver.application.user.dto.ResetPasswordCommand
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank
    @field:Email
    val email: String,

    @field:NotBlank
    @field:Size(min = 8, max = 64)
    val password: String,

    @field:NotBlank
    @field:Size(max = 50)
    val name: String,
) {
    fun toCommand(): RegisterUserCommand = RegisterUserCommand(email, password, name)
}

data class LoginRequest(
    @field:NotBlank
    @field:Email
    val email: String,

    @field:NotBlank
    val password: String,
) {
    fun toCommand(): LoginCommand = LoginCommand(email, password)
}

data class PasswordResetRequest(
    @field:NotBlank
    @field:Email
    val email: String,
) {
    fun toCommand(): RequestPasswordResetCommand = RequestPasswordResetCommand(email)
}

data class PasswordResetConfirmRequest(
    @field:NotBlank
    val token: String,

    @field:NotBlank
    @field:Size(min = 8, max = 64)
    val newPassword: String,
) {
    fun toCommand(): ResetPasswordCommand = ResetPasswordCommand(token, newPassword)
}
