package com.polykhel.ssa.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Controller for getting the SSH public key.
 */
@RestController
@RequestMapping("/api/ssh")
public class SshResource {

    private final Logger log = LoggerFactory.getLogger(SshResource.class);

    /**
     * {@code GET  /ssh/public_key} : get the SSH public key
     */
    @GetMapping(value = "/public_key", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getSshPublicKey() {
        try {
            String publicKey = getPublicKey();
            if (publicKey != null) {
                return ResponseEntity.ok(publicKey);
            }
        } catch (IOException e) {
            log.warn("SSH public key could not be loaded: {}", e.getMessage());
        }
        return ResponseEntity.notFound().build();
    }

    String getPublicKey() throws IOException {
        return new String(Files.readAllBytes(
            Paths.get(System.getProperty("user.home") + "/.ssh/id_rsa.pub")));
    }
}
