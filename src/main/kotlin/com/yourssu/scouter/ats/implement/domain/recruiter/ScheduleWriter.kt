package com.yourssu.scouter.ats.implement.domain.recruiter

import com.yourssu.scouter.ats.implement.support.exception.DuplicateScheduleException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Component

@Component
class ScheduleWriter(
    private val scheduleRepository: ScheduleRepository,
) {
    fun writeAll(schedules: List<Schedule>) {
        try {
            scheduleRepository.saveAll(schedules)
        } catch (_: DuplicateKeyException) {
            // MySQL, PostgreSQL 등에서 정상 변환된 경우
            throw DuplicateScheduleException("이미 해당 시간에 면접이 예정되어 있습니다.")
        } catch (e: DataIntegrityViolationException) {
            val rootCause = e.rootCause

            // H2의 JdbcSQLIntegrityConstraintViolationException을 rootCause에서 체크
            if (rootCause is java.sql.SQLException && rootCause.sqlState == "23505") {
                throw DuplicateScheduleException("이미 해당 시간에 면접이 예정되어 있습니다.")
            }

            // Hibernate의 ConstraintViolationException도 체크
            if (e.cause is org.hibernate.exception.ConstraintViolationException) {
                throw DuplicateScheduleException("이미 해당 시간에 면접이 예정되어 있습니다.")
            }

            throw e
        }
    }

    fun deleteAllByPart(partId: Long): Int {
        return scheduleRepository.deleteAllByPartId(partId)
    }
}