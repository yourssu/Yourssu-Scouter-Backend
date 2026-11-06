package com.yourssu.scouter.recruiting.comment.business

import com.yourssu.scouter.auth.user.implement.User
import com.yourssu.scouter.auth.user.implement.UserReader
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantReader
import com.yourssu.scouter.recruiting.comment.business.dto.CreateDocumentCommentCommand
import com.yourssu.scouter.recruiting.comment.business.dto.DocumentCommentAuthorDto
import com.yourssu.scouter.recruiting.comment.business.dto.DocumentCommentDto
import com.yourssu.scouter.recruiting.comment.business.dto.UpdateDocumentCommentCommand
import com.yourssu.scouter.recruiting.comment.implement.CommentCategory
import com.yourssu.scouter.recruiting.comment.implement.DocumentComment
import com.yourssu.scouter.recruiting.comment.implement.DocumentCommentReader
import com.yourssu.scouter.recruiting.comment.implement.DocumentCommentWriter
import com.yourssu.scouter.recruiting.comment.business.dto.QuestionInterviewCommentsDto
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionReader
import com.yourssu.scouter.recruiting.rubric.implement.DocumentSectionReader
import com.yourssu.scouter.recruiting.support.business.EvaluatorDirectory
import com.yourssu.scouter.recruiting.support.implement.exception.AssignedQuestionNotFoundException
import com.yourssu.scouter.recruiting.support.implement.exception.DocumentCommentAccessDeniedException
import com.yourssu.scouter.recruiting.support.implement.exception.DocumentCommentReplyDepthExceededException
import com.yourssu.scouter.recruiting.support.implement.exception.DocumentCommentSectionMismatchException
import com.yourssu.scouter.recruiting.support.implement.exception.DocumentSectionNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentService(
    private val documentCommentReader: DocumentCommentReader,
    private val documentCommentWriter: DocumentCommentWriter,
    private val documentSectionReader: DocumentSectionReader,
    private val assignedQuestionReader: AssignedQuestionReader,
    private val applicantReader: ApplicantReader,
    private val userReader: UserReader,
    private val evaluatorDirectory: EvaluatorDirectory,
) {

    @Transactional
    fun createDocumentComment(command: CreateDocumentCommentCommand): DocumentCommentDto {
        require(command.content.isNotBlank()) { "코멘트 내용은 비어있을 수 없습니다." }

        val applicant = applicantReader.readById(command.applicantId)
        val sectionExists = documentSectionReader.readAllByPartId(applicant.part.id!!)
            .any { it.id == command.sectionId }
        if (!sectionExists) {
            throw DocumentSectionNotFoundException("해당 파트에 존재하지 않는 문항입니다: ${command.sectionId}")
        }

        if (command.parentCommentId != null) {
            val parent = documentCommentReader.readById(command.parentCommentId)
            if (parent.category != CommentCategory.DOCUMENT) {
                throw DocumentCommentSectionMismatchException("서류 코멘트에만 답글을 작성할 수 있습니다.")
            }
            if (parent.isReply) {
                throw DocumentCommentReplyDepthExceededException("답글에는 다시 답글을 달 수 없습니다.")
            }
            if (parent.sectionId != command.sectionId) {
                throw DocumentCommentSectionMismatchException("부모 코멘트와 sectionId가 일치하지 않습니다.")
            }
        }

        val saved = documentCommentWriter.write(
            DocumentComment(
                applicantId = command.applicantId,
                sectionId = command.sectionId,
                authorUserId = command.authorUserId,
                content = command.content,
                parentCommentId = command.parentCommentId,
                category = CommentCategory.DOCUMENT,
            ),
        )

        return DocumentCommentDto.of(saved, toAuthorDto(command.authorUserId))
    }

    @Transactional
    fun createInterviewComment(command: CreateDocumentCommentCommand): DocumentCommentDto {
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
            ),
        )

        return DocumentCommentDto.of(saved, toAuthorDto(command.authorUserId))
    }

    @Transactional
    fun updateDocumentComment(command: UpdateDocumentCommentCommand): DocumentCommentDto {
        require(command.content.isNotBlank()) { "코멘트 내용은 비어있을 수 없습니다." }

        val comment = documentCommentReader.readById(command.commentId)
        if (comment.category != CommentCategory.DOCUMENT) {
            throw DocumentCommentAccessDeniedException("서류 코멘트만 수정할 수 있습니다.")
        }
        if (comment.authorUserId != command.requesterUserId) {
            throw DocumentCommentAccessDeniedException("본인이 작성한 코멘트만 수정할 수 있습니다.")
        }

        val updated = documentCommentWriter.update(comment.edit(command.content))

        return DocumentCommentDto.of(updated, toAuthorDto(updated.authorUserId))
    }

    fun readAllDocumentCommentsByApplicantId(applicantId: Long): List<DocumentCommentDto> {
        applicantReader.readById(applicantId)

        return readCommentDtos(applicantId, CommentCategory.DOCUMENT)
    }

    fun readAllInterviewCommentsByApplicantId(applicantId: Long): List<QuestionInterviewCommentsDto> {
        applicantReader.readById(applicantId)

        val assignedQuestions = assignedQuestionReader.readAllByApplicantId(applicantId)
            .sortedBy { it.sortOrder }
        val commentsBySectionId = readCommentDtos(applicantId, CommentCategory.INTERVIEW)
            .groupBy { it.sectionId }

        return assignedQuestions.map { question ->
            QuestionInterviewCommentsDto(
                sectionId = question.id!!,
                comments = commentsBySectionId[question.id].orEmpty(),
            )
        }
    }

    @Transactional
    fun deleteDocumentCommentById(commentId: Long, requesterUserId: Long) {
        deleteByIdAndCategory(commentId, requesterUserId, CommentCategory.DOCUMENT, "서류 코멘트만 삭제할 수 있습니다.")
    }

    @Transactional
    fun deleteInterviewCommentById(commentId: Long, requesterUserId: Long) {
        deleteByIdAndCategory(commentId, requesterUserId, CommentCategory.INTERVIEW, "면접 코멘트만 삭제할 수 있습니다.")
    }

    private fun deleteByIdAndCategory(
        commentId: Long,
        requesterUserId: Long,
        category: CommentCategory,
        categoryMismatchMessage: String,
    ) {
        val comment = documentCommentReader.readById(commentId)
        if (comment.category != category) {
            throw DocumentCommentAccessDeniedException(categoryMismatchMessage)
        }
        if (comment.authorUserId != requesterUserId) {
            throw DocumentCommentAccessDeniedException("본인이 작성한 코멘트만 삭제할 수 있습니다.")
        }

        documentCommentWriter.delete(commentId)
    }

    private fun readCommentDtos(applicantId: Long, category: CommentCategory): List<DocumentCommentDto> {
        val comments = documentCommentReader.readAllByApplicantIdAndCategory(applicantId, category)
        val authors = comments.map { it.authorUserId }.distinct().associateWith { toAuthorDto(it) }

        return comments.map { comment -> DocumentCommentDto.of(comment, authors.getValue(comment.authorUserId)) }
    }

    private fun toAuthorDto(userId: Long): DocumentCommentAuthorDto {
        val user: User = userReader.readById(userId)
        val info = evaluatorDirectory.findEvaluatorInfo(user.userInfo.email)

        return DocumentCommentAuthorDto(
            userId = userId,
            nickname = info?.nicknameEnglish ?: user.userInfo.name,
            part = info?.partName ?: "",
        )
    }
}
