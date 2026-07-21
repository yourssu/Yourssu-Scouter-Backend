package com.yourssu.scouter.common.division.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaDivisionRepository : JpaRepository<DivisionEntity, Long> {
}
