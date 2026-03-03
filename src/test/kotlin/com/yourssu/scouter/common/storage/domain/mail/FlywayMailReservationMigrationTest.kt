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
 * Flyway V2 마이그레이션( mail_reservation.status 컬럼 추가 )이 MySQL에서 정상 동작하는지 검증한다.
 * 사전조건: mail_reservation 테이블이 존재해야 함 (baseline 또는 Hibernate로 생성된 스키마)
 * Docker 필요. Docker 없으면 스킵됨.
 *
 * 주의: V1 migration SQL이 없어 테스트에서 DDL로 테이블을 직접 생성한다.
 * V1에서 mail_reservation 컬럼 구조가 변경되면 이 테스트의 DDL도 함께 수정해야 한다.
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

        // 1. 사전조건: mail_reservation 테이블만 생성 (마이그레이션 대상만 검증)
        DriverManager.getConnection(jdbcUrl, username, password).use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute(
                    """
                    CREATE TABLE IF NOT EXISTS mail_reservation (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        mail_id BIGINT NOT NULL,
                        reservation_time TIMESTAMP(6) NOT NULL
                    )
                    """.trimIndent(),
                )
                // 과거 예약 1건, 미래 예약 1건 삽입 (mail_id는 FK 없음)
                stmt.execute(
                    "INSERT INTO mail_reservation (mail_id, reservation_time) VALUES (1, DATE_SUB(UTC_TIMESTAMP(), INTERVAL 1 HOUR))",
                )
                stmt.execute(
                    "INSERT INTO mail_reservation (mail_id, reservation_time) VALUES (1, DATE_ADD(UTC_TIMESTAMP(), INTERVAL 1 HOUR))",
                )
            }
        }

        // 2. Flyway 실행 (baseline-on-migrate: true, baseline-version: 1)
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
