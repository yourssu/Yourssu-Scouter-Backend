package com.yourssu.scouter.hrms.application.domain.member

fun ReadActiveMemberListItemResponse.maskSensitive(): ReadActiveMemberListItemResponse =
    this.copy(
        phoneNumber = null,
        studentId = null,
        birthDate = null,
        membershipFee = null,
        note = null,
    )

fun ReadInactiveMemberListItemResponse.maskSensitive(): ReadInactiveMemberListItemResponse =
    this.copy(
        phoneNumber = null,
        studentId = null,
        birthDate = null,
        expectedReturnSemester = null,
        reason = null,
        smsReplied = null,
        smsReplyDesiredPeriod = null,
        activitySemestersLabel = null,
        totalActiveSemesters = null,
        totalInactiveSemesters = null,
        activeSemesterCountLabel = null,
        inactiveSemesterCountLabel = null,
        note = null,
    )

fun ReadCompletedMemberListItemResponse.maskSensitive(): ReadCompletedMemberListItemResponse =
    this.copy(
        phoneNumber = null,
        studentId = null,
        birthDate = null,
        completionSemester = null,
        note = null,
    )

fun ReadGraduatedMemberListItemResponse.maskSensitive(): ReadGraduatedMemberListItemResponse =
    this.copy(
        phoneNumber = null,
        studentId = null,
        birthDate = null,
        activePeriod = null,
        note = null,
    )

fun ReadWithdrawnMemberListItemResponse.maskSensitive(): ReadWithdrawnMemberListItemResponse =
    this.copy(
        withdrawnDate = null,
        note = null,
    )

fun ReadActiveMemberResponse.maskSensitive(): ReadActiveMemberResponse =
    this.copy(
        phoneNumber = null,
        studentId = null,
        birthDate = null,
        membershipFee = null,
        note = null,
        isSensitiveMasked = true,
    )

fun ReadInactiveMemberResponse.maskSensitive(): ReadInactiveMemberResponse =
    this.copy(
        phoneNumber = null,
        studentId = null,
        birthDate = null,
        expectedReturnSemester = null,
        reason = null,
        smsReplied = null,
        smsReplyDesiredPeriod = null,
        activitySemestersLabel = null,
        totalActiveSemesters = null,
        totalInactiveSemesters = null,
        activeSemesterCountLabel = null,
        inactiveSemesterCountLabel = null,
        note = null,
        isSensitiveMasked = true,
    )

fun ReadCompletedMemberResponse.maskSensitive(): ReadCompletedMemberResponse =
    this.copy(
        phoneNumber = null,
        studentId = null,
        birthDate = null,
        completionSemester = null,
        note = null,
        isSensitiveMasked = true,
    )

fun ReadGraduatedMemberResponse.maskSensitive(): ReadGraduatedMemberResponse =
    this.copy(
        phoneNumber = null,
        studentId = null,
        birthDate = null,
        activePeriod = null,
        note = null,
        isSensitiveMasked = true,
    )

fun ReadWithdrawnMemberResponse.maskSensitive(): ReadWithdrawnMemberResponse =
    this.copy(
        withdrawnDate = null,
        note = null,
        isSensitiveMasked = true,
    )

