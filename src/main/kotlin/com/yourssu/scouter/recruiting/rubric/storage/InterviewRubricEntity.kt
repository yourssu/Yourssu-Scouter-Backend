package com.yourssu.scouter.recruiting.rubric.storage

import com.yourssu.scouter.common.part.implement.Part
import com.yourssu.scouter.common.part.storage.PartEntity
import com.yourssu.scouter.recruiting.evaluation.storage.InterviewEvaluationItemEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "interview_rubric",
    uniqueConstraints = [UniqueConstraint(name = "uk_interview_rubric_part_semester", columnNames = ["part_id", "semester"])],
)
class InterviewRubricEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false)
    var semester: String,

    @Column(nullable = false)
    var deadline: LocalDateTime,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false)
    var part: PartEntity,

    @Column(name = "is_locked", nullable = false)
    var isLocked: Boolean = false,

    @Column(name = "interviewer_count", nullable = false)
    var interviewerCount: Int,

    @OneToMany(mappedBy = "interviewRubric", cascade = [CascadeType.ALL], orphanRemoval = true)
    val items: MutableList<InterviewEvaluationItemEntity> = mutableListOf()
) {
    fun lock() {
        this.isLocked = true
    }

    fun addItem(item: InterviewEvaluationItemEntity) {
        items.add(item)
        item.interviewRubric = this
    }
}
