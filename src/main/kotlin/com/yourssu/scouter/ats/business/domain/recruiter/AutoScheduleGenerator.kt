package com.yourssu.scouter.ats.business.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.ats.implement.domain.recruiter.ScheduleDuplicateKey
import com.yourssu.scouter.ats.implement.support.exception.InvalidScheduleException
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

open class AutoScheduleGenerator {

    /**
     * 지원자 리스트에 대해 면접 시간을 자동으로 배정합니다.
     * 백트래킹 알고리즘을 사용하여 모든 가능한 배정 조합을 탐색합니다.
     * 같은 파트의 같은 시간에는 중복 배정하지 않습니다.
     *
     * @param applicants 면접 시간을 배정할 지원자 리스트
     * @return 생성된 면접 스케줄 리스트
     * @throws InvalidScheduleException 모든 조합을 시도해도 배정이 불가능한 경우
     */
    @Transactional(readOnly = true)
    open fun generateSchedules(applicants: List<Applicant>): List<AutoScheduleDto> {
        if (applicants.isEmpty()) {
            return emptyList()
        }


        val sortedApplicants = applicants.sortedBy { it.availableTimes.size }

        val schedules = mutableListOf<AutoScheduleDto>()
        val assignedSlots = mutableSetOf<ScheduleDuplicateKey>()

        val success = backtrack(
            applicants = sortedApplicants,
            currentIndex = 0,
            schedules = schedules,
            assignedSlots = assignedSlots
        )

        if (!success) {
            throw InvalidScheduleException("모든 지원자에게 면접 시간을 배정할 수 없습니다.")
        }

        return schedules
    }

    /**
     * 백트래킹을 사용하여 재귀적으로 면접 시간을 배정합니다.
     *
     * @param applicants 배정할 지원자 리스트
     * @param currentIndex 현재 처리 중인 지원자 인덱스
     * @param schedules 현재까지 생성된 스케줄 (mutable)
     * @param assignedSlots 현재까지 사용된 시간 슬롯 (mutable)
     * @return 모든 지원자 배정 성공 시 true, 실패 시 false
     */
    private fun backtrack(
        applicants: List<Applicant>,
        currentIndex: Int,
        schedules: MutableList<AutoScheduleDto>,
        assignedSlots: MutableSet<ScheduleDuplicateKey>
    ): Boolean {
        // 모든 지원자를 성공적으로 배정한 경우
        if (currentIndex == applicants.size) {
            return true
        }

        // 남은 지원자들의 배정이 가능한지 1차 확인
        if (!isPossibleToComplete(applicants, currentIndex, assignedSlots)) {
            return false
        }

        val currentApplicant = applicants[currentIndex]

        // 현재 지원자의 모든 가능한 시간을 시도
        for (timeSlot in currentApplicant.availableTimes) {
            val key = ScheduleDuplicateKey.ofUnsafe(
                partId = requireNotNull(currentApplicant.part.id) { "partId를 조회할 수 없습니다"},
                interviewTime = timeSlot
            )

            // 이미 사용 중인 시간 슬롯이면 건너뛰기
            if (assignedSlots.contains(key)) {
                continue
            }

            // 현재 시간 슬롯에 배정 시도
            val schedule = createSchedule(currentApplicant, timeSlot)
            schedules.add(schedule)
            assignedSlots.add(key)


            // 다음 지원자 배정 시도
            if (backtrack(applicants, currentIndex + 1, schedules, assignedSlots)) {
                return true  // 성공적으로 모든 지원자 배정 완료
            }

            // 백트래킹: 현재 배정 취소하고 다른 시간 시도
            schedules.removeAt(schedules.lastIndex)
            assignedSlots.remove(key)
        }

        // 현재 지원자에게 배정 가능한 시간이 없음
        return false
    }

    private fun isPossibleToComplete(
        applicants: List<Applicant>,
        currentIndex: Int,
        assignedSlots: MutableSet<ScheduleDuplicateKey>
    ): Boolean {
        return applicants.drop(currentIndex).all { applicant ->
            applicant.availableTimes.any { time ->
                !assignedSlots.contains(ScheduleDuplicateKey.ofUnsafe(applicant.part.id!!, time))
            }
        }
    }

    /**
     * 지원자와 면접 시간으로 Schedule 객체를 생성합니다.
     */
    private fun createSchedule(applicant: Applicant, interviewTime: LocalDateTime): AutoScheduleDto {
        return AutoScheduleDto(
            applicantId = requireNotNull(applicant.id) { "applicantId를 조회할 수 없습니다"},
            applicantName = applicant.name,
            interviewTime = interviewTime,
            part = applicant.part.name
        )
    }
}