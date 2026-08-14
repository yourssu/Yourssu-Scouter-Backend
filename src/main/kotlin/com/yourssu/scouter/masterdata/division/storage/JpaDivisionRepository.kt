package com.yourssu.scouter.masterdata.division.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaDivisionRepository : JpaRepository<DivisionEntity, Long> {
}
