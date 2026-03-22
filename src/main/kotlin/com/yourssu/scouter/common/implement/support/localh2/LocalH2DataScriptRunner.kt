package com.yourssu.scouter.common.implement.support.localh2

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import javax.sql.DataSource
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component

/**
 * 로컬 H2에서만, 앱 초기화(CommandLineRunner Order 1~4) 이후에 초기 데이터 스크립트를 실행한다.
 * MySQL 덤프는 문법 차이로 실패할 수 있으므로 [scripts/mysql-dump-for-local-h2.sh] 등으로 가공하는 것을 권장한다.
 */
@Component
@Profile("local")
@Order(Ordered.LOWEST_PRECEDENCE)
class LocalH2DataScriptRunner(
    private val dataSource: DataSource,
    private val properties: ScouterLocalH2DataProperties,
) : ApplicationRunner {

    companion object {
        private val log = LoggerFactory.getLogger(LocalH2DataScriptRunner::class.java)
    }

    override fun run(args: ApplicationArguments) {
        val raw = properties.initScript.trim()
        if (raw.isEmpty()) {
            return
        }

        dataSource.connection.use { conn ->
            val url = conn.metaData.url
            if (!url.contains("jdbc:h2", ignoreCase = true)) {
                log.debug("DataSource가 H2가 아니므로 local-h2-data 스크립트를 건너뜁니다. url={}", url)
                return
            }

            val runScriptPath =
                when {
                    raw.startsWith("classpath:") -> {
                        val sub = raw.removePrefix("classpath:").trimStart('/')
                        val resource = ClassPathResource(sub)
                        if (!resource.exists()) {
                            log.warn("classpath 리소스를 찾을 수 없습니다: {}", raw)
                            return
                        }
                        val copy = Files.createTempFile("scouter-h2-init-", ".sql")
                        resource.inputStream.use { input -> Files.copy(input, copy, StandardCopyOption.REPLACE_EXISTING) }
                        copy.toFile().deleteOnExit()
                        copy.toAbsolutePath().normalize()
                    }
                    else -> Path.of(raw).toAbsolutePath().normalize()
                }

            if (!Files.exists(runScriptPath)) {
                log.warn("scouter.local-h2-data.init-script 파일이 없습니다: {}", runScriptPath)
                return
            }

            val pathForH2 = runScriptPath.toString().replace('\\', '/').replace("'", "''")
            conn.createStatement().use { st ->
                st.execute("RUNSCRIPT FROM '$pathForH2' CHARSET 'UTF-8'")
            }
            log.info("로컬 H2 초기 데이터 스크립트 실행 완료: {}", runScriptPath)
        }
    }
}
