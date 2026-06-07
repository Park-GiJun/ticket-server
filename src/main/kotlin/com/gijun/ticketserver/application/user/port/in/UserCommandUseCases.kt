package com.gijun.ticketserver.application.user.port.`in`

import com.gijun.ticketserver.application.user.dto.LoginCommand
import com.gijun.ticketserver.application.user.dto.RegisterUserCommand
import com.gijun.ticketserver.application.user.dto.RequestPasswordResetCommand
import com.gijun.ticketserver.application.user.dto.ResetPasswordCommand
import com.gijun.ticketserver.application.user.dto.TokenResult
import com.gijun.ticketserver.application.user.dto.UserResult

/**
 * 사용자 명령(Command) 유스케이스. **1 인터페이스 = 1 함수** 규칙을 따른다.
 */

interface RegisterUserUseCase {
    fun register(command: RegisterUserCommand): UserResult
}

interface LoginUseCase {
    fun login(command: LoginCommand): TokenResult
}

interface RequestPasswordResetUseCase {
    fun requestPasswordReset(command: RequestPasswordResetCommand)
}

interface ResetPasswordUseCase {
    fun resetPassword(command: ResetPasswordCommand)
}
