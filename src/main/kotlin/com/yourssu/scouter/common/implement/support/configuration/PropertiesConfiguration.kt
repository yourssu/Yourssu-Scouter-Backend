package com.yourssu.scouter.common.implement.support.configuration

import com.yourssu.scouter.common.implement.support.initialization.ApplicantAvailableTimeMap
import com.yourssu.scouter.common.implement.support.initialization.ApplicantSyncMappingData
import com.yourssu.scouter.common.implement.support.storage.MailS3StorageProperties
import com.yourssu.scouter.common.implement.support.security.oauth2.GoogleOAuth2Properties
import com.yourssu.scouter.common.implement.support.security.token.JwtProperties
import com.yourssu.scouter.hrms.implement.support.MemberParseMappingData
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(JwtProperties::class, GoogleOAuth2Properties::class, ApplicantSyncMappingData::class, MemberParseMappingData::class,
    ApplicantAvailableTimeMap::class, MailS3StorageProperties::class)
class PropertiesConfiguration
