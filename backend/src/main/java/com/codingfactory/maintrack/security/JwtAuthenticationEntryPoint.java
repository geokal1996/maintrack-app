package com.codingfactory.maintrack.security;

import com.codingfactory.maintrack.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Xoris auto, to Spring Security gyrizei APO PROEPILOGI 403 (Forbidden) otan
// leipei i einai lathos to token - alla to sosto REST status gia "den eisai
// syndedemenos" einai 401 (Unauthorized). To 403 tha eprepe na simainei
// "eisai syndedemenos, alla den exeis to dikaioma".
// Auto to entry point "piazei" akrivos afti tin periptosi kai stelnei 401.
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        ApiError error = new ApiError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Apaiteitai syndesi (login) gia prosvasi se auto to endpoint"
        );
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
