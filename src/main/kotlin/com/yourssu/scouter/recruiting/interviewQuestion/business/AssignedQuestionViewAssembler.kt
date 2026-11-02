package com.yourssu.scouter.recruiting.interviewQuestion.business

import com.yourssu.scouter.member.core.implement.MemberReader
import com.yourssu.scouter.recruiting.applicant.implement.Applicant
import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.AssignedQuestionDto
import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.AssignedQuestionsDto
import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.QuestionRequirementDto
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestion
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionCategory
import com.yourssu.scouter.recruiting.interviewQuestion.implement.Question
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionCategory
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionReader
import com.yourssu.scouter.recruiting.support.business.InterviewRequirementLookup
import com.yourssu.scouter.recruiting.support.business.InterviewRequirementProfile
import org.springframework.stereotype.Component

/**
 * 지원자별 면접 질문 응답 DTO를 조립한다.
 */
@Component
class AssignedQuestionViewAssembler(
    private val questionReader: QuestionReader,
    private val memberReader: MemberReader,
    private val interviewRequirementLookup: InterviewRequirementLookup,
) {

    fun assembleSaved(
        applicant: Applicant,
        questions: List<AssignedQuestion>,
        selectedCultureQuestionIds: Set<Long>,
    ): AssignedQuestionsDto {
        val requirementsById = readRequirementsById(applicant)
        val sourceQuestionsById = readSourceQuestionsById(questions)
        val assignedMemberNamesById = readAssignedMemberNamesById(questions)

        val savedDtos = questions
            .sortedBy { it.sortOrder }
            .map { question ->
                question.toDto(sourceQuestionsById, requirementsById, assignedMemberNamesById, selectedCultureQuestionIds)
            }

        val unsavedCatalogDtos = readUnsavedCatalogQuestions(
            applicant.part.id!!,
            applicant.applicationSemester.id!!,
            savedDtos,
            requirementsById,
            selectedCultureQuestionIds,
        )

        return AssignedQuestionsDto(questions = savedDtos + unsavedCatalogDtos)
    }

    /** 이 지원자가 한 번도 저장한 적 없을 때 보여줄, 카탈로그만으로 구성된 기본 질문지. */
    fun assembleDefaults(applicant: Applicant, selectedCultureQuestionIds: Set<Long>): AssignedQuestionsDto {
        val requirementsById = readRequirementsById(applicant)
        val applicantSemesterId = applicant.applicationSemester.id!!

        // INTRO / OUTRO : 학기에 무관하게 전체 공유
        val introOutroQuestions = questionReader
            .readAll()
            .filter { it.partId == null && it.category in setOf(QuestionCategory.INTRO, QuestionCategory.OUTRO) }

        // CULTURE / PART : 지원자의 학기에 귀속된 질문만 조회
        val cultureQuestions = questionReader
            .readAllBySemesterId(applicantSemesterId)
            .filter { it.partId == null && it.category == QuestionCategory.CULTURE }
        val partQuestions = questionReader.readAllByPartIdAndSemesterId(applicant.part.id!!, applicantSemesterId)

        val catalogQuestions = introOutroQuestions + cultureQuestions + partQuestions

        return AssignedQuestionsDto(
            questions = catalogQuestions.mapIndexed { index, question ->
                question.toCatalogDto(index, requirementsById, selectedCultureQuestionIds)
            },
        )
    }

    /**
     * CULTURE/PART는 개별 지원자가 저장한 적 없어도(= 한 번도 선택/배정될 필요가 없었던 문항이라
     * assignedMemberId를 정할 필요가 없었던 경우) 그 파트+학기의 카탈로그 문항 전체가 보여야 한다.
     * 아직 이 지원자에게 인스턴스로 저장되지 않은 문항을 조회 시 카탈로그에서 보충해서 채워준다.
     * (다른 지원자의 저장으로 새로 생긴 PART/CULTURE 카탈로그 문항도 이 방식으로 즉시 반영된다.)
     */
    private fun readUnsavedCatalogQuestions(
        applicantPartId: Long,
        applicantSemesterId: Long,
        savedDtos: List<AssignedQuestionDto>,
        requirementsById: Map<Long?, InterviewRequirementProfile>,
        selectedCultureQuestionIds: Set<Long>,
    ): List<AssignedQuestionDto> {
        val savedSourceIdsByCategory = savedDtos
            .filter { it.category == AssignedQuestionCategory.CULTURE || it.category == AssignedQuestionCategory.PART }
            .groupBy({ it.category }) { it.sourceQuestionId }
            .mapValues { (_, ids) -> ids.toSet() }
        val savedCultureSourceIds = savedSourceIdsByCategory[AssignedQuestionCategory.CULTURE].orEmpty()
        val savedPartSourceIds = savedSourceIdsByCategory[AssignedQuestionCategory.PART].orEmpty()

        val unsavedCultureQuestions = questionReader
            .readAllBySemesterId(applicantSemesterId)
            .filter {
                it.partId == null && it.category == QuestionCategory.CULTURE && (it.id == null || it.id !in savedCultureSourceIds)
            }
        val unsavedPartQuestions = questionReader
            .readAllByPartIdAndSemesterId(applicantPartId, applicantSemesterId)
            .filter { it.id == null || it.id !in savedPartSourceIds }

        val unsavedCatalogQuestions = unsavedCultureQuestions + unsavedPartQuestions
        if (unsavedCatalogQuestions.isEmpty()) return emptyList()

        val startSortOrder = (savedDtos.maxOfOrNull { it.sortOrder } ?: -1) + 1

        return unsavedCatalogQuestions.mapIndexed { offset, question ->
            question.toCatalogDto(startSortOrder + offset, requirementsById, selectedCultureQuestionIds)
        }
    }

    private fun readSourceQuestionsById(questions: List<AssignedQuestion>): Map<Long?, Question> =
        questionReader
            .readAllByIdIn(questions.mapNotNull { it.sourceQuestionId })
            .associateBy { it.id }

    private fun readRequirementsById(applicant: Applicant): Map<Long?, InterviewRequirementProfile> =
        interviewRequirementLookup
            .findAllByPartIdAndSemester(applicant.part.id!!, applicant.applicationSemester)
            .associateBy { it.id }

    private fun readAssignedMemberNamesById(questions: List<AssignedQuestion>): Map<Long, String> =
        questions
            .mapNotNull { it.assignedMemberId }
            .distinct()
            .mapNotNull { memberId ->
                val member = runCatching { memberReader.readById(memberId) }.getOrNull() ?: return@mapNotNull null
                memberId to member.nicknameEnglish.ifBlank { member.name }
            }.toMap()

    /** 아직 인스턴스로 저장되지 않은 카탈로그 문항을 응답에 그대로 노출할 때 쓰는 변환. */
    private fun Question.toCatalogDto(
        sortOrder: Int,
        requirementsById: Map<Long?, InterviewRequirementProfile>,
        selectedCultureQuestionIds: Set<Long>,
    ): AssignedQuestionDto =
        AssignedQuestionDto(
            id = null,
            assignedMemberId = null,
            assignedMemberName = null,
            sourceQuestionId = id,
            content = content,
            category = category.toAssignedQuestionCategory(),
            sortOrder = sortOrder,
            isSelected = if (category == QuestionCategory.CULTURE) id != null && id in selectedCultureQuestionIds else null,
            requirements = requirementIds.toRequirementDtos(requirementsById),
        )

    private fun AssignedQuestion.toDto(
        sourceQuestionsById: Map<Long?, Question>,
        requirementsById: Map<Long?, InterviewRequirementProfile>,
        assignedMemberNamesById: Map<Long, String>,
        selectedCultureQuestionIds: Set<Long>,
    ): AssignedQuestionDto {
        // 카탈로그 카테고리(INTRO/OUTRO/CULTURE/PART)는 요구조건을 원본 질문(Question)에서 가져오고, PERSONAL만 인스턴스 자체 값을 사용한다.
        val effectiveRequirementIds = if (category == AssignedQuestionCategory.PERSONAL) {
            requirementIds
        } else {
            sourceQuestionsById[sourceQuestionId]?.requirementIds.orEmpty()
        }

        // CULTURE 선택 여부는 지원자 개인 값이 아닌 파트+학기 단위 공유 상태에서 가져온다.
        val effectiveIsSelected = if (category == AssignedQuestionCategory.CULTURE) {
            sourceQuestionId != null && sourceQuestionId in selectedCultureQuestionIds
        } else {
            isSelected
        }

        return AssignedQuestionDto(
            id = id!!,
            assignedMemberId = assignedMemberId,
            assignedMemberName = assignedMemberId?.let { assignedMemberNamesById[it] },
            sourceQuestionId = sourceQuestionId,
            content = content ?: sourceQuestionsById[sourceQuestionId]?.content.orEmpty(),
            category = category,
            sortOrder = sortOrder,
            isSelected = effectiveIsSelected,
            requirements = effectiveRequirementIds.toRequirementDtos(requirementsById),
        )
    }

    private fun List<Long>.toRequirementDtos(
        requirementsById: Map<Long?, InterviewRequirementProfile>,
    ): List<QuestionRequirementDto> =
        mapNotNull { requirementId ->
            requirementsById[requirementId]?.let { QuestionRequirementDto(it.id, it.content) }
        }

    private fun QuestionCategory.toAssignedQuestionCategory(): AssignedQuestionCategory =
        AssignedQuestionCategory.valueOf(name)
}
