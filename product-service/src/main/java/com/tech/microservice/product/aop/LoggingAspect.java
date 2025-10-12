package com.tech.microservice.product.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Pointcut("execution(* com.tech.microservice.product.service..*(..))")
    public void anyServiceMethod() {}


    @Around("anyServiceMethod()")
    public Object logRequestAndResponse(ProceedingJoinPoint joinPoint) throws Throwable {

        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        Object result;

        String username = "zahid_2258";

        log.info("========== [START] {} | User: {} ==========", methodName, username);

        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                try {
                    log.info("User: {} | Request Arg[{}]: {}", username, i, objectMapper.writeValueAsString(args[i]));
                } catch (Exception e) {
                    log.info("User: {} | Request Arg[{}]: [unserializable: {}]", username, i, args[i].getClass().getSimpleName());
                }
            }
        } else {
            log.info("User: {} | No Request Arguments", username);
        }

        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            log.error("User: {} | Exception in {}: {}", username, methodName, ex.getMessage(), ex);
            throw ex;
        }

        try {
            log.info("User: {} | Response: {}", username, objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            log.info("User: {} | Response: [unserializable: {}]", username, result != null ? result.getClass().getSimpleName() : "null");
        }

        log.info("========== [END] {} | User: {} ==========", methodName, username);
        return result;
    }

    /**
     * Safely extracts current username from Spring SecurityContext.
     */
    /*private String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            // no user in context (e.g., public endpoint)
        }
        return "anonymous";
    }*/
}
