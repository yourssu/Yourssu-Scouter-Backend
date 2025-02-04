package com.yourssu.scouter.hrms.implement.domain.member

import com.yourssu.scouter.common.implement.domain.semester.Semester

class InactiveMember(
    val id: Long? = null,
    val member: Member,
    val activePeriod: SemesterPeriod,
    val expectedReturnSemester: Semester,
    val inactivePeriod: SemesterPeriod,
) : Comparable<InactiveMember> {

    constructor(
        member: Member,
        joinSemester: Semester,
        stateChangeSemester: Semester,
        previousSemesterBeforeStateChange: Semester,
        nextSemesterAfterStateChange: Semester
    ) : this(
        member = member,
        activePeriod = SemesterPeriod(
            startSemester = joinSemester,
            endSemester = previousSemesterBeforeStateChange
        ),
        expectedReturnSemester = nextSemesterAfterStateChange,
        inactivePeriod = SemesterPeriod(
            startSemester = stateChangeSemester,
            endSemester = stateChangeSemester
        ),
    )

    fun updateExpectedReturnSemester(
        expectedReturnSemester: Semester,
        previousSemesterBeforeExpectedReturnSemester: Semester,
    ): InactiveMember {
        return InactiveMember(
            id = id,
            member = member,
            activePeriod = activePeriod,
            expectedReturnSemester = expectedReturnSemester,
            inactivePeriod = SemesterPeriod(inactivePeriod.startSemester, previousSemesterBeforeExpectedReturnSemester),
        )
    }

    override fun compareTo(other: InactiveMember): Int {
        val returnCompare = this.expectedReturnSemester.compareTo(other.expectedReturnSemester)
        if (returnCompare != 0) {
            return returnCompare
        }

        return this.member.compareTo(other.member)
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
}
