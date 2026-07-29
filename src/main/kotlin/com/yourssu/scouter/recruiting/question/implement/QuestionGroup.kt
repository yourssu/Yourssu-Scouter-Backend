package com.yourssu.scouter.recruiting.question.implement

enum class QuestionGroup(val isFixed: Boolean) {
    REQUIRED(true),
    CULTURE_FIT(true),
    CLOSING(true),
    PERSONAL(false),
    TEAM_FIT(false),
    JOB_FIT(false),
}
