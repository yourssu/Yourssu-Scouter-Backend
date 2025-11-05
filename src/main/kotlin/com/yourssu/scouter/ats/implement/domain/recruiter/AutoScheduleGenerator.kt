package com.yourssu.scouter.ats.implement.domain.recruiter

import com.yourssu.scouter.ats.business.domain.recruiter.AutoScheduleDto
import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.ats.implement.support.exception.InvalidScheduleException
import com.yourssu.scouter.ats.implement.support.util.StrategyMapper
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime

@Component
class AutoScheduleGenerator {

    private val strategyMapper = StrategyMapper

    /**
     * 지원자 리스트에 대해 면접 시간을 자동으로 배정합니다.
     * 백트래킹 알고리즘을 사용하여 모든 가능한 배정 조합을 탐색합니다.
     * 같은 파트의 같은 시간에는 중복 배정하지 않습니다.
     *
     * @param applicants 면접 시간을 배정할 지원자 리스트
     * @param duration 면접 소요 시간 (기본값: 30분)
     * @return 생성된 면접 스케줄 리스트 배열
     * @throws InvalidScheduleException 모든 조합을 시도해도 배정이 불가능한 경우
     */
    @Transactional(readOnly = true)
    fun generateSchedules(
        applicants: List<Applicant>,
        strategy: String,
        size: Int = 5,
        duration: Duration = Duration.ofMinutes(30)
    ): List<List<AutoScheduleDto>> {
        if (applicants.isEmpty()) {
            return emptyList()
        }

        val sortedApplicants = applicants.sortedBy { it.availableTimes.size }

        val schedules = mutableListOf<AutoScheduleDto>()
        val assignedSlots = mutableSetOf<ScheduleDuplicateKey>()

        val scheduleBeam = ScheduleBeam(schedules, assignedSlots, 0L)
        val beamQueue = ArrayDeque<ScheduleBeam>()

        backtrack(
            applicants = sortedApplicants,
            currentIndex = 0,
            beamQueue = beamQueue,
            currentBeam = scheduleBeam,
            strategy = strategyMapper.getStrategy(strategy),
            size = size,
            duration = duration
        )

        if (beamQueue.isEmpty()) {
            throw InvalidScheduleException("모든 지원자에게 면접 시간을 배정할 수 없습니다.")
        }

        return beamQueue.map { it.schedules }
    }

    /**
     * 백트래킹을 사용하여 재귀적으로 면접 시간을 배정합니다.
     *
     * @param applicants 배정할 지원자 리스트
     * @param currentIndex 현재 처리 중인 지원자 인덱스
     * @param beamQueue 최대 size크기의 가능한 스케줄 배열
     * @param currentBeam 현재 탐색 중인 스케줄
     * @param size beamQueue의 최대 크기
     * @param duration 면접 소요 시간
     * @return 모든 지원자 배정 성공 시 true, 실패 시 false
     */
    private fun backtrack(
        applicants: List<Applicant>,
        currentIndex: Int,
        beamQueue: ArrayDeque<ScheduleBeam>,
        currentBeam: ScheduleBeam,
        strategy: ScheduleStrategy,
        size: Int = 5,
        duration: Duration
    ) {
        // beamQueue가 꽉 찼으며, 현재 beam의 penaltyScore가 이미 beamQueue의 최고 penaltyScore를 넘으면 더이상 탐색할 이유가 없음
        if (beamQueue.size >= size && beamQueue.maxBy { it.penaltyScore }.penaltyScore < currentBeam.penaltyScore) {
            return
        }

        // 모든 지원자를 성공적으로 배정한 경우
        if (currentIndex == applicants.size) {
            // 백트래킹으로 인해 수정되지 않도록 복사본을 저장
            beamQueue.add(
                ScheduleBeam(
                    schedules = currentBeam.schedules.toMutableList(),
                    assignedSlots = currentBeam.assignedSlots.toMutableSet(),
                    penaltyScore = currentBeam.penaltyScore
                )
            )
            if (beamQueue.size > size)
                beamQueue.remove(beamQueue.maxBy { it.penaltyScore })
            return
        }

        // 남은 지원자들의 배정이 가능한지 1차 확인
        if (!isPossibleToComplete(applicants, currentIndex, currentBeam.assignedSlots)) {
            return
        }

        val currentApplicant = applicants[currentIndex]

        // 현재 지원자의 모든 가능한 시간을 시도
        for (timeSlot in currentApplicant.availableTimes) {
            val key = ScheduleDuplicateKey.ofUnsafe(
                partId = requireNotNull(currentApplicant.part.id) { "partId를 조회할 수 없습니다"},
                startTime = timeSlot
            )

            // 이미 사용 중인 시간 슬롯이면 건너뛰기
            if (currentBeam.assignedSlots.contains(key)) {
                continue
            }

            // 현재 시간 슬롯에 배정 시도
            val schedule = createSchedule(currentApplicant, timeSlot, duration)
            val penalty = strategy.getPenaltyScore(currentBeam.assignedSlots, schedule)

            currentBeam.schedules.add(schedule)
            currentBeam.assignedSlots.add(key)
            currentBeam.penaltyScore += penalty

            // 다음 지원자 배정 시도
            backtrack(applicants, currentIndex + 1, beamQueue, currentBeam, strategy, size, duration)

            // 백트래킹: 현재 배정 취소하고 다른 시간 시도
            currentBeam.schedules.removeAt(currentBeam.schedules.lastIndex)
            currentBeam.assignedSlots.remove(key)
            currentBeam.penaltyScore -= penalty
        }

        return
    }

    private data class ScheduleBeam(
        val schedules: MutableList<AutoScheduleDto>,
        val assignedSlots: MutableSet<ScheduleDuplicateKey>,
        var penaltyScore: Long
    )

    private fun isPossibleToComplete(
        applicants: List<Applicant>,
        currentIndex: Int,
        assignedSlots: Set<ScheduleDuplicateKey>
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
    private fun createSchedule(applicant: Applicant, startTime: LocalDateTime, duration: Duration): AutoScheduleDto {
        return AutoScheduleDto(
            applicantId = requireNotNull(applicant.id) { "applicantId를 조회할 수 없습니다" },
            applicantName = applicant.name,
            startTime = startTime,
            endTime = startTime.plus(duration),
            part = applicant.part.name
        )
    }
}