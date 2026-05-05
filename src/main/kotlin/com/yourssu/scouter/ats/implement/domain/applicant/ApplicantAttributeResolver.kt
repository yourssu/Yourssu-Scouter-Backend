package com.yourssu.scouter.ats.implement.domain.applicant

import com.yourssu.scouter.common.implement.domain.mail.template.RecipientAttributeResolver
import org.springframework.stereotype.Component

@Component
class ApplicantAttributeResolver(
    private val applicantReader: ApplicantReader,
) : RecipientAttributeResolver {

    override fun availableKeys(): Set<String> = Applicant.ATTRIBUTE_KEYS

    override fun resolve(emails: List<String>): Map<String, Map<String, String?>> {
        // TODO:
        // 1. applicantReader.readByEmails(emails) 로 배치 조회 → Map<email, Applicant>
        // 2. 각 Applicant를 Map<attributeKey, value> 로 변환
        // 3. email이 없는 경우 결과 map에서 누락 (MailRenderingValidator가 누락 감지)
        return applicantReader
            .readByEmails(emails)
            .mapValues { (_, applicant) ->
                applicant.toAttributeMap()
            }
    }
}
