package com.polykhel.ssa.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Returns a 403 error code (Unauthorized) to the client.
 */
@Component
@Slf4j
public class UnauthorizedEntryPoint implements AuthenticationEntryPoint {

    /**
     * Return a 401 error code to the client.
     */
    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException)
        throws IOException {
        log.debug("Pre-authenticated entry point called. Rejected");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access Denied");
    }
}
