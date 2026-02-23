package com.yourssu.scouter.common.storage.domain.mail

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface JpaMailRepository : JpaRepository<MailEntity, Long> {
    @EntityGraph(attributePaths = ["recipientEmailAddress", "attachments"])
    override fun findById(id: Long): Optional<MailEntity>
}
