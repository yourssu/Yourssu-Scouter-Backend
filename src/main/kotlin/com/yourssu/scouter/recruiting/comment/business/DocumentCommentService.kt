package com.yourssu.scouter.recruiting.comment.business

import com.yourssu.scouter.recruiting.comment.business.dto.DocumentCommentAuthorDto

import com.yourssu.scouter.recruiting.comment.business.dto.CreateDocumentCommentCommand

import com.yourssu.scouter.recruiting.comment.business.dto.DocumentCommentDto

import com.yourssu.scouter.recruiting.comment.business.dto.UpdateDocumentCommentCommand

import com.yourssu.scouter.recruiting.applicant.implement.ApplicantReader
import com.yourssu.scouter.auth.user.implement.User
import com.yourssu.scouter.auth.user.implement.UserReader
import com.yourssu.scouter.recruiting.comment.implement.DocumentComment
import com.yourssu.scouter.recruiting.comment.implement.DocumentCommentReader
import com.yourssu.scouter.recruiting.comment.implement.DocumentCommentWriter
import com.yourssu.scouter.recruiting.rubric.implement.DocumentSectionReader
import com.yourssu.scouter.recruiting.support.implement.exception.DocumentCommentAccessDeniedException
import com.yourssu.scouter.recruiting.support.implement.exception.DocumentCommentReplyDepthExceededException
import com.yourssu.scouter.recruiting.support.implement.exception.DocumentCommentSectionMismatchException
import com.yourssu.scouter.recruiting.support.business.EvaluatorDirectory
import com.yourssu.scouter.recruiting.support.implement.exception.DocumentSectionNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import com.yourssu.scouter.recruiting.comment.implement.CommentCategory

@Service
class DocumentCommentService(
    private val documentCommentReader: DocumentCommentReader,
    private val documentCommentWriter: DocumentCommentWriter,
    private val documentSectionReader: DocumentSectionReader,
    private val applicantReader: ApplicantReader,
    private val userReader: UserReader,
    private val evaluatorDirectory: EvaluatorDirectory,
) {

    @Transactional
    fun create(command: CreateDocumentCommentCommand): DocumentCommentDto {
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
    fun update(command: UpdateDocumentCommentCommand): DocumentCommentDto {
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

    fun readAllByApplicantId(applicantId: Long): List<DocumentCommentDto> {
        applicantReader.readById(applicantId)

        val comments = documentCommentReader.readAllByApplicantIdAndCategory(applicantId, CommentCategory.DOCUMENT)
        val authors = comments.map { it.authorUserId }.distinct().associateWith { toAuthorDto(it) }

        return comments.map { comment -> DocumentCommentDto.of(comment, authors.getValue(comment.authorUserId)) }
    }

    @Transactional
    fun deleteById(commentId: Long, requesterUserId: Long) {
        val comment = documentCommentReader.readById(commentId)
        if (comment.category != CommentCategory.DOCUMENT) {
            throw DocumentCommentAccessDeniedException("서류 코멘트만 삭제할 수 있습니다.")
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
            userId = userId,
            nickname = info?.nicknameEnglish ?: user.userInfo.name,
            part = info?.partName ?: "",
        )
    }
}
