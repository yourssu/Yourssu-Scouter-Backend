package com.yourssu.scouter.hrms.implement.domain.member.export

import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.hrms.business.support.utils.MemberStateConverter
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import com.yourssu.scouter.hrms.implement.domain.member.WithdrawnMember
import com.yourssu.scouter.hrms.implement.domain.member.parser.ColumnNumberMapping
import com.yourssu.scouter.hrms.implement.domain.member.parser.MemberPartRoleResolver
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * 인포시트 업로드 파서([ColumnNumberMapping], 시트 한글명)와 맞는 멤버 전체 엑셀을 만든다.
 */
@Component
class MemberInfoExcelWorkbookExporter(
    private val memberReader: MemberReader,
    private val memberPartRoleResolver: MemberPartRoleResolver,
) {

    private val birthDisplayFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.M.d")
    private val joinDisplayFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("yy.MM.dd")

    fun buildWorkbook(): XSSFWorkbook {
        val workbook = XSSFWorkbook()
        writeActiveSheet(workbook)
        writeInactiveSheet(workbook)
        writeCompletedSheet(workbook)
        writeGraduatedSheet(workbook)
        writeWithdrawnSheet(workbook)
        return workbook
    }

    private fun writeActiveSheet(workbook: XSSFWorkbook) {
        val sheet = workbook.createSheet(MemberStateConverter.convertToString(MemberState.ACTIVE))
        val headers = listOf(
            "팀명",
            "파트/역할",
            "이름",
            "닉네임",
            "닉네임(발음)",
            "유어슈 이메일",
            "연락처",
            "전공",
            "생년월일",
            "학번",
            "가입일",
            "회비납부",
            "비고",
        )
        headerRow(sheet.createRow(0), headers)
        val m = ColumnNumberMapping.ACTIVE_MEMBER
        memberReader.readAllActive().sorted().forEachIndexed { i, am ->
            val row = sheet.createRow(i + 1)
            val member = am.member
            row.createTextCell(0, "-")
            row.createTextCell(m.partRoleName, partRoleCell(member))
            row.createTextCell(m.name, member.name)
            row.createTextCell(m.nickname, member.nicknameEnglish)
            row.createTextCell(m.pronunciation!!, member.nicknameKorean)
            row.createTextCell(m.email, member.email)
            row.createTextCell(m.phoneNumber, member.phoneNumber)
            row.createTextCell(m.departmentName, member.department.name)
            row.createTextCell(m.birthDate, member.birthDate.format(birthDisplayFmt))
            row.createTextCell(m.studentId, member.studentId)
            row.createTextCell(m.joinDate, member.joinDate.format(joinDisplayFmt))
            row.createTextCell(11, if (am.isMembershipFeePaid) "o" else "")
            row.createTextCell(m.note, member.note)
        }
    }

    private fun writeInactiveSheet(workbook: XSSFWorkbook) {
        val sheet = workbook.createSheet(MemberStateConverter.convertToString(MemberState.INACTIVE))
        val headers = listOf(
            "파트",
            "이름",
            "닉네임",
            "발음",
            "이메일",
            "연락처",
            "전공",
            "생년월일",
            "학번",
            "가입일",
            "사유",
            "활동 학기",
            "예정복귀 시기",
            "문자회신여부",
            "문자회신 희망시기",
            "비고",
        )
        headerRow(sheet.createRow(0), headers)
        val c = ColumnNumberMapping.INACTIVE_MEMBER
        memberReader.readAllInactive().sorted().forEachIndexed { i, im ->
            val row = sheet.createRow(i + 1)
            writeBasicMemberColumns(row, c, im.member, partRoleInColumn0 = true)
            row.createTextCell(10, im.reason.orEmpty())
            row.createTextCell(11, semesterRangeLabel(im.activePeriod.startSemester, im.activePeriod.endSemester))
            row.createTextCell(12, semesterShort(im.expectedReturnSemester))
            row.createTextCell(13, when (im.smsReplied) {
                true -> "o"
                false -> "x"
                null -> ""
            })
            row.createTextCell(14, im.smsReplyDesiredPeriod.orEmpty())
            row.createTextCell(c.note, im.member.note)
        }
    }

    private fun writeCompletedSheet(workbook: XSSFWorkbook) {
        val sheet = workbook.createSheet(MemberStateConverter.convertToString(MemberState.COMPLETED))
        val headers = listOf(
            "팀명",
            "파트/역할",
            "이름",
            "닉네임",
            "닉네임(발음)",
            "유어슈 이메일",
            "연락처",
            "전공",
            "생년월일",
            "학번",
            "가입일",
            "수료일자",
        )
        headerRow(sheet.createRow(0), headers)
        val c = ColumnNumberMapping.COMPLETED_MEMBER
        memberReader.readAllCompleted().sorted().forEachIndexed { i, cm ->
            val row = sheet.createRow(i + 1)
            val member = cm.member
            row.createTextCell(0, "-")
            row.createTextCell(c.partRoleName, partRoleCell(member))
            row.createTextCell(c.name, member.name)
            row.createTextCell(c.nickname, member.nicknameEnglish)
            row.createTextCell(c.pronunciation!!, member.nicknameKorean)
            row.createTextCell(c.email, member.email)
            row.createTextCell(c.phoneNumber, member.phoneNumber)
            row.createTextCell(c.departmentName, member.department.name)
            row.createTextCell(c.birthDate, member.birthDate.format(birthDisplayFmt))
            row.createTextCell(c.studentId, member.studentId)
            row.createTextCell(c.joinDate, member.joinDate.format(joinDisplayFmt))
            row.createTextCell(
                11,
                approximateTermEndDate(cm.activePeriod.endSemester).format(joinDisplayFmt),
            )
        }
    }

    private fun writeGraduatedSheet(workbook: XSSFWorkbook) {
        val sheet = workbook.createSheet(MemberStateConverter.convertToString(MemberState.GRADUATED))
        val headers = listOf(
            "파트",
            "이름",
            "닉네임",
            "닉네임(발음)",
            "연락처",
            "유어슈 이메일",
            "전공",
            "생년월일",
            "학번",
            "가입일",
            "비고",
            "졸업학기",
        )
        headerRow(sheet.createRow(0), headers)
        val c = ColumnNumberMapping.GRADUATED_MEMBER
        memberReader.readAllGraduated().sorted().forEachIndexed { i, gm ->
            val row = sheet.createRow(i + 1)
            val member = gm.member
            row.createTextCell(0, partRoleCell(member))
            row.createTextCell(c.name, member.name)
            row.createTextCell(c.nickname, member.nicknameEnglish)
            row.createTextCell(c.pronunciation!!, member.nicknameKorean)
            row.createTextCell(c.phoneNumber, member.phoneNumber)
            row.createTextCell(c.email, member.email)
            row.createTextCell(c.departmentName, member.department.name)
            row.createTextCell(c.birthDate, member.birthDate.format(birthDisplayFmt))
            row.createTextCell(c.studentId, member.studentId)
            row.createTextCell(c.joinDate, member.joinDate.format(joinDisplayFmt))
            row.createTextCell(c.note, member.note)
            val graduationSemester = gm.activePeriod.endSemester.next()
            row.createTextCell(11, semesterShort(graduationSemester))
        }
    }

    private fun writeWithdrawnSheet(workbook: XSSFWorkbook) {
        val sheet = workbook.createSheet(MemberStateConverter.convertToString(MemberState.WITHDRAWN))
        val headers = listOf(
            "이름",
            "닉네임(발음)",
            "부서(파트)",
            "탈퇴 일자",
            "비고",
        )
        headerRow(sheet.createRow(0), headers)
        memberReader.readAllWithdrawn().sorted().forEachIndexed { i, wm ->
            val row = sheet.createRow(i + 1)
            val m = wm.member
            row.createTextCell(0, m.name)
            row.createTextCell(1, withdrawnNicknameCell(m))
            row.createTextCell(2, m.parts.joinToString("/") { it.name }.ifBlank { "-" })
            row.createTextCell(3, (wm.withdrawnDate ?: m.joinDate).format(joinDisplayFmt))
            row.createTextCell(4, m.note)
        }
    }

    private fun writeBasicMemberColumns(
        row: Row,
        c: ColumnNumberMapping,
        member: Member,
        partRoleInColumn0: Boolean,
    ) {
        if (partRoleInColumn0) {
            row.createTextCell(0, partRoleCell(member))
        }
        row.createTextCell(c.name, member.name)
        row.createTextCell(c.nickname, member.nicknameEnglish)
        c.pronunciation?.let { row.createTextCell(it, member.nicknameKorean) }
        row.createTextCell(c.email, member.email)
        row.createTextCell(c.phoneNumber, member.phoneNumber)
        row.createTextCell(c.departmentName, member.department.name)
        row.createTextCell(c.birthDate, member.birthDate.format(birthDisplayFmt))
        row.createTextCell(c.studentId, member.studentId)
        row.createTextCell(c.joinDate, member.joinDate.format(joinDisplayFmt))
    }

    private fun partRoleCell(member: Member): String {
        val s = member.parts.joinToString("/") { part ->
            memberPartRoleResolver.resolveToString(part, member.role) ?: part.name
        }
        return s.ifBlank { "-" }
    }

    private fun withdrawnNicknameCell(member: Member): String =
        when {
            member.nicknameKorean.isNotBlank() ->
                "${member.nicknameEnglish}(${member.nicknameKorean})"
            else -> member.nicknameEnglish
        }

    /** DB에 수료일이 없어 수료 학기 말일을 수료일자 열 값으로 쓴다(업로드 시 날짜 파싱과 호환). */
    private fun approximateTermEndDate(semester: Semester): LocalDate {
        val lastMonth = semester.term.targetMonthRange.last
        return YearMonth.of(semester.year.value, lastMonth).atEndOfMonth()
    }

    private fun semesterShort(s: Semester): String {
        val yy = s.year.value % 100
        return "$yy${Semester.DELIMITER}${s.term.intValue}"
    }

    private fun semesterRangeLabel(start: Semester, end: Semester): String =
        "${semesterShort(start)}~${semesterShort(end)}"

    private fun headerRow(row: Row, headers: List<String>) {
        headers.forEachIndexed { i, h -> row.createTextCell(i, h) }
    }

    private fun Row.createTextCell(column: Int, text: String) {
        createCell(column).setCellValue(text)
    }
}
