package com.yourssu.scouter.hrms.business.support

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * dev 프로필 전용. 민감정보 열람 권한 테스트 시 "일시적으로 비권한자로 동작"할 userId를 보관.
 * DB 없이 인메모리로만 유지되며, 서버 재시작 시 초기화됨.
 */
@Profile("local", "dev")
@Component
class DevPrivilegeTestHolder {

    private val forcedNonPrivilegedUserIds = ConcurrentHashMap.newKeySet<Long>()

    fun isMarkedAsNonPrivileged(userId: Long): Boolean =
        forcedNonPrivilegedUserIds.contains(userId)

    fun markAsNonPrivilegedForTest(userId: Long) {
        forcedNonPrivilegedUserIds.add(userId)
    }

    fun unmark(userId: Long) {
        forcedNonPrivilegedUserIds.remove(userId)
    }
}
