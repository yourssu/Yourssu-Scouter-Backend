package com.yourssu.scouter

import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = [
    "token.jwt.access-key=test-access-key-test-access-key-test-32bytes!",
    "token.jwt.refresh-key=test-refresh-key-test-refresh-key-test-32bytes!",
])
class ScouterApplicationTests {

	@Test
	fun contextLoads() {
	}

}
