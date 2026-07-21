package com.yourssu.scouter.common.part.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaPartRepository : JpaRepository<PartEntity, Long> {
}
