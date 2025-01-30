package com.yourssu.scouter.hrms.implement.domain.member

class GraduatedMember(
    val id: Long? = null,
    val member: Member,
    val activePeriod: SemesterPeriod,
    val isAdvisorDesired: Boolean,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GraduatedMember

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "GraduatedMember(id=$id, member=$member, activePeriod=$activePeriod, isAdvisorDesired=$isAdvisorDesired)"
    }
}
