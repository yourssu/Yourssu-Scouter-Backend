package com.yourssu.scouter.common.support.implement.configuration

import com.yourssu.scouter.common.support.implement.initialization.ApplicantAvailableTimeMap
import com.yourssu.scouter.common.support.implement.initialization.ApplicantSyncMappingData
import com.yourssu.scouter.common.support.implement.localh2.ScouterLocalH2DataProperties
import com.yourssu.scouter.mail.support.implement.mail.MailSenderProperties
import com.yourssu.scouter.mail.support.implement.storage.MailS3StorageProperties
import com.yourssu.scouter.auth.support.oauth2.GoogleOAuth2Properties
import com.yourssu.scouter.auth.support.jwt.JwtProperties
import com.yourssu.scouter.hrms.support.implement.MemberExcelToolProperties
import com.yourssu.scouter.hrms.support.implement.MemberParseMappingData
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(JwtProperties::class, GoogleOAuth2Properties::class, ApplicantSyncMappingData::class, MemberParseMappingData::class,
    ApplicantAvailableTimeMap::class, MailS3StorageProperties::class, MemberExcelToolProperties::class, ScouterLocalH2DataProperties::class, MailSenderProperties::class)
class PropertiesConfiguration
