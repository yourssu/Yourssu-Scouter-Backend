package com.yourssu.scouter.recruiting.applicant.implement

import com.yourssu.scouter.mail.template.implement.RecipientAttributeResolver
import org.springframework.stereotype.Component

@Component
class ApplicantAttributeResolver(
    private val applicantReader: ApplicantReader,
) : RecipientAttributeResolver {
    override fun availableKeys(): Set<String> = Applicant.ATTRIBUTE_KEYS

    override fun resolve(emails: List<String>): Map<String, Map<String, String?>> =
        applicantReader
            .readByEmails(emails)
            .mapValues { (_, applicant) ->
                applicant.toAttributeMap()
            }
}
