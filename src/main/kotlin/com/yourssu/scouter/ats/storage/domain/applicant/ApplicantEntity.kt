package com.yourssu.scouter.ats.storage.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantState
import com.yourssu.scouter.common.storage.domain.part.PartEntity
import com.yourssu.scouter.common.storage.domain.semester.SemesterEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "applicant")
class ApplicantEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val email: String,

    @Column(nullable = false)
    val phoneNumber: String,

    @Column(nullable = false)
    val age: String,

    @Column(nullable = false)
    val department: String,

    @Column(nullable = false)
    val studentId: String,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "part_id", nullable = false, foreignKey = ForeignKey(name = "fk_applicant_part"))
    val part: PartEntity,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val state: ApplicantState,

    @Column(nullable = false)
    val applicationDateTime: LocalDateTime,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "semester_id", nullable = false, foreignKey = ForeignKey(name = "fk_applicant_semester"))
    val applicationSemester: SemesterEntity,

    @Column(nullable = false)
    val academicSemester: String,
) {

    companion object {
        fun from(applicant: Applicant): ApplicantEntity = ApplicantEntity(
            id = applicant.id,
            name = applicant.name,
            email = applicant.email,
            phoneNumber = applicant.phoneNumber,
            age = applicant.age,
            department = applicant.department,
            studentId = applicant.studentId,
            part = PartEntity.from(applicant.part),
            state = applicant.state,
            applicationDateTime = applicant.applicationDateTime,
            applicationSemester = SemesterEntity.from(applicant.applicationSemester),
            academicSemester = applicant.academicSemester,
        )
    }

    fun toDomain(): Applicant = Applicant(
        id = id,
        name = name,
        email = email,
        phoneNumber = phoneNumber,
        age = age,
        department = department,
        studentId = studentId,
        part = part.toDomain(),
        state = state,
        applicationDateTime = applicationDateTime,
        applicationSemester = applicationSemester.toDomain(),
        academicSemester = academicSemester,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ApplicantEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "ApplicantEntity(id=$id, name='$name', email='$email', phoneNumber='$phoneNumber', age='$age', department=$department, studentId='$studentId', part=$part, state=$state, applicationDate=$applicationDateTime, applicationSemester=$applicationSemester)"
    }
}
