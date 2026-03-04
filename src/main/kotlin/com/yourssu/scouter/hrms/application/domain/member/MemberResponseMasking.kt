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
        note = null,
    )

fun ReadGraduatedMemberListItemResponse.maskSensitive(): ReadGraduatedMemberListItemResponse =
    this.copy(
        phoneNumber = null,
        studentId = null,
        birthDate = null,
        note = null,
    )

fun ReadWithdrawnMemberListItemResponse.maskSensitive(): ReadWithdrawnMemberListItemResponse =
    this.copy(
        phoneNumber = null,
        studentId = null,
        birthDate = null,
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
        note = null,
        isSensitiveMasked = true,
    )

fun ReadGraduatedMemberResponse.maskSensitive(): ReadGraduatedMemberResponse =
    this.copy(
        phoneNumber = null,
        studentId = null,
        birthDate = null,
        note = null,
        isSensitiveMasked = true,
    )

fun ReadWithdrawnMemberResponse.maskSensitive(): ReadWithdrawnMemberResponse =
    this.copy(
        phoneNumber = null,
        studentId = null,
        birthDate = null,
        note = null,
        isSensitiveMasked = true,
    )

