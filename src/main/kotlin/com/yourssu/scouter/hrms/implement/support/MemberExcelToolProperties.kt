package com.yourssu.scouter.hrms.implement.support

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 멤버 인포 엑셀 업로드 페이지의 "다운로드" 보호용.
 * [downloadPassword]가 비어 있으면 비밀번호 경로는 비활성화되고, Bearer + HR/스카우터 특권만 허용된다.
 */
@ConfigurationProperties(prefix = "scouter.member-excel-tool")
data class MemberExcelToolProperties(
    val downloadPassword: String = "",
)
