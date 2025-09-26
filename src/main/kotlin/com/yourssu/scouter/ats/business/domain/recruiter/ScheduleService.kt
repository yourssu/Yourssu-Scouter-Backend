package com.yourssu.scouter.ats.business.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantReader
import com.yourssu.scouter.ats.implement.domain.recruiter.InterviewSchedule
import com.yourssu.scouter.ats.implement.domain.recruiter.ScheduleReader
import com.yourssu.scouter.ats.implement.domain.recruiter.ScheduleWriter
import com.yourssu.scouter.ats.implement.support.exception.ApplicantNotFoundException
import com.yourssu.scouter.common.implement.domain.part.PartReader
import com.yourssu.scouter.common.implement.support.exception.PartNotFoundException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ScheduleService(
    private val scheduleWriter: ScheduleWriter,
    private val scheduleReader: ScheduleReader,
    private val partReader: PartReader,
    private val applicantReader: ApplicantReader,
) {

    @Transactional
    fun createSchedules(scheduleCommands: List<CreateScheduleCommand>) {
        val schedules = commandsToInterviewSchedules(scheduleCommands)
        scheduleWriter.writeAll(schedules)
    }

    fun readSchedulesByPartId(partId: Long): List<ScheduleDto> {
        val schedules = scheduleReader.readAllByPartId(partId)
        return ScheduleDto.fromDomainList(schedules)
    }

    private fun commandsToInterviewSchedules(commands: List<CreateScheduleCommand>): List<InterviewSchedule> {
        val partIds = commands.map { it.partId }.distinct()
        val applicantIds = commands.map { it.applicantId }.distinct()

        val partsMap = partReader.readAllByIds(partIds).associateBy { it.id }
        val applicantsMap = applicantReader.readByIds(applicantIds).associateBy { it.id }

        return commands.map { command ->
            InterviewSchedule(
                id = null,
                part = partsMap[command.partId] ?: throw PartNotFoundException("partId: ${command.partId}"),
                applicant = applicantsMap[command.applicantId]
                    ?: throw ApplicantNotFoundException("applicantId: ${command.applicantId}"),
                interviewTime = LocalDateTime.parse(command.interviewTime),
            )
        }
    }
}