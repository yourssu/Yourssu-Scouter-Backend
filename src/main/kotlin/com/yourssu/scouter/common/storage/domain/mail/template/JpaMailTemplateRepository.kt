package com.yourssu.scouter.common.storage.domain.mail.template

import org.springframework.data.jpa.repository.JpaRepository

interface JpaMailTemplateRepository : JpaRepository<MailTemplateEntity, Long>
