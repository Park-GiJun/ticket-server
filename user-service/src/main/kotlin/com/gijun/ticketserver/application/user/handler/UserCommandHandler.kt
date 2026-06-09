package com.gijun.ticketserver.application.user.handler

import com.gijun.ticketserver.application.user.dto.LoginCommand
import com.gijun.ticketserver.application.user.dto.RegisterUserCommand
import com.gijun.ticketserver.application.user.dto.RequestPasswordResetCommand
import com.gijun.ticketserver.application.user.dto.ResetPasswordCommand
import com.gijun.ticketserver.application.user.dto.TokenResult
import com.gijun.ticketserver.application.user.dto.UserResult
import com.gijun.ticketserver.application.user.port.`in`.LoginUseCase
import com.gijun.ticketserver.application.user.port.`in`.RegisterUserUseCase
import com.gijun.ticketserver.application.user.port.`in`.RequestPasswordResetUseCase
import com.gijun.ticketserver.application.user.port.`in`.ResetPasswordUseCase
import com.gijun.ticketserver.application.user.port.out.UserMemoryPort
import com.gijun.ticketserver.application.user.port.out.UserMessagePort
import com.gijun.ticketserver.application.user.port.out.UserPersistencePort
import com.gijun.ticketserver.application.user.port.out.UserTokenPort
import com.gijun.ticketserver.domain.exception.UserException
import com.gijun.ticketserver.domain.service.UserDomainService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.util.UUID

@Service
@Transactional
class UserCommandHandler(
    private val userPersistencePort: UserPersistencePort,
    private val userMemoryPort: UserMemoryPort,
    private val userMessagePort: UserMessagePort,
    private val userTokenPort: UserTokenPort,
    private val passwordEncoder: PasswordEncoder,
    private val userDomainService: UserDomainService = UserDomainService(),
) : RegisterUserUseCase,
    LoginUseCase,
    RequestPasswordResetUseCase,
    ResetPasswordUseCase {

    override fun register(command: RegisterUserCommand): UserResult {
        val email = userDomainService.normalizeEmail(command.email)
        if (userPersistencePort.existsByEmail(email)) {
            throw UserException.EmailAlreadyExists(email)
        }
        userDomainService.validatePasswordPolicy(command.rawPassword)

        val encoded = encodePassword(command.rawPassword)
        val user = userDomainService.createUser(email, encoded, command.name)
        val saved = userPersistencePort.save(user)

        userMessagePort.sendUserRegistered(requireNotNull(saved.id), saved.email)
        return UserResult.from(saved)
    }

    override fun login(command: LoginCommand): TokenResult {
        val email = userDomainService.normalizeEmail(command.email)
        val user = userPersistencePort.findByEmail(email)
            ?: throw UserException.InvalidCredentials()

        if (!passwordEncoder.matches(command.rawPassword, user.encodedPassword)) {
            throw UserException.InvalidCredentials()
        }
        if (!user.isActive) {
            throw UserException.InactiveUser()
        }

        val issued = userTokenPort.issueAccessToken(user)
        return TokenResult(issued.token, issued.tokenType, issued.expiresInSeconds)
    }

    override fun requestPasswordReset(command: RequestPasswordResetCommand) {
        val email = userDomainService.normalizeEmail(command.email)
        // 계정 존재 여부를 노출하지 않기 위해 미존재 시에도 동일하게 정상 응답한다.
        val user = userPersistencePort.findByEmail(email) ?: return

        val token = UUID.randomUUID().toString().replace("-", "")
        userMemoryPort.savePasswordResetToken(token, user.email, RESET_TOKEN_TTL)
        userMessagePort.sendPasswordResetRequested(user.email, token)
    }

    override fun resetPassword(command: ResetPasswordCommand) {
        val email = userMemoryPort.findEmailByPasswordResetToken(command.token)
            ?: throw UserException.InvalidResetToken()
        val user = userPersistencePort.findByEmail(email)
            ?: throw UserException.UserNotFound()

        userDomainService.validatePasswordPolicy(command.newRawPassword)
        val encoded = encodePassword(command.newRawPassword)
        userPersistencePort.save(user.withPassword(encoded))

        userMemoryPort.deletePasswordResetToken(command.token)
    }

    private fun encodePassword(rawPassword: String): String =
        requireNotNull(passwordEncoder.encode(rawPassword)) { "비밀번호 인코딩에 실패했습니다" }

    companion object {
        private val RESET_TOKEN_TTL: Duration = Duration.ofMinutes(30)
    }
}
