package com.polykhel.ssa.config.logging;

import org.apache.commons.lang3.ObjectUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import java.util.Arrays;

import static com.polykhel.ssa.utils.constants.ProfileConstants.SPRING_PROFILE_DEVELOPMENT;

@Aspect
public class LoggingAspect {

    private final Environment env;

    public LoggingAspect(Environment env) {
        this.env = env;
    }

    /**
     * Pointcut that matches all repositories, services and Web REST endpoints.
     */
    @Pointcut(
        "within(@org.springframework.stereotype.Repository *)"
            + " || within(@org.springframework.stereotype.Service *)"
            + " || within(@org.springframework.web.bind.annotation.RestController *)")
    public void springBeanPointcut() {
        // the implementations are in the advices
    }

    /**
     * Pointcut that matches all Spring beans in the application's main packages.
     */
    @Pointcut(
        "within(com.polykhel.ssa.repository..*)"
            + " || within(com.polykhel.ssa.service..*)"
            + " || within(com.polykhel.ssa.web.rest..*)")
    public void applicationPackagePointcut() {
        // the implementations are in the advices
    }

    /**
     * Retrieves the {@link Logger} associated to the given {@link JoinPoint}.
     *
     * @param joinPoint join point we want the logger for.
     * @return {@link Logger} associated to the given {@link JoinPoint}
     */
    private Logger logger(JoinPoint joinPoint) {
        return LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName());
    }

    /**
     * Advice that logs methods throwing exceptions.
     *
     * @param joinPoint join point for advice.
     * @param e         exception
     */
    @AfterThrowing(pointcut = "applicationPackagePointcut() && springBeanPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        Logger log = logger(joinPoint);
        if (env.acceptsProfiles(Profiles.of(SPRING_PROFILE_DEVELOPMENT))) {
            log.error(
                "Exception in {}() with cause = '{}' and exception = '{}'",
                joinPoint.getSignature().getName(),
                ObjectUtils.defaultIfNull(e.getCause(), "NULL"),
                e.getMessage(),
                e);

        } else {
            log.error(
                "Exception in {}() with cause = {}",
                joinPoint.getSignature().getName(),
                ObjectUtils.defaultIfNull(e.getCause(), "NULL"));
        }
    }

    /**
     * Advice that logs when a method is entered and exited.
     *
     * @param joinPoint join point for advice.
     * @return result.
     * @throws Throwable throws {@link IllegalArgumentException}.
     */
    @Around("applicationPackagePointcut() && springBeanPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger log = logger(joinPoint);
        if (log.isDebugEnabled()) {
            log.debug(
                "Enter: {}() with argument[s] = {}",
                joinPoint.getSignature().getName(),
                Arrays.toString(joinPoint.getArgs()));
        }
        try {
            Object result = joinPoint.proceed();
            if (log.isDebugEnabled()) {
                log.debug("Exist: {}() with result = {}", joinPoint.getSignature().getName(), result);
            }
            return result;
        } catch (IllegalArgumentException e) {
            log.error(
                "Illegal argument: {} in {}()",
                Arrays.toString(joinPoint.getArgs()),
                joinPoint.getSignature().getName());
            throw e;
        }
    }
}
