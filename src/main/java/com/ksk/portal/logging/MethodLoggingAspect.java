package com.ksk.portal.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Logs entry, completion time and failures for application methods without logging arguments/secrets.
 */
@Aspect
@Component
public class MethodLoggingAspect {
    private static final Logger log = LoggerFactory.getLogger(MethodLoggingAspect.class);

    @Around("execution(* com.ksk.portal..*(..)) && !within(com.ksk.portal.logging..*)")
    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        long start = System.nanoTime();
        log.debug("Method started: {}", method);
        try {
            Object result = joinPoint.proceed();
            log.debug("Method completed: {} durationMs={}", method, (System.nanoTime() - start) / 1_000_000);
            return result;
        } catch (Throwable ex) {
            log.error("Method failed: {} durationMs={} exceptionType={} message={}", method,
                    (System.nanoTime() - start) / 1_000_000, ex.getClass().getSimpleName(), ex.getMessage(), ex);
            throw ex;
        }
    }
}
