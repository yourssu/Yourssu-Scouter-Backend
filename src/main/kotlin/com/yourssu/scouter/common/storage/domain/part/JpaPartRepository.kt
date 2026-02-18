package com.yourssu.scouter.common.storage.domain.part

import org.springframework.data.jpa.repository.JpaRepository

interface JpaPartRepository : JpaRepository<PartEntity, Long> {
}
