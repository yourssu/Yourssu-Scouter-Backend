package com.yourssu.scouter.ats.business.domain.applicant

data class ApplicantSyncCommand(
    val semesterId: Long,
    val partId: Long,
    val formId: String,
    val nameQuestion: String?,
    val phoneNumberQuestion: String?,
    val ageQuestion: String?,
    val departmentQuestion: String?,
    val studentIdQuestion: String,
    val academicSemesterQuestion: String?,
) {
    fun toMappingQuestionDto(): MappingQuestionDto {
        return MappingQuestionDto(
            nameQuestion = nameQuestion,
            phoneNumberQuestion = phoneNumberQuestion,
            ageQuestion = ageQuestion,
            departmentQuestion = departmentQuestion,
            studentIdQuestion = studentIdQuestion,
            academicSemesterQuestion = academicSemesterQuestion,
        )
    }
}
