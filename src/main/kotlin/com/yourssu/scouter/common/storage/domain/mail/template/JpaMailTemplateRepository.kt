package com.yourssu.scouter.common.storage.domain.mail.template

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface JpaMailTemplateRepository : JpaRepository<MailTemplateEntity, Long> {
    fun findAllBy(pageable: Pageable): Page<MailTemplateEntity>

    @EntityGraph(attributePaths = ["variables", "attachmentReferences"])
    override fun findById(id: Long): Optional<MailTemplateEntity>
}
