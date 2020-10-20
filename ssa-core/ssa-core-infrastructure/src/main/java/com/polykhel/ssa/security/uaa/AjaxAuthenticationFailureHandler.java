package com.polykhel.ssa.security.uaa;

import lombok.NoArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@NoArgsConstructor
public class AjaxAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    public static final String UNAUTHORIZED_MESSAGE = "Authentication failed";

    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        response.sendError(401, UNAUTHORIZED_MESSAGE);
    }
}
