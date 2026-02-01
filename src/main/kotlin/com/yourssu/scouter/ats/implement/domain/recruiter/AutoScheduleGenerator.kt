package com.yourssu.scouter.ats.implement.domain.recruiter

import com.yourssu.scouter.ats.business.domain.recruiter.AutoScheduleDto
import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.ats.implement.support.exception.InvalidScheduleException
import com.yourssu.scouter.ats.implement.support.util.StrategyMapper
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Component
class AutoScheduleGenerator {
    private val strategyMapper = StrategyMapper
    private val seoulZone = ZoneId.of("Asia/Seoul")

    companion object {
        private const val TIMEOUT_MILLIS = 30_000L
    }

    @Transactional(readOnly = true)
    fun generateSchedules(
        applicants: List<Applicant>,
        strategy: String,
        size: Int = 5,
        duration: Duration = Duration.ofMinutes(30),
    ): List<List<AutoScheduleDto>> {
        if (applicants.isEmpty()) {
            return emptyList()
        }

        val sortedApplicants = applicants.sortedBy { it.availableTimes.size }
        val strategyImpl = strategyMapper.getStrategy(strategy)

        val solutions = mutableListOf<ScheduleSolution>()
        val seenSignatures = mutableSetOf<String>()
        val maxPenalty = applicants.size.toLong()

        val startTime = System.currentTimeMillis()
        val searchContext = SearchContext(startTime, TIMEOUT_MILLIS)

        val minPenalty = calculateMinPenalty(sortedApplicants, strategyImpl)

        var penaltyThreshold = minPenalty
        while (solutions.size < size && penaltyThreshold <= maxPenalty && !searchContext.isTimedOut()) {
            searchWithThreshold(
                applicants = sortedApplicants,
                domains = sortedApplicants.map { it.availableTimes.toMutableSet() },
                currentIndex = 0,
                assignment = mutableListOf(),
                usedSlots = mutableSetOf(),
                usedDates = mutableSetOf(),
                currentPenalty = 0L,
                penaltyThreshold = penaltyThreshold,
                solutions = solutions,
                targetCount = size,
                seenSignatures = seenSignatures,
                strategy = strategyImpl,
                duration = duration,
                searchContext = searchContext,
            )
            penaltyThreshold++
        }

        if (searchContext.isTimedOut() && solutions.size < size) {
            fillWithGreedy(
                applicants = sortedApplicants,
                solutions = solutions,
                seenSignatures = seenSignatures,
                targetCount = size,
                duration = duration,
            )
        }

        if (solutions.isEmpty()) {
            val greedySolution = tryGreedySolution(sortedApplicants, duration)
            if (greedySolution != null) {
                return listOf(greedySolution)
            }
            throw InvalidScheduleException("모든 지원자에게 면접 시간을 배정할 수 없습니다.")
        }

        return solutions.map { it.schedules }
    }

    private fun calculateMinPenalty(
        applicants: List<Applicant>,
        strategy: ScheduleStrategy,
    ): Long {
        val allDates =
            applicants.flatMap { applicant ->
                applicant.availableTimes.map { it.atZone(seoulZone).toLocalDate() }
            }.toSet()

        return when (strategy) {
            is com.yourssu.scouter.ats.implement.domain.recruiter.strategy.DistributedDayStrategy -> {
                maxOf(0L, applicants.size.toLong() - allDates.size)
            }
            is com.yourssu.scouter.ats.implement.domain.recruiter.strategy.ConcentratedDayStrategy -> {
                if (applicants.isEmpty()) 0L else 0L
            }
            else -> 0L
        }
    }

    private fun searchWithThreshold(
        applicants: List<Applicant>,
        domains: List<MutableSet<Instant>>,
        currentIndex: Int,
        assignment: MutableList<Pair<Applicant, Instant>>,
        usedSlots: MutableSet<Instant>,
        usedDates: MutableSet<LocalDate>,
        currentPenalty: Long,
        penaltyThreshold: Long,
        solutions: MutableList<ScheduleSolution>,
        targetCount: Int,
        seenSignatures: MutableSet<String>,
        strategy: ScheduleStrategy,
        duration: Duration,
        searchContext: SearchContext,
    ) {
        if (solutions.size >= targetCount || searchContext.isTimedOut()) {
            return
        }

        if (currentIndex == applicants.size) {
            val signature = getAssignmentSignature(assignment)
            if (signature in seenSignatures) {
                return
            }
            seenSignatures.add(signature)

            solutions.add(
                ScheduleSolution(
                    schedules =
                        assignment.map { (applicant, startTime) ->
                            createSchedule(applicant, startTime, duration)
                        },
                    penaltyScore = currentPenalty,
                ),
            )
            return
        }

        val currentApplicant = applicants[currentIndex]
        val currentDomain = domains[currentIndex]

        val orderedSlots = orderSlotsByPenalty(currentDomain, usedDates, strategy)

        for (slot in orderedSlots) {
            if (searchContext.shouldCheckTimeout()) {
                if (searchContext.isTimedOut()) return
            }

            val slotDate = slot.atZone(seoulZone).toLocalDate()
            val isNewDate = slotDate !in usedDates
            val penalty = calculatePenalty(isNewDate, strategy)

            if (currentPenalty + penalty > penaltyThreshold) {
                continue
            }

            if (slot in usedSlots) {
                continue
            }

            val removed = forwardCheck(domains, currentIndex, slot) ?: continue

            assignment.add(currentApplicant to slot)
            usedSlots.add(slot)
            val dateAdded = usedDates.add(slotDate)

            searchWithThreshold(
                applicants = applicants,
                domains = domains,
                currentIndex = currentIndex + 1,
                assignment = assignment,
                usedSlots = usedSlots,
                usedDates = usedDates,
                currentPenalty = currentPenalty + penalty,
                penaltyThreshold = penaltyThreshold,
                solutions = solutions,
                targetCount = targetCount,
                seenSignatures = seenSignatures,
                strategy = strategy,
                duration = duration,
                searchContext = searchContext,
            )

            assignment.removeAt(assignment.lastIndex)
            usedSlots.remove(slot)
            if (dateAdded && assignment.none { (_, s) -> s.atZone(seoulZone).toLocalDate() == slotDate }) {
                usedDates.remove(slotDate)
            }

            removed.forEach { (j, s) -> domains[j].add(s) }
        }
    }

    private fun fillWithGreedy(
        applicants: List<Applicant>,
        solutions: MutableList<ScheduleSolution>,
        seenSignatures: MutableSet<String>,
        targetCount: Int,
        duration: Duration,
    ) {
        var attempts = 0
        val maxAttempts = (targetCount - solutions.size) * 10

        while (solutions.size < targetCount && attempts < maxAttempts) {
            attempts++
            val greedySolution = tryGreedySolutionWithShuffle(applicants, duration, attempts)
            if (greedySolution != null) {
                val signature =
                    greedySolution
                        .sortedWith(compareBy({ it.applicantId }, { it.startTime }))
                        .joinToString("|") { "${it.applicantId}:${it.startTime}" }

                if (signature !in seenSignatures) {
                    seenSignatures.add(signature)
                    solutions.add(ScheduleSolution(greedySolution, Long.MAX_VALUE))
                }
            }
        }
    }

    private fun tryGreedySolution(
        applicants: List<Applicant>,
        duration: Duration,
    ): List<AutoScheduleDto>? {
        return tryGreedySolutionWithShuffle(applicants, duration, 0)
    }

    private fun tryGreedySolutionWithShuffle(
        applicants: List<Applicant>,
        duration: Duration,
        seed: Int,
    ): List<AutoScheduleDto>? {
        val usedSlots = mutableSetOf<Instant>()
        val result = mutableListOf<AutoScheduleDto>()

        val orderedApplicants =
            if (seed == 0) {
                applicants.sortedBy { it.availableTimes.size }
            } else {
                applicants.shuffled(java.util.Random(seed.toLong()))
            }

        for (applicant in orderedApplicants) {
            val availableSlots =
                if (seed == 0) {
                    applicant.availableTimes
                } else {
                    applicant.availableTimes.shuffled(java.util.Random(seed.toLong() + applicant.id!!))
                }

            val slot = availableSlots.firstOrNull { it !in usedSlots }
            if (slot == null) {
                return null
            }

            usedSlots.add(slot)
            result.add(createSchedule(applicant, slot, duration))
        }

        return result
    }

    private fun forwardCheck(
        domains: List<MutableSet<Instant>>,
        currentIndex: Int,
        slot: Instant,
    ): Map<Int, Instant>? {
        val removed = mutableMapOf<Int, Instant>()

        for (j in (currentIndex + 1) until domains.size) {
            if (domains[j].remove(slot)) {
                removed[j] = slot
            }
            if (domains[j].isEmpty()) {
                removed.forEach { (idx, s) -> domains[idx].add(s) }
                return null
            }
        }

        return removed
    }

    private fun orderSlotsByPenalty(
        slots: Set<Instant>,
        usedDates: Set<LocalDate>,
        strategy: ScheduleStrategy,
    ): List<Instant> {
        return slots.sortedBy { slot ->
            val slotDate = slot.atZone(seoulZone).toLocalDate()
            val isNewDate = slotDate !in usedDates
            calculatePenalty(isNewDate, strategy)
        }
    }

    private fun calculatePenalty(
        isNewDate: Boolean,
        strategy: ScheduleStrategy,
    ): Long {
        return when (strategy) {
            is com.yourssu.scouter.ats.implement.domain.recruiter.strategy.DistributedDayStrategy -> {
                if (isNewDate) 0L else 1L
            }
            is com.yourssu.scouter.ats.implement.domain.recruiter.strategy.ConcentratedDayStrategy -> {
                if (isNewDate) 1L else 0L
            }
            else -> 0L
        }
    }

    private fun createSchedule(
        applicant: Applicant,
        startTime: Instant,
        duration: Duration,
    ): AutoScheduleDto {
        return AutoScheduleDto(
            applicantId = requireNotNull(applicant.id) { "applicantId를 조회할 수 없습니다" },
            applicantName = applicant.name,
            startTime = startTime,
            endTime = startTime.plus(duration),
            part = applicant.part.name,
        )
    }

    private fun getAssignmentSignature(assignment: List<Pair<Applicant, Instant>>): String {
        return assignment
            .sortedWith(compareBy({ it.first.id }, { it.second }))
            .joinToString("|") { "${it.first.id}:${it.second}" }
    }

    private data class ScheduleSolution(
        val schedules: List<AutoScheduleDto>,
        val penaltyScore: Long,
    )

    private class SearchContext(
        private val startTime: Long,
        private val timeoutMillis: Long,
    ) {
        private var checkCounter = 0

        fun isTimedOut(): Boolean {
            return System.currentTimeMillis() - startTime > timeoutMillis
        }

        fun shouldCheckTimeout(): Boolean {
            checkCounter++
            return checkCounter % 1000 == 0
        }
    }
}
