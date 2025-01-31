package com.yourssu.scouter.dummy

import com.yourssu.scouter.common.storage.domain.user.JpaUserRepository
import com.yourssu.scouter.common.storage.domain.user.UserEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DummyController(
    private val jpaUserRepository: JpaUserRepository,
) {

    @GetMapping("/dummy/users")
    fun users(): ResponseEntity<List<UserEntity>> {
        return ResponseEntity.ok(jpaUserRepository.findAll())
    }
}
