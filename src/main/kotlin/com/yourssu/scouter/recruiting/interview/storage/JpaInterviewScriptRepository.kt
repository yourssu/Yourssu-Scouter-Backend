package com.yourssu.scouter.recruiting.interview.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaInterviewScriptRepository : JpaRepository<InterviewScriptEntity, Long>
