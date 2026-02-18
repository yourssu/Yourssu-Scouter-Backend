package com.yourssu.scouter.ats.application.domain.applicant

import com.yourssu.scouter.ats.business.domain.applicant.ApplicantDto
import com.yourssu.scouter.ats.business.domain.applicant.ApplicantService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@Tag(name = "리크루팅 지원자")
@RestController
class ApplicantController(
    private val applicantService: ApplicantService,
) {

    @Operation(summary = "지원자 추가 API")
    @ApiResponse(
        description = "CREATED", responseCode = "201", headers = [
            Header(name = "Location", description = "/applicants/{applicantId}")
        ]
    )
    @PostMapping("/applicants")
    fun create(
        @RequestBody @Valid request: CreateApplicantRequest,
    ): ResponseEntity<Unit> {
        val command = request.toCommand()
        val applicantId: Long = applicantService.create(command)

        return ResponseEntity.created(URI.create("/applicants/$applicantId")).build()
    }


    @Operation(
        summary = "지원자 목록 조회",
        description = "지원자 목록을 검색, 필터링을 통해 얻습니다. 필터링에 필요한 정보는 각 api에서 얻어야 합니다."
    )
    @GetMapping("/applicants")
    fun readAll(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) state: String?,
        @RequestParam(required = false) semesterId: Long?,
        @RequestParam(required = false) partId: Long?,
    ): ResponseEntity<List<ReadApplicantResponse>> {
        val applicantDtos: List<ApplicantDto> = applicantService.readAllByFilters(
            name = name,
            state = state,
            semesterId = semesterId,
            partId = partId,
        )
        val responses: List<ReadApplicantResponse> = applicantDtos.map { ReadApplicantResponse.from(it) }
        return ResponseEntity.ok(responses)
    }

    @Operation(summary = "지원자 정보 수정", description = "변경할 지원자 정보의 값을 보냅니다. 변경되지 않은 값은 보내면 안 됩니다.")
    @PatchMapping("/applicants/{applicantId}")
    fun updateById(
        @PathVariable applicantId: Long,
        @RequestBody @Valid request: UpdateApplicantRequest,
    ): ResponseEntity<Unit> {
        val command = request.toCommand(applicantId)
        applicantService.updateById(command)

        return ResponseEntity.ok().build()
    }

    @Operation(summary = "지원자 단일 조회", description = "지원자 목록 조회에서 얻은 applicantId를 이용해 지원자의 세부정보를 확인합니다.")
    @GetMapping("/applicants/{applicantId}")
    fun readById(
        @PathVariable applicantId: Long,
    ): ResponseEntity<ReadApplicantResponse> {
        val applicantDto: ApplicantDto = applicantService.readById(applicantId)
        val response = ReadApplicantResponse.from(applicantDto)

        return ResponseEntity.ok(response)
    }

    @Operation(summary = "지원자 삭제", description = "지원자 목록 조회에서 얻은 applicantId를 이용해 지원자를 삭제합니다.")
    @ApiResponse(description = "NO_CONTENT", responseCode = "204")
    @DeleteMapping("/applicants/{applicantId}")
    fun deleteById(
        @PathVariable applicantId: Long,
    ): ResponseEntity<Unit> {
        applicantService.deleteById(applicantId)

        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "지원자 상태 목록 조회", description = "전체 지원자의 가능한 상태 목록을 확인합니다. 현재 지원자와는 관련이 없습니다.")
    @ApiResponse(
        description = "OK", responseCode = "200", content = [Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = String::class)),
            examples =
                [ExampleObject(value = "[ \"심사 진행 중\", \"서류 불합\", \"면접 불합\", \"인큐베이팅 불합\", \"최종 합격\" ]")]
        )]
    )
    @GetMapping("/applicants/states")
    fun readAllMemberStates(): ResponseEntity<List<String>> {
        val states: List<String> = applicantService.readAllStates()

        return ResponseEntity.ok(states)
    }
}
