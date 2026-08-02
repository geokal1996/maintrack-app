package com.codingfactory.maintrack.security;

import com.codingfactory.maintrack.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Auto to handler "piazei" tin periptosi pou o xristis EINAI syndedemenos
// (exei egkyro token) alla DEN exei to dikaioma gia sygkekrimeni energeia
// (p.x. SUPERVISOR pou prospathei na kanei DELETE se mihani - mono MANAGER mporei).
// I sosti apantisi edo einai 403 (Forbidden) - se antithesi me to
// JwtAuthenticationEntryPoint pou stelnei 401 otan DEN yparxei kan syndesi.
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public JwtAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                        AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        ApiError error = new ApiError(
                HttpServletResponse.SC_FORBIDDEN,
                "Den exeis to dikaioma gia afti tin energeia"
        );
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
