package com.gijun.ticketserver.application.user.port.`in`

import com.gijun.ticketserver.application.user.dto.GetUserQuery
import com.gijun.ticketserver.application.user.dto.UserResult

/**
 * 사용자 조회(Query) 유스케이스. **1 인터페이스 = 1 함수** 규칙을 따른다.
 */

interface GetUserUseCase {
    fun getById(query: GetUserQuery): UserResult
}
