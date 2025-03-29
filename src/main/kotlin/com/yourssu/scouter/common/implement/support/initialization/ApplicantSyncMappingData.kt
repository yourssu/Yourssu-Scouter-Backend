package com.yourssu.scouter.common.implement.support.initialization

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "applicant-sync-mapping-data")
class ApplicantSyncMappingData(
    val datas: Array<MappingData>,
) {

    data class MappingData(
        val semester: String,
        val part: String,
        val formId: String,
        val nameQuestion: String? = null,
        val emailQuestion: String? = null,
        val phoneNumberQuestion: String? = null,
        val ageQuestion: String? = null,
        val departmentQuestion: String? = null,
        val studentIdQuestion: String,
        val academicSemesterQuestion: String? = null,
    )
}


