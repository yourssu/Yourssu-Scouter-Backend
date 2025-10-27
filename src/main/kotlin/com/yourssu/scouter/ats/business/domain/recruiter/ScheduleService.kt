package com.yourssu.scouter.ats.business.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantReader
import com.yourssu.scouter.ats.implement.domain.recruiter.Schedule
import com.yourssu.scouter.ats.implement.domain.recruiter.ScheduleReader
import com.yourssu.scouter.ats.implement.domain.recruiter.ScheduleWriter
import com.yourssu.scouter.ats.implement.support.exception.ApplicantNotFoundException
import com.yourssu.scouter.ats.implement.support.exception.ScheduleNotFoundException
import com.yourssu.scouter.common.implement.domain.part.PartReader
import com.yourssu.scouter.common.implement.support.exception.PartNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ScheduleService(
    private val scheduleWriter: ScheduleWriter,
    private val scheduleReader: ScheduleReader,
    private val partReader: PartReader,
    private val applicantReader: ApplicantReader,
    private val scheduleValidator: ScheduleValidator,
) {

    private val autoScheduleGenerator = AutoScheduleGenerator()

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

    fun autoGenerateSchedules(partId: Long): List<AutoScheduleDto> {
        val applicants = applicantReader.readByPartId(partId)
        return autoScheduleGenerator.generateSchedules(applicants)
    }

    fun deleteByPart(partId: Long): Int {
        partReader.readById(partId) // 파트가 존재하는지 확인, 존재하지 않으면 PartNotFoundException이 발생함
        return scheduleWriter.deleteAllByPart(partId)
    }

    fun deleteOne(scheduleId: Long) {
        require(scheduleReader.existsById(scheduleId)) {
            throw ScheduleNotFoundException(scheduleId)
        }
        scheduleWriter.deleteOne(scheduleId)
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
                interviewTime = command.interviewTime,
                part = partsMap[command.partId]
                    ?: throw PartNotFoundException("파트를 찾을 수 없습니다: ${command.partId}"),
            )
        }
    }
}