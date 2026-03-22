package com.yourssu.scouter.common.implement.support.localh2

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 로컬 프로필 + H2일 때만 [LocalH2DataScriptRunner]가 사용한다.
 * [initScript]가 비어 있으면 아무 것도 하지 않는다.
 */
@ConfigurationProperties(prefix = "scouter.local-h2-data")
data class ScouterLocalH2DataProperties(
    /**
     * H2 [RUNSCRIPT]로 실행할 파일 경로(프로젝트 기준 상대 경로 권장) 또는 `classpath:경로` (예: `classpath:local-h2-seed.sql`).
     * 환경변수 [SCOUTER_LOCAL_H2_INIT_SCRIPT]로 덮어쓸 수 있다.
     */
    val initScript: String = "",
)
