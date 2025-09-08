package com.framework.apiserver.aspects;

import com.framework.apiserver.service.BrowserContextManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class BrowserContextAspect {

    @Autowired
    private BrowserContextManager browserContextManager;

    /**
     * Automatically extract browser type before any test execution service method
     */
    @Around("execution(* com.framework.apiserver.service.TestExecutionService.*(..)) || " +
            "execution(* com.framework.apiserver.service.TestRerunService.*(..))")
    public Object setBrowserContextForTestExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        // Extract and set browser type BEFORE method execution
        String browserType = browserContextManager.extractAndSetBrowserType();
        try {
            // Execute the original method
            Object result = joinPoint.proceed();
            return result;

        } catch (Exception e) {
            System.out.println("BrowserContextAspect: Method failed with browser " + browserType + ": " + e.getMessage());
            throw e;
        }
    }

}