package com.gijun.ticketserver.infrastructure.adapter.`in`.user.web

import com.gijun.ticketserver.application.user.dto.GetUserQuery
import com.gijun.ticketserver.application.user.port.`in`.GetUserUseCase
import com.gijun.ticketserver.application.user.port.`in`.LoginUseCase
import com.gijun.ticketserver.application.user.port.`in`.RegisterUserUseCase
import com.gijun.ticketserver.application.user.port.`in`.RequestPasswordResetUseCase
import com.gijun.ticketserver.application.user.port.`in`.ResetPasswordUseCase
import com.gijun.ticketserver.infrastructure.adapter.`in`.user.web.dto.LoginRequest
import com.gijun.ticketserver.infrastructure.adapter.`in`.user.web.dto.PasswordResetConfirmRequest
import com.gijun.ticketserver.infrastructure.adapter.`in`.user.web.dto.PasswordResetRequest
import com.gijun.ticketserver.infrastructure.adapter.`in`.user.web.dto.RegisterRequest
import com.gijun.ticketserver.infrastructure.adapter.`in`.user.web.dto.TokenResponse
import com.gijun.ticketserver.infrastructure.adapter.`in`.user.web.dto.UserResponse
import com.gijun.ticketserver.infrastructure.config.OpenApiConfig
import com.gijun.ticketserver.infrastructure.config.security.AuthenticatedUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "User", description = "사용자 인증 API (회원가입 / 로그인 / 비밀번호 재설정)")
@RestController
@RequestMapping("/api")
class UserWebAdapter(
    private val registerUserUseCase: RegisterUserUseCase,
    private val loginUseCase: LoginUseCase,
    private val requestPasswordResetUseCase: RequestPasswordResetUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val getUserUseCase: GetUserUseCase,
) {

    @Operation(summary = "회원가입", description = "이메일/비밀번호/이름으로 사용자를 등록한다.")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "가입 성공"),
        ApiResponse(responseCode = "400", description = "요청 값 검증 실패 / 비밀번호 정책 위반"),
        ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일"),
    )
    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody request: RegisterRequest): UserResponse =
        UserResponse.from(registerUserUseCase.register(request.toCommand()))

    @Operation(summary = "로그인", description = "인증 후 JWT 액세스 토큰을 발급한다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "로그인 성공, 토큰 반환"),
        ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치"),
        ApiResponse(responseCode = "403", description = "비활성 계정"),
    )
    @PostMapping("/auth/login")
    fun login(@Valid @RequestBody request: LoginRequest): TokenResponse =
        TokenResponse.from(loginUseCase.login(request.toCommand()))

    @Operation(
        summary = "비밀번호 재설정 요청",
        description = "재설정 토큰을 생성해 메일 발송 채널로 전달한다. 계정 존재 여부와 무관하게 202 를 반환한다.",
    )
    @ApiResponse(responseCode = "202", description = "요청 접수")
    @PostMapping("/auth/password-reset/request")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun requestPasswordReset(@Valid @RequestBody request: PasswordResetRequest) {
        requestPasswordResetUseCase.requestPasswordReset(request.toCommand())
    }

    @Operation(summary = "비밀번호 재설정 확정", description = "재설정 토큰과 새 비밀번호로 비밀번호를 변경한다.")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "변경 성공"),
        ApiResponse(responseCode = "400", description = "토큰 무효/만료 또는 비밀번호 정책 위반"),
    )
    @PostMapping("/auth/password-reset/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun confirmPasswordReset(@Valid @RequestBody request: PasswordResetConfirmRequest) {
        resetPasswordUseCase.resetPassword(request.toCommand())
    }

    @Operation(summary = "내 정보 조회", description = "인증된 사용자의 정보를 반환한다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "401", description = "인증 필요 / 토큰 무효"),
    )
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    @GetMapping("/users/me")
    fun me(@AuthenticationPrincipal principal: AuthenticatedUser): UserResponse =
        UserResponse.from(getUserUseCase.getById(GetUserQuery(principal.userId)))
}
