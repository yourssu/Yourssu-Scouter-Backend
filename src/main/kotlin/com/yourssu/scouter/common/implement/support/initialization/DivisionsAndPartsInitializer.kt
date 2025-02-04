package com.yourssu.scouter.common.implement.support.initialization

import com.yourssu.scouter.common.implement.domain.division.Division
import com.yourssu.scouter.common.implement.domain.division.DivisionRepository
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.part.PartRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Suppress("NonAsciiCharacters", "FunctionName")
@Component
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
        parts.add(Part(division = division, name = "Head lead"))
        parts.add(Part(division = division, name = "finance"))
        parts.add(Part(division = division, name = "HR"))
        parts.add(Part(division = division, name = "Contents Marketing"))
        parts.add(Part(division = division, name = "legal"))
        parts.add(Part(division = division, name = "PM"))

        partRepository.saveAll(parts)
    }

    private fun initialize_개발() {
        val division = divisionRepository.save(Division(name = "개발", sortPriority = 2))
        val parts = mutableListOf<Part>()
        parts.add(Part(division = division, name = "Backend"))
        parts.add(Part(division = division, name = "Android"))
        parts.add(Part(division = division, name = "iOS"))
        parts.add(Part(division = division, name = "Web-frontend"))

        partRepository.saveAll(parts)
    }

    private fun initialize_디자인() {
        val division = divisionRepository.save(Division(name = "디자인", sortPriority = 3))

        val parts = mutableListOf<Part>()
        parts.add(Part(division = division, name = "Product Design"))

        partRepository.saveAll(parts)
    }
}
