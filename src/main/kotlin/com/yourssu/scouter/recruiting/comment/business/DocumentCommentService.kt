package com.yourssu.scouter.recruiting.comment.business

import com.yourssu.scouter.recruiting.applicant.implement.ApplicantReader
import com.yourssu.scouter.auth.user.implement.User
import com.yourssu.scouter.auth.user.implement.UserReader
import com.yourssu.scouter.recruiting.comment.implement.DocumentComment
import com.yourssu.scouter.recruiting.comment.implement.DocumentCommentReader
import com.yourssu.scouter.recruiting.comment.implement.DocumentCommentWriter
import com.yourssu.scouter.recruiting.rubric.implement.DocumentSectionReader
import com.yourssu.scouter.recruiting.support.implement.exception.DocumentCommentAccessDeniedException
import com.yourssu.scouter.recruiting.support.implement.exception.DocumentSectionNotFoundException
import com.yourssu.scouter.hrms.member.implement.MemberReader
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DocumentCommentService(
    private val documentCommentReader: DocumentCommentReader,
    private val documentCommentWriter: DocumentCommentWriter,
    private val documentSectionReader: DocumentSectionReader,
    private val applicantReader: ApplicantReader,
    private val userReader: UserReader,
    private val memberReader: MemberReader,
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

        val saved = documentCommentWriter.write(
            DocumentComment(
                applicantId = command.applicantId,
                sectionId = command.sectionId,
                authorUserId = command.authorUserId,
                content = command.content,
            ),
        )

        return DocumentCommentDto.of(saved, toAuthorDto(command.authorUserId))
    }

    fun readAllByApplicantId(applicantId: Long): List<DocumentCommentDto> {
        applicantReader.readById(applicantId)

        val comments = documentCommentReader.readAllByApplicantId(applicantId)
        val authors = comments.map { it.authorUserId }.distinct().associateWith { toAuthorDto(it) }

        return comments.map { comment -> DocumentCommentDto.of(comment, authors.getValue(comment.authorUserId)) }
    }

    @Transactional
    fun deleteById(commentId: Long, requesterUserId: Long) {
        val comment = documentCommentReader.readById(commentId)
        if (comment.authorUserId != requesterUserId) {
            throw DocumentCommentAccessDeniedException("본인이 작성한 코멘트만 삭제할 수 있습니다.")
        }

        documentCommentWriter.delete(commentId)
    }

    private fun toAuthorDto(userId: Long): DocumentCommentAuthorDto {
        val user: User = userReader.readById(userId)
        val member = memberReader.readAllActive().find { it.member.email == user.userInfo.email }

        return DocumentCommentAuthorDto(
            userId = userId,
            nickname = member?.member?.nicknameEnglish ?: user.userInfo.name,
            part = member?.member?.parts?.firstOrNull()?.name ?: "",
        )
    }
}
