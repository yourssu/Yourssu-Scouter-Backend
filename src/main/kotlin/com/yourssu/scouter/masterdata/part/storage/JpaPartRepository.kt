package com.yourssu.scouter.masterdata.part.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaPartRepository : JpaRepository<PartEntity, Long> {
}
