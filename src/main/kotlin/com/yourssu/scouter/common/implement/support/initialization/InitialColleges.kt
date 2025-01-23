package com.yourssu.scouter.common.implement.support.initialization

import com.yourssu.scouter.common.implement.domain.college.College

class InitialColleges {

    companion object {
        fun getData(): List<College> = listOf(
            College(name = "IT대학"),
            College(name = "경영대학"),
            College(name = "경제통상대학"),
            College(name = "공과대학"),
            College(name = "법과대학"),
            College(name = "베어드학부대학"),
            College(name = "사회과학대학"),
            College(name = "인문대학"),
            College(name = "자연과학대학"),
            College(name = "차세대반도체학과"),
        )
    }
}
