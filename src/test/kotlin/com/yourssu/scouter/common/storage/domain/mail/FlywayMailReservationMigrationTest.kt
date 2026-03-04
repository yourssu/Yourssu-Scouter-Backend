package com.yourssu.scouter.common.storage.domain.mail

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.sql.DriverManager

/**
 * Flyway V2·V3 마이그레이션이 MySQL에서 정상 동작하는지 검증한다.
 * - V2: mail_reservation.status 컬럼 추가 및 기존 데이터 보정
 * - V3: member.state ENUM에 COMPLETED 추가 (member 테이블 필요)
 * 사전조건: mail_reservation·member 테이블을 테스트에서 생성 후 Flyway 전체 실행(최신까지 적용).
 * Docker 필요. Docker 없으면 스킵됨.
 */
@Testcontainers(disabledWithoutDocker = true)
@Suppress("NonAsciiCharacters")
class FlywayMailReservationMigrationTest {

    companion object {
        @Container
        @JvmStatic
        val mysql: MySQLContainer<*> =
            MySQLContainer(DockerImageName.parse("mysql:8.0"))
                .withDatabaseName("scouter")
                .withUsername("test")
                .withPassword("test")
    }

    @Test
    fun `V2 마이그레이션 적용 시 status 컬럼이 추가되고 기존 데이터가 보정된다`() {
        val jdbcUrl = mysql.jdbcUrl
        val username = mysql.username
        val password = mysql.password

        // 1. 사전조건: V2·V3 마이그레이션에 필요한 테이블만 생성 (실무와 동일하게 최신 마이그레이션 전부 적용)
        DriverManager.getConnection(jdbcUrl, username, password).use { conn ->
            conn.createStatement().use { stmt ->
                // V2 대상
                stmt.execute(
                    """
                    CREATE TABLE IF NOT EXISTS mail_reservation (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        mail_id BIGINT NOT NULL,
                        reservation_time TIMESTAMP(6) NOT NULL
                    )
                    """.trimIndent(),
                )
                stmt.execute(
                    "INSERT INTO mail_reservation (mail_id, reservation_time) VALUES (1, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 1 HOUR))",
                )
                stmt.execute(
                    "INSERT INTO mail_reservation (mail_id, reservation_time) VALUES (1, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 1 HOUR))",
                )
                // V3 대상: member 테이블 (V3 적용 전 state ENUM)
                stmt.execute(
                    """
                    CREATE TABLE IF NOT EXISTS member (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        state ENUM('ACTIVE', 'INACTIVE', 'GRADUATED', 'WITHDRAWN') NOT NULL
                    )
                    """.trimIndent(),
                )
            }
        }

        // 2. Flyway 실행 — target 제한 없이 최신까지 전부 적용 (실무와 동일)
        val flyway =
            Flyway.configure()
                .dataSource(jdbcUrl, username, password)
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .locations("classpath:db/migration")
                .load()
        flyway.migrate()

        // 3. 검증: status 컬럼 존재, 과거 예약은 PENDING_SEND, 미래 예약은 SCHEDULED
        DriverManager.getConnection(jdbcUrl, username, password).use { conn ->
            conn.createStatement().use { stmt ->
                val rs =
                    stmt.executeQuery(
                        "SELECT id, status, reservation_time FROM mail_reservation ORDER BY reservation_time",
                    )
                val rows = mutableListOf<Pair<Long, String>>()
                while (rs.next()) {
                    rows.add(rs.getLong("id") to rs.getString("status"))
                }
                assertEquals(2, rows.size)
                // 과거 예약: DEFAULT 'PENDING_SEND' 유지
                assertEquals("PENDING_SEND", rows[0].second)
                // 미래 예약: UPDATE로 SCHEDULED로 보정됨
                assertEquals("SCHEDULED", rows[1].second)
            }
        }
    }
}
