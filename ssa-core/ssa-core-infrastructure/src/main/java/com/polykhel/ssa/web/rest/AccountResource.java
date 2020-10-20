package com.polykhel.ssa.web.rest;

import com.polykhel.ssa.web.rest.vm.UserVM;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class AccountResource {

    /**
     * GET  /authenticate : check if the user is authenticated and return its login.
     *
     * @param request the HTTP request
     * @return the login if the user is authenticated
     */
    @GetMapping("/authenticate")
    public String isAuthenticated(HttpServletRequest request) {
        log.debug("REST request to check if the current user is authenticated");
        return request.getRemoteUser();
    }

    /**
     * GET  /account : get the current user.
     *
     * @return the ResponseEntity with status 200 (OK) and the current user in body, or status 500 (Internal Server
     * Error) if the user couldn't be returned
     */
    @GetMapping("/account")
    public ResponseEntity<UserVM> getAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        try {
            String login;
            if (authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                login = user.getUsername();
                log.debug("The username `{}` has been found using JWT", login);
            } else if (authentication.getPrincipal() instanceof String) {
                login = (String) authentication.getPrincipal();
            } else if (authentication instanceof OAuth2AuthenticationToken) {
                login = ((OAuth2AuthenticationToken) authentication).getPrincipal().getName();
                log.debug("The username `{}` has been found using OpenID Connect", login);
            } else {
                log.debug("The username could not be found");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            UserVM userVM = new UserVM(login,
                authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority).collect(Collectors.toSet()));
            return ResponseEntity.ok(userVM);
        } catch (NullPointerException | ClassCastException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
