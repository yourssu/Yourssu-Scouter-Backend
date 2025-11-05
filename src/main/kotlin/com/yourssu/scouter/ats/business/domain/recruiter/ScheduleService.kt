package com.yourssu.scouter.ats.business.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantReader
import com.yourssu.scouter.ats.implement.domain.recruiter.AutoScheduleGenerator
import com.yourssu.scouter.ats.implement.domain.recruiter.Schedule
import com.yourssu.scouter.ats.implement.domain.recruiter.ScheduleReader
import com.yourssu.scouter.ats.implement.domain.recruiter.ScheduleWriter
import com.yourssu.scouter.ats.implement.support.exception.ApplicantNotFoundException
import com.yourssu.scouter.common.implement.domain.part.PartReader
import com.yourssu.scouter.common.implement.support.exception.PartNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Service
class ScheduleService(
    private val scheduleWriter: ScheduleWriter,
    private val scheduleReader: ScheduleReader,
    private val partReader: PartReader,
    private val applicantReader: ApplicantReader,
    private val scheduleValidator: ScheduleValidator,
    private val autoScheduleGenerator: AutoScheduleGenerator
) {

    private val logger = org.slf4j.LoggerFactory.getLogger(ScheduleService::class.java)

    @Transactional
    fun createSchedules(scheduleCommands: List<CreateScheduleCommand>) {
        val schedules = commandsToInterviewSchedules(scheduleCommands)
        scheduleValidator.validateNoDuplicates(schedules)
        scheduleWriter.writeAll(schedules)
    }

    fun readSchedulesByPartId(partId: Long): List<ScheduleDto> {
        val schedules = scheduleReader.readAllByPartId(partId)
        return schedules.map(ScheduleDto::from)
    }

    fun autoGenerateSchedules(partId: Long, strategy: String, duration: Long, size: Int): List<List<AutoScheduleDto>> {
        val applicants = applicantReader.readByPartId(partId)
        return autoScheduleGenerator.generateSchedules(applicants, strategy, size, Duration.ofMinutes(duration))
    }

    @Transactional
    fun deleteByPart(partId: Long): Int {
        val part = partReader.readById(partId) // 파트가 존재하는지 확인, 존재하지 않으면 PartNotFoundException이 발생함
        logger.debug("${part.name} 파트의 모든 면접 스케줄을 삭제합니다")
        return scheduleWriter.deleteAllByPart(partId)
    }

    @Transactional
    fun updateByPart(partId: Long, scheduleCommands: List<CreateScheduleCommand>) {
        val requests = commandsToInterviewSchedules(scheduleCommands)
        scheduleValidator.validateNoDuplicates(requests)
        val requestsMap = requests.associateBy { it.startTime }

        val exists = scheduleReader.readAllByPartId(partId)
        val existsMap = exists.associateBy { it.startTime }

        val toDeletes =
            exists.filter { !requestsMap.containsKey(it.startTime) || requestsMap[it.startTime]?.applicant?.id != it.applicantId }
                .map { it.id }
        scheduleWriter.deleteAll(toDeletes)

        val toCreates =
            requests.filter { !existsMap.containsKey(it.startTime) || existsMap[it.startTime]?.applicantId != it.applicant.id }
        scheduleWriter.writeAll(toCreates)
    }

    private fun commandsToInterviewSchedules(commands: List<CreateScheduleCommand>): List<Schedule> {
        val partIds = commands.map { it.partId }.distinct()
        val applicantIds = commands.map { it.applicantId }.distinct()

        val partsMap = partReader.readAllByIds(partIds).associateBy { it.id }
        val applicantsMap = applicantReader.readByIdsWithoutAvailableTimes(applicantIds).associateBy { it.id }

        return commands.map { command ->
            Schedule.create(
                applicant = applicantsMap[command.applicantId]
                    ?: throw ApplicantNotFoundException("지원자 정보를 찾을 수 없습니다: ${command.applicantId}"),
                startTime = command.startTime,
                endTime = command.endTime,
                part = partsMap[command.partId]
                    ?: throw PartNotFoundException("파트를 찾을 수 없습니다: ${command.partId}"),
            )
        }
    }
}