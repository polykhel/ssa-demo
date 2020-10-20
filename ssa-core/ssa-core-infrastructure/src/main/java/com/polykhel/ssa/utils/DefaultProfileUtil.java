package com.polykhel.ssa.utils;

import org.springframework.boot.SpringApplication;

import java.util.HashMap;
import java.util.Map;

import static com.polykhel.ssa.utils.constants.ProfileConstants.SPRING_PROFILE_DEVELOPMENT;

/**
 * Sets the default Spring profile to {@code dev} if no active profile is set in {@code
 * application.yml} or as command line argument.
 */
public final class DefaultProfileUtil {

    private static final String SPRING_PROFILE_DEFAULT = "spring.profiles.default";

    private DefaultProfileUtil() {
    }

    public static void setDefaultProfile(SpringApplication application) {
        Map<String, Object> props = new HashMap<>();
        props.put(SPRING_PROFILE_DEFAULT, SPRING_PROFILE_DEVELOPMENT);
        application.setDefaultProperties(props);
    }
}
