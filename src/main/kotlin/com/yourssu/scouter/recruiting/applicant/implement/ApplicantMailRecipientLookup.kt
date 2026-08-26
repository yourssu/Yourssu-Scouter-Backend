package com.yourssu.scouter.recruiting.applicant.implement

import com.yourssu.scouter.mail.core.business.MailRecipientLookup
import com.yourssu.scouter.mail.core.business.MailRecipientProfile
import org.springframework.stereotype.Component

@Component
class ApplicantMailRecipientLookup(
    private val applicantReader: ApplicantReader,
) : MailRecipientLookup {

    override fun findByIds(applicantIds: List<Long>): Map<Long, MailRecipientProfile> =
        applicantReader.readByIds(applicantIds).mapValues { (_, applicant) ->
            MailRecipientProfile(
                email = applicant.email,
                attributes = applicant.toAttributeMap(),
            )
        }
}
