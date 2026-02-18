package com.yourssu.scouter.common.storage.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.Mail
import com.yourssu.scouter.common.implement.domain.mail.MailRepository
import org.springframework.stereotype.Repository

@Repository
class MailRepositoryImpl(
    private val jpaMailRepository: JpaMailRepository,
    private val mailEntityMapper: MailEntityMapper,
) : MailRepository {

    override fun save(mail: Mail): Mail {
        return mailEntityMapper.toDomain(
            jpaMailRepository.save(mailEntityMapper.toEntity(mail))
        )
    }

    override fun findById(mailId: Long): Mail? {
        return jpaMailRepository.findById(mailId)
            .map(mailEntityMapper::toDomain)
            .orElse(null)
    }
}
