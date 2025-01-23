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
        val division = divisionRepository.save(Division(name = "운영"))

        val parts = mutableListOf<Part>()
        parts.add(Part(divisionId = division.id!!, name = "Head lead"))
        parts.add(Part(divisionId = division.id, name = "finance"))
        parts.add(Part(divisionId = division.id, name = "HR"))
        parts.add(Part(divisionId = division.id, name = "marketing"))
        parts.add(Part(divisionId = division.id, name = "legal"))
        parts.add(Part(divisionId = division.id, name = "PM"))

        partRepository.saveAll(parts)
    }

    private fun initialize_개발() {
        val division = divisionRepository.save(Division(name = "개발"))
        val parts = mutableListOf<Part>()
        parts.add(Part(divisionId = division.id!!, name = "Backend"))
        parts.add(Part(divisionId = division.id, name = "Android"))
        parts.add(Part(divisionId = division.id, name = "iOS"))
        parts.add(Part(divisionId = division.id, name = "Web-frontend"))

        partRepository.saveAll(parts)
    }

    private fun initialize_디자인() {
        val division = divisionRepository.save(Division(name = "디자인"))

        val parts = mutableListOf<Part>()
        parts.add(Part(divisionId = division.id!!, name = "Product Design"))

        partRepository.saveAll(parts)
    }
}
