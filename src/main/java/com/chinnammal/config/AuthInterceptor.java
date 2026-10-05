package com.chinnammal.config;

import com.chinnammal.dto.UserSessionDTO;
import com.smarthostel.model.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String SESSION_USER = "CHINNAMMAL_USER";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        String uri = request.getRequestURI();

        // Allow public and auth endpoints
        if (uri.startsWith("/api/auth/")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        UserSessionDTO currentUser = (session != null) ? (UserSessionDTO) session.getAttribute(SESSION_USER) : null;

        if (currentUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"Authentication required. Please log in.\"}");
            return false;
        }

        // Inactive account check
        if (!"ACTIVE".equalsIgnoreCase(currentUser.getStatus())) {
            if (session != null) {
                session.invalidate();
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"Your account has been deactivated. Please contact the administrator.\"}");
            return false;
        }

        // Mandatory password change check: user must change temporary password before calling any API
        if (currentUser.isForcePasswordChange()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"Mandatory password change required before accessing platform features.\",\"code\":\"PASSWORD_CHANGE_REQUIRED\"}");
            return false;
        }

        if (uri.startsWith("/api/admin/")) {
            if (currentUser.getRole() != Role.ADMIN) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"message\":\"Access denied: Administrator privileges required.\"}");
                return false;
            }
        } else if (uri.startsWith("/api/warden/")) {
            if (currentUser.getRole() != Role.WARDEN && currentUser.getRole() != Role.ADMIN) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"message\":\"Access denied: Warden privileges required.\"}");
                return false;
            }
        } else if (uri.startsWith("/api/student/")) {
            if (currentUser.getRole() != Role.STUDENT) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"message\":\"Access denied: Student access required.\"}");
                return false;
            }
        }

        return true;
    }
}
