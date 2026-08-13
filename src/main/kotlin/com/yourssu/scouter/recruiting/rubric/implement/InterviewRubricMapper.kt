package com.yourssu.scouter.recruiting.rubric.implement

import com.yourssu.scouter.masterdata.part.storage.PartEntity
import com.yourssu.scouter.masterdata.semester.storage.SemesterEntity
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewEvaluationItem
import com.yourssu.scouter.recruiting.evaluation.storage.InterviewEvaluationItemEntity
import com.yourssu.scouter.recruiting.rubric.storage.InterviewRubricEntity
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.collections.map

@Component
class InterviewRubricMapper {

    fun toDomain(entity: InterviewRubricEntity): InterviewRubric {
        val partId = entity.part.id
            ?: throw IllegalStateException("Rubric에 연관된 PartEntity의 ID가 null일 수 없습니다. rubricId=${entity.id}")
        return InterviewRubric(
            id = entity.id,
            partId = partId,
            semester = entity.semester.toDomain(),
            deadline = entity.deadline.toInstant(ZoneOffset.UTC),
            isLocked = entity.isLocked,
            items = entity.items.map { itemEntity ->
                InterviewEvaluationItem(
                    id = itemEntity.id,
                    interviewRequirementId = itemEntity.interviewRequirementId,
                    keyword = itemEntity.keyword,
                    rubricType = itemEntity.rubricType,
                    maxScore = itemEntity.maxScore
                )
            }
        )
    }

    fun toEntity(domain: InterviewRubric, partEntity: PartEntity): InterviewRubricEntity {
        val entity = InterviewRubricEntity(
            id = domain.id ?: 0L,
            semester = SemesterEntity.from(domain.semester),
            deadline = LocalDateTime.ofInstant(domain.deadline, ZoneOffset.UTC), // Instant -> LocalDateTime
            part = partEntity,
            isLocked = domain.isLocked
        )

        domain.items.forEach { itemDomain ->
            val itemEntity = InterviewEvaluationItemEntity(
                id = itemDomain.id ?: 0L,
                interviewRubric = entity,
                interviewRequirementId = itemDomain.interviewRequirementId,
                keyword = itemDomain.keyword,
                rubricType = itemDomain.rubricType,
                maxScore = itemDomain.maxScore
            )
            entity.addItem(itemEntity)
        }

        return entity
    }
}
