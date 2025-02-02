package com.yourssu.scouter.hrms.implement.domain.member

import com.yourssu.scouter.common.implement.domain.semester.Semester
import java.time.LocalDate

class InactiveMember(
    val id: Long? = null,
    val member: Member,
    val activePeriod: SemesterPeriod,
    val expectedReturnSemester: Semester,
    val inactivePeriod: SemesterPeriod,
) {

    constructor(member: Member, stateChangeDate: LocalDate) : this(
        member = member,
        activePeriod = SemesterPeriod(
            startSemester = Semester.of(member.joinDate),
            endSemester = Semester.previous(stateChangeDate)
        ),
        expectedReturnSemester = Semester.next(stateChangeDate),
        inactivePeriod = SemesterPeriod(
            startSemester = Semester.of(stateChangeDate),
            endSemester = Semester.of(stateChangeDate)
        ),
    )

    fun updateExpectedReturnSemester(expectedReturnSemester: Semester): InactiveMember {
        return InactiveMember(
            id = id,
            member = member,
            activePeriod = activePeriod,
            expectedReturnSemester = expectedReturnSemester,
            inactivePeriod = SemesterPeriod(inactivePeriod.startSemester, expectedReturnSemester.previousSemester()),
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InactiveMember

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "InactiveMember(id=$id, member=$member, activePeriod=$activePeriod, expectedReturnSemester=$expectedReturnSemester, inactivePeriod=$inactivePeriod)"
    }
}
