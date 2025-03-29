package com.yourssu.scouter.ats.storage.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantSyncMapping
import com.yourssu.scouter.common.storage.domain.part.PartEntity
import com.yourssu.scouter.common.storage.domain.semester.SemesterEntity
import jakarta.persistence.Column
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
@Table(name = "applicant_sync_mapping")
class ApplicantSyncMappingEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "applicant_semester_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_applicant_sync_mapping_semester")
    )
    val applicantSemester: SemesterEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "part_id", nullable = false, foreignKey = ForeignKey(name = "fk_applicant_sync_mapping_part"))
    val part: PartEntity,

    @Column(nullable = false)
    val formId: String,

    val nameQuestion: String?,

    val phoneNumberQuestion: String?,

    val ageQuestion: String?,

    val departmentQuestion: String?,

    @Column(nullable = false)
    val studentIdQuestion: String,

    val academicSemesterQuestion: String?,
) {

    companion object {
        fun from(applicantSyncMapping: ApplicantSyncMapping): ApplicantSyncMappingEntity {
            return ApplicantSyncMappingEntity(
                id = applicantSyncMapping.id,
                applicantSemester = SemesterEntity.from(applicantSyncMapping.applicantSemester),
                part = PartEntity.from(applicantSyncMapping.part),
                formId = applicantSyncMapping.formId,
                nameQuestion = applicantSyncMapping.nameQuestion,
                phoneNumberQuestion = applicantSyncMapping.phoneNumberQuestion,
                ageQuestion = applicantSyncMapping.ageQuestion,
                departmentQuestion = applicantSyncMapping.departmentQuestion,
                studentIdQuestion = applicantSyncMapping.studentIdQuestion,
                academicSemesterQuestion = applicantSyncMapping.academicSemesterQuestion,
            )
        }
    }

    fun toDomain(): ApplicantSyncMapping {
        return ApplicantSyncMapping(
            id = id,
            applicantSemester = applicantSemester.toDomain(),
            part = part.toDomain(),
            formId = formId,
            nameQuestion = nameQuestion,
            phoneNumberQuestion = phoneNumberQuestion,
            ageQuestion = ageQuestion,
            departmentQuestion = departmentQuestion,
            studentIdQuestion = studentIdQuestion,
            academicSemesterQuestion = academicSemesterQuestion,
        )
    }
}
