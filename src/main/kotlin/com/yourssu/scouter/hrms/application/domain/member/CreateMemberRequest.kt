package com.yourssu.scouter.hrms.application.domain.member

import com.fasterxml.jackson.annotation.JsonFormat
import com.yourssu.scouter.hrms.business.domain.member.CreateMemberCommand
import com.yourssu.scouter.hrms.business.domain.member.MemberRoleConverter
import com.yourssu.scouter.hrms.business.domain.member.MemberStateConverter
import com.yourssu.scouter.hrms.business.domain.member.NicknameConverter
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import java.time.LocalDate

data class CreateMemberRequest(

    @NotNull(message = "파트를 입력하지 않았습니다.")
    val partId: Long,

    @NotBlank(message = "역할을 입력하지 않았습니다.")
    val role: String,

    @NotBlank(message = "이름을 입력하지 않았습니다.")
    val name: String,

    @NotBlank(message = "닉네임을 입력하지 않았습니다.")
    @Pattern(
        regexp = "^[a-zA-Z]+\\([가-힣]+\\)$",
        message = "닉네임은 \\{ 영어(발음) \\} 형식이어야 합니다."
    )
    val nickname: String,

    @NotBlank(message = "상태를 입력하지 않았습니다.")
    val state: String,

    @NotNull(message = "가입일을 입력하지 않았습니다.")
    @JsonFormat(pattern = "yyyy.MM.dd")
    val joinDate: LocalDate,

    @Email(message = "이메일 형식이 아닙니다.")
    val email: String,

    @NotBlank(message = "전화번호를 입력하지 않았습니다.")
    @Pattern(
        regexp = "^010-\\d{4}-\\d{4}\$",
        message = "전화번호는 \\{ 010-xxxx-xxxx \\} 형식이어야 합니다"
    )
    val phoneNumber: String,

    @NotNull(message = "학과를 입력하지 않았습니다.")
    val departmentId: Long,

    @NotBlank(message = "학번을 입력하지 않았습니다.")
    val studentId: String,

    @NotNull(message = "가입일을 입력하지 않았습니다.")
    @JsonFormat(pattern = "yyyy.MM.dd")
    val birthDate: LocalDate,

    @NotNull(message = "회비 납부 여부를 입력하지 않았습니다.")
    val membershipFee: Boolean,

    @NotBlank(message = "비고를 입력하지 않았습니다.")
    val note: String,
) {

    fun toCommand(): CreateMemberCommand = CreateMemberCommand(
        name = name,
        email = email,
        phoneNumber = phoneNumber,
        birthDate = birthDate,
        departmentId = departmentId,
        studentId = studentId,
        partId = partId,
        role = MemberRoleConverter.convertToEnum(role),
        nicknameEnglish = NicknameConverter.extractNickname(nickname),
        nicknameKorean = NicknameConverter.extractPronunciation(nickname),
        state = MemberStateConverter.convertToEnum(state),
        joinDate = joinDate,
        isMembershipFeePaid = membershipFee,
        note = note,
    )
}
