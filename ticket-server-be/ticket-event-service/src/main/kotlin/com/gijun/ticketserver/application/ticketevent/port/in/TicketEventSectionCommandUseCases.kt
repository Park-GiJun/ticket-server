package com.gijun.ticketserver.application.ticketevent.port.`in`

import com.gijun.ticketserver.application.ticketevent.dto.CreateSectionsCommand
import com.gijun.ticketserver.application.ticketevent.dto.SectionCreationResult

/**
 * 구역(Section) 명령 유스케이스. **1 인터페이스 = 1 함수** 규칙을 따른다.
 */

/** 구역 생성(셋업 2단계): 이벤트를 EVENT_CREATED → SECTION_CREATED 로 진행. */
interface CreateSectionsUseCase {
    fun createSections(command: CreateSectionsCommand): SectionCreationResult
}
