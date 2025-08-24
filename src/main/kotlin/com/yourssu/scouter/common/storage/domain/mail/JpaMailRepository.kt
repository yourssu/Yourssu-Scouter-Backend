package com.yourssu.scouter.common.storage.domain.mail

import org.springframework.data.jpa.repository.JpaRepository

interface JpaMailRepository : JpaRepository<MailEntity, Long> {
}
