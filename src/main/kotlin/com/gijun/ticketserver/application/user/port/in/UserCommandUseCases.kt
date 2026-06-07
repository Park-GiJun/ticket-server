package com.gijun.ticketserver.application.user.port.`in`

import com.gijun.ticketserver.application.user.dto.LoginCommand
import com.gijun.ticketserver.application.user.dto.RegisterUserCommand
import com.gijun.ticketserver.application.user.dto.RequestPasswordResetCommand
import com.gijun.ticketserver.application.user.dto.ResetPasswordCommand
import com.gijun.ticketserver.application.user.dto.TokenResult
import com.gijun.ticketserver.application.user.dto.UserResult

interface UserCommandUseCases {
    fun register(command: RegisterUserCommand): UserResult
    fun login(command: LoginCommand): TokenResult
    fun requestPasswordReset(command: RequestPasswordResetCommand)
    fun resetPassword(command: ResetPasswordCommand)
}
