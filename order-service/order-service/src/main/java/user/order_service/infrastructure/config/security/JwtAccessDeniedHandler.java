package user.order_service.infrastructure.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import user.order_service.infrastructure.exception.ErrorResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        ErrorResponse er = new ErrorResponse(false, "Invalid credentials", List.of("Access denied"));
        ObjectMapper mapper = new ObjectMapper();

        response.getWriter().write(mapper.writeValueAsString(er));

    }
}