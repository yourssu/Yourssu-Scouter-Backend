package com.yourssu.scouter.recruiting.evaluation.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaInterviewEvaluationItemRepository : JpaRepository<InterviewEvaluationItemEntity, Long>
