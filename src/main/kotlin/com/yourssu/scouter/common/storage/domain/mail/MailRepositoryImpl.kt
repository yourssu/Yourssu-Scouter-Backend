package com.yourssu.scouter.common.storage.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.Mail
import com.yourssu.scouter.common.implement.domain.mail.MailRepository
import org.springframework.stereotype.Repository

@Repository
class MailRepositoryImpl(
    private val jpaMailRepository: JpaMailRepository,
) : MailRepository {

    override fun save(mail: Mail): Mail {
        return jpaMailRepository.save(MailEntity.from(mail)).toDomain()
    }

    override fun findById(mailId: Long): Mail? {
        return jpaMailRepository.findById(mailId).orElse(null)?.toDomain()
    }
}
