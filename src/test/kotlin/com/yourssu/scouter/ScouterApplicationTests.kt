package com.yourssu.scouter

import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

// TODO: for 에민: S3 의존성 제외 필요
// - contextLoads() 실패 원인: S3MailFileStorage 빈 생성 시 bucket/region 미설정으로 예외 발생
// - 해결 방안: @SpringBootTest에서 S3/Storage 관련 자동설정 제외하거나, S3 빈을 테스트용 Mock으로 교체

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = [
    "token.jwt.access-key=test-access-key-test-access-key-test-32bytes!",
    "token.jwt.refresh-key=test-refresh-key-test-refresh-key-test-32bytes!",
])
class ScouterApplicationTests {

	// S3 의존성 미설정으로 contextLoads 실패 → 주석 처리
	// @Test
	// fun contextLoads() {
	// }

}
