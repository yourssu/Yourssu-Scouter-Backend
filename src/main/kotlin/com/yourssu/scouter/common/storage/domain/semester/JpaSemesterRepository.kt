package com.yourssu.scouter.common.storage.domain.semester

import org.springframework.data.jpa.repository.JpaRepository

interface JpaSemesterRepository : JpaRepository<SemesterEntity, Long> {
}
