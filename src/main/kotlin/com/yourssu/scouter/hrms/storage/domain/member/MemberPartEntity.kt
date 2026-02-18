package com.yourssu.scouter.hrms.storage.domain.member

import com.yourssu.scouter.common.storage.domain.part.PartEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "member_part")
class MemberPartEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = ForeignKey(name = "fk_member_part_member"))
    val member: MemberEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "part_id", nullable = false, foreignKey = ForeignKey(name = "fk_member_part_part"))
    val part: PartEntity,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MemberPartEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
