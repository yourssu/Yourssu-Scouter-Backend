package com.yourssu.scouter.admin

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AdminSessionInterceptor : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val isAuthorized = request.session.getAttribute(AdminConstants.RECRUITING_ADMIN_SESSION_KEY) == true
        if (!isAuthorized) {
            response.sendRedirect("/admin/recruiting")
            return false
        }
        return true
    }
}
