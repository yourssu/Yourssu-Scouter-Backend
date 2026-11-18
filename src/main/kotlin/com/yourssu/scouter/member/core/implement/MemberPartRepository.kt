package com.yourssu.scouter.member.core.implement

interface MemberPartRepository {

    fun existsAssignment(memberId: Long, partId: Long): Boolean

    fun countDistinctParts(memberId: Long): Long

    fun assign(memberId: Long, partId: Long)

    fun unassign(memberId: Long, partId: Long)
}
