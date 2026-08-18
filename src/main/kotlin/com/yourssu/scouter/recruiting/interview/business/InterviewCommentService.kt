package com.yourssu.scouter.recruiting.interview.business

import com.yourssu.scouter.auth.user.implement.User
import com.yourssu.scouter.auth.user.implement.UserReader
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantReader
import com.yourssu.scouter.recruiting.comment.business.dto.CreateDocumentCommentCommand
import com.yourssu.scouter.recruiting.comment.business.dto.DocumentCommentAuthorDto
import com.yourssu.scouter.recruiting.comment.business.dto.DocumentCommentDto
import com.yourssu.scouter.recruiting.comment.implement.CommentCategory
import com.yourssu.scouter.recruiting.comment.implement.DocumentComment
import com.yourssu.scouter.recruiting.comment.implement.DocumentCommentReader
import com.yourssu.scouter.recruiting.comment.implement.DocumentCommentWriter
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionReader
import com.yourssu.scouter.recruiting.support.business.EvaluatorDirectory
import com.yourssu.scouter.recruiting.support.implement.exception.AssignedQuestionNotFoundException
import com.yourssu.scouter.recruiting.support.implement.exception.DocumentCommentAccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InterviewCommentService(
    private val documentCommentReader: DocumentCommentReader,
    private val documentCommentWriter: DocumentCommentWriter,
    private val applicantReader: ApplicantReader,
    private val assignedQuestionReader: AssignedQuestionReader,
    private val userReader: UserReader,
    private val evaluatorDirectory: EvaluatorDirectory,
) {

    @Transactional
    fun create(command: CreateDocumentCommentCommand): DocumentCommentDto {
        require(command.content.isNotBlank()) { "코멘트 내용은 비어있을 수 없습니다." }

        applicantReader.readById(command.applicantId)
        val questionExists = assignedQuestionReader.readAllByApplicantId(command.applicantId)
            .any { it.id == command.sectionId }
        if (!questionExists) {
            throw AssignedQuestionNotFoundException("해당 면접자에게 존재하지 않는 질문입니다: ${command.sectionId}")
        }

        val saved = documentCommentWriter.write(
            DocumentComment(
                applicantId = command.applicantId,
                sectionId = command.sectionId,
                authorUserId = command.authorUserId,
                content = command.content,
                parentCommentId = null,
                category = CommentCategory.INTERVIEW,
                isEdited = false,
            ),
        )

        return DocumentCommentDto.of(saved, toAuthorDto(command.authorUserId))
    }

    fun readAllByApplicantId(applicantId: Long): List<DocumentCommentDto> {
        applicantReader.readById(applicantId)

        val comments = documentCommentReader.readAllByApplicantIdAndCategory(applicantId, CommentCategory.INTERVIEW)
        val authors = comments.map { it.authorUserId }.distinct().associateWith { toAuthorDto(it) }

        return comments.map { comment -> DocumentCommentDto.of(comment, authors.getValue(comment.authorUserId)) }
    }

    @Transactional
    fun deleteById(commentId: Long, requesterUserId: Long) {
        val comment = documentCommentReader.readById(commentId)
        if (comment.category != CommentCategory.INTERVIEW) {
            throw DocumentCommentAccessDeniedException("면접 코멘트만 삭제할 수 있습니다.")
        }
        if (comment.authorUserId != requesterUserId) {
            throw DocumentCommentAccessDeniedException("본인이 작성한 코멘트만 삭제할 수 있습니다.")
        }

        documentCommentWriter.delete(commentId)
    }

    private fun toAuthorDto(userId: Long): DocumentCommentAuthorDto {
        val user: User = userReader.readById(userId)
        val info = evaluatorDirectory.findEvaluatorInfo(user.userInfo.email)

        return DocumentCommentAuthorDto(
            memberId = info?.memberId,
            nickname = info?.nicknameEnglish ?: user.userInfo.name,
            part = info?.partName ?: "",
        )
    }
}
