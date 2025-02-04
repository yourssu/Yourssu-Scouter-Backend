package com.yourssu.scouter.common.storage.domain.basetime

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import java.time.LocalDateTime
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@EntityListeners(AuditingEntityListener::class)
@MappedSuperclass
class BaseCreateTimeEntity {

    @CreatedDate
    @Column(updatable = false, nullable = false)
    var createdTime: LocalDateTime? = null
        protected set
}
