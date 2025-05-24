package com.yourssu.scouter.common.business.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.Mail
import com.yourssu.scouter.common.implement.domain.mail.MailReserveCommand
import com.yourssu.scouter.common.implement.domain.mail.MailWriter
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import org.springframework.stereotype.Service

@Service
class MailService(
    private val mailWriter: MailWriter,
    private val memberReader: MemberReader,
) {

    fun reserveMail(command: MailReserveCommand) {
        val sender: Member = memberReader.readById(command.senderMemberId)
        val mail: Mail = command.toMail(sender)

        mailWriter.reserve(mail, command.reservationTime)
    }
}
