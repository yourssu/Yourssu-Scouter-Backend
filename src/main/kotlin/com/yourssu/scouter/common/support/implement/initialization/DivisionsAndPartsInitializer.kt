package com.yourssu.scouter.common.support.implement.initialization

import com.yourssu.scouter.masterdata.division.implement.Division
import com.yourssu.scouter.masterdata.division.implement.DivisionRepository
import com.yourssu.scouter.masterdata.part.implement.Part
import com.yourssu.scouter.masterdata.part.implement.PartRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Suppress("NonAsciiCharacters", "FunctionName")
@Component
@Order(2)
@Transactional
class DivisionsAndPartsInitializer(
    private val divisionRepository: DivisionRepository,
    private val partRepository: PartRepository,
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (alreadyInitialized()) {
            return
        }

        initialize_운영()
        initialize_개발()
        initialize_디자인()
    }

    private fun alreadyInitialized() = divisionRepository.count() != 0L

    private fun initialize_운영() {
        val division = divisionRepository.save(Division(name = "운영", sortPriority = 1))

        val parts = mutableListOf<Part>()
        parts.add(Part(division = division, hasAssignment = false, name ="Head lead", sortPriority = 1))
        parts.add(Part(division = division, hasAssignment = false, name ="Finance", sortPriority = 2))
        parts.add(Part(division = division, hasAssignment = false, name ="HR", sortPriority = 3))
        parts.add(Part(division = division, hasAssignment = false, name ="Marketing", sortPriority = 4))
        parts.add(Part(division = division, hasAssignment = false, name ="Legal", sortPriority = 5))
        parts.add(Part(division = division, hasAssignment = false, name ="PM", sortPriority = 6))

        partRepository.saveAll(parts)
    }

    private fun initialize_개발() {
        val division = divisionRepository.save(Division(name = "개발", sortPriority = 2))
        val parts = mutableListOf<Part>()
        parts.add(Part(division = division, hasAssignment = false, name ="Backend", sortPriority = 1))
        parts.add(Part(division = division, hasAssignment = false, name ="Android", sortPriority = 2))
        parts.add(Part(division = division, hasAssignment = false, name ="iOS", sortPriority = 3))
        parts.add(Part(division = division, hasAssignment = false, name ="Frontend", sortPriority = 4))

        partRepository.saveAll(parts)
    }

    private fun initialize_디자인() {
        val division = divisionRepository.save(Division(name = "디자인", sortPriority = 3))

        val parts = mutableListOf<Part>()
        parts.add(Part(division = division, hasAssignment = false, name ="Product Design", sortPriority = 1))

        partRepository.saveAll(parts)
    }
}
