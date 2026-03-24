package com.yourssu.scouter.hrms.implement.support

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 멤버 인포 엑셀 업로드/다운로드 보호용.
 * [uploadPassword]가 비어 있으면 [downloadPassword]를 업로드 비밀번호로 사용한다.
 */
@ConfigurationProperties(prefix = "scouter.member-excel-tool")
data class MemberExcelToolProperties(
    val downloadPassword: String = "",
    val uploadPassword: String = "",
)
