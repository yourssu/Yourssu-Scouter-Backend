package com.yourssu.scouter.common.storage.domain.basetime

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import java.time.LocalDateTime
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@EntityListeners(AuditingEntityListener::class)
@MappedSuperclass
class BaseTimeEntity : BaseCreateTimeEntity() {

    @LastModifiedDate
    @Column(nullable = false)
    var updatedTime: LocalDateTime? = null
        protected set
}
