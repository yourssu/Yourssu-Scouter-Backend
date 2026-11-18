package com.yourssu.scouter.common.configuration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.core.env.StandardEnvironment
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

/**
 * QA 전용 엔드포인트가 운영에 열리는 것을 막는다.
 * `@Profile`을 빠뜨린 새 `/internal/dev` 컨트롤러는 prod 프로필에서 그대로 등록되고,
 * 그 사실은 배포 후 Swagger를 열어 보기 전까지 드러나지 않는다.
 */
class DevOnlyEndpointProfileTest {

    private val basePackage = "com.yourssu.scouter"
    private val devPathPrefix = "/internal/dev"
    private val prodProfile = "prod"

    @Test
    fun `internal dev 엔드포인트를 가진 컨트롤러는 prod 프로필에서 제외된다`() {
        val devControllers = scanControllers().filter { it.mappedPaths().any { path -> path.startsWith(devPathPrefix) } }

        assertThat(devControllers)
            .describedAs("'$devPathPrefix' 경로를 가진 컨트롤러를 찾지 못했다. 경로 규칙이 바뀌었다면 이 테스트를 함께 고쳐야 한다.")
            .isNotEmpty()

        val exposedInProd = devControllers.filter { controller ->
            val profile = AnnotatedElementUtils.findMergedAnnotation(controller, Profile::class.java)
            profile == null || prodProfile in profile.value
        }

        assertThat(exposedInProd.map { it.name })
            .describedAs("prod에 노출되는 QA 전용 컨트롤러. @Profile(\"local\", \"dev\")를 붙여야 한다.")
            .isEmpty()
    }

    private fun scanControllers(): List<Class<*>> {
        val scanner = ClassPathScanningCandidateComponentProvider(false)
        scanner.addIncludeFilter(AnnotationTypeFilter(Controller::class.java))
        // 스캐너는 @Profile을 조건으로 평가해 후보에서 빼버린다. 검사 대상을 전부 보려면 모든 프로필을 켠다.
        scanner.setEnvironment(StandardEnvironment().apply { setActiveProfiles("local", "dev", prodProfile) })

        return scanner.findCandidateComponents(basePackage)
            .mapNotNull { it.beanClassName }
            .map { Class.forName(it) }
    }

    private fun Class<*>.mappedPaths(): List<String> {
        val classPaths = AnnotatedElementUtils.findMergedAnnotation(this, RequestMapping::class.java)
            ?.value
            ?.toList()
            .orEmpty()
            .ifEmpty { listOf("") }

        val methodPaths = declaredMethods
            .flatMap { AnnotatedElementUtils.findMergedAnnotation(it, RequestMapping::class.java)?.value?.toList().orEmpty() }
            .ifEmpty { listOf("") }

        return classPaths.flatMap { classPath -> methodPaths.map { methodPath -> classPath + methodPath } }
    }
}
