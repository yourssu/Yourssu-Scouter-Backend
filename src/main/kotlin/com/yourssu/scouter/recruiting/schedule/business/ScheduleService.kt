package com.yourssu.scouter.recruiting.schedule.business

import com.yourssu.scouter.recruiting.schedule.business.dto.CreateScheduleCommand

import com.yourssu.scouter.recruiting.schedule.business.dto.UpdateScheduleLocationCommand

import com.yourssu.scouter.recruiting.schedule.business.dto.ScheduleDto

import com.yourssu.scouter.recruiting.schedule.business.dto.AutoScheduleDto

import com.yourssu.scouter.recruiting.applicant.implement.ApplicantReader
import com.yourssu.scouter.recruiting.schedule.implement.AutoScheduleGenerator
import com.yourssu.scouter.recruiting.schedule.implement.Schedule
import com.yourssu.scouter.recruiting.schedule.implement.ScheduleReader
import com.yourssu.scouter.recruiting.schedule.implement.ScheduleWriter
import com.yourssu.scouter.recruiting.support.implement.exception.ApplicantNotFoundException
import com.yourssu.scouter.recruiting.support.implement.exception.ScheduleNotFoundException
import com.yourssu.scouter.masterdata.part.implement.PartReader
import com.yourssu.scouter.masterdata.support.exception.PartNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant

@Service
class ScheduleService(
    private val scheduleWriter: ScheduleWriter,
    private val scheduleReader: ScheduleReader,
    private val partReader: PartReader,
    private val applicantReader: ApplicantReader,
    private val autoScheduleGenerator: AutoScheduleGenerator,
) {
    private val logger = org.slf4j.LoggerFactory.getLogger(ScheduleService::class.java)

    @Transactional
    fun createSchedules(scheduleCommands: List<CreateScheduleCommand>) {
        val schedules = commandsToInterviewSchedules(scheduleCommands)
        scheduleWriter.writeAll(schedules)
    }

    fun readSchedules(partId: Long?): List<ScheduleDto> {
        val schedules = if (partId == null) scheduleReader.readAll() else scheduleReader.readAllByPartId(partId)
        return schedules.map(ScheduleDto::from)
    }

    fun autoGenerateSchedules(
        partId: Long,
        strategy: String,
        duration: Long,
        size: Int,
    ): List<List<AutoScheduleDto>> {
        val applicants = applicantReader.readByPartIdUnderReview(partId)
        return autoScheduleGenerator.generateSchedules(applicants, strategy, size, Duration.ofMinutes(duration))
    }

    @Transactional
    fun deleteByPart(partId: Long): Int {
        val part = partReader.readById(partId) // 파트가 존재하는지 확인, 존재하지 않으면 PartNotFoundException이 발생함
        logger.debug("${part.name} 파트의 모든 면접 스케줄을 삭제합니다")
        return scheduleWriter.deleteAllByPart(partId)
    }

    @Transactional
    fun updateByPart(
        partId: Long,
        scheduleCommands: List<CreateScheduleCommand>,
    ) {
        val requests = commandsToInterviewSchedules(scheduleCommands)
        val exists = scheduleReader.readAllByPartId(partId)

        // 같은 시간대에 여러 스케줄이 존재할 수 있으므로 (시작 시간, 지원자) 조합으로 비교한다.
        val requestKeys: Set<Pair<Instant, Long?>> = requests.map { it.startTime to it.applicant.id }.toSet()
        val existsKeys: Set<Pair<Instant, Long?>> = exists.map { it.startTime to it.applicantId }.toSet()

        val toDeletes = exists.filter { (it.startTime to it.applicantId) !in requestKeys }.map { it.id }
        if (toDeletes.isNotEmpty()) {
            scheduleWriter.deleteAll(toDeletes)
        }

        val toCreates = requests.filter { (it.startTime to it.applicant.id) !in existsKeys }
        if (toCreates.isNotEmpty()) {
            scheduleWriter.writeAll(toCreates)
        }
    }

    @Transactional
    fun updateLocation(command: UpdateScheduleLocationCommand) {
        if (!scheduleReader.existsById(command.scheduleId)) {
            throw ScheduleNotFoundException(command.scheduleId)
        }

        scheduleWriter.updateLocationById(command.scheduleId, command.locationType, command.locationDetail)
    }

    private fun commandsToInterviewSchedules(commands: List<CreateScheduleCommand>): List<Schedule> {
        val partIds = commands.map { it.partId }.distinct()
        val applicantIds = commands.map { it.applicantId }.distinct()

        val partsMap = partReader.readAllByIds(partIds).associateBy { it.id }
        val applicantsMap = applicantReader.readByIdsWithoutAvailableTimes(applicantIds).associateBy { it.id }

        return commands.map { command ->
            Schedule.create(
                applicant =
                    applicantsMap[command.applicantId]
                        ?: throw ApplicantNotFoundException("지원자 정보를 찾을 수 없습니다: ${command.applicantId}"),
                startTime = command.startTime,
                endTime = command.endTime,
                part =
                    partsMap[command.partId]
                        ?: throw PartNotFoundException("파트를 찾을 수 없습니다: ${command.partId}"),
                locationType = command.locationType,
                locationDetail = command.locationDetail,
            )
        }
    }
}
