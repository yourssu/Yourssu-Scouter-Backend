package com.yourssu.scouter.hrms.application.domain.member

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ExcelMemberParsingController {

    @GetMapping("/members/upload")
    fun showUploadPage(model: Model): String = "member-upload.html"
}
