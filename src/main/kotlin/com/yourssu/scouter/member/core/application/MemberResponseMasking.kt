package com.yourssu.scouter.member.core.application

import com.yourssu.scouter.member.core.application.dto.ReadActiveMemberResponse

import com.yourssu.scouter.member.core.application.dto.ReadGraduatedMemberListItemResponse

import com.yourssu.scouter.member.core.application.dto.ReadCompletedMemberResponse

import com.yourssu.scouter.member.core.application.dto.ReadWithdrawnMemberListItemResponse

import com.yourssu.scouter.member.core.application.dto.ReadGraduatedMemberResponse

import com.yourssu.scouter.member.core.application.dto.ReadActiveMemberListItemResponse

import com.yourssu.scouter.member.core.application.dto.ReadInactiveMemberListItemResponse

import com.yourssu.scouter.member.core.application.dto.ReadWithdrawnMemberResponse

import com.yourssu.scouter.member.core.application.dto.ReadInactiveMemberResponse

import com.yourssu.scouter.member.core.application.dto.ReadCompletedMemberListItemResponse

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

