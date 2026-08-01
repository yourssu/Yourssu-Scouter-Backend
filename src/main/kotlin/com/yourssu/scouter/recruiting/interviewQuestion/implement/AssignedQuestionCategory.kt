package com.yourssu.scouter.recruiting.interviewQuestion.implement

enum class AssignedQuestionCategory(val isCatalog: Boolean) {
    /**
     * Question 카탈로그는 PERSONAL이 생길 수 없으므로 ENUM 분리
     */
    GLOBAL(true),
    CULTURE(true),
    PART(true),
    PERSONAL(false),
}
