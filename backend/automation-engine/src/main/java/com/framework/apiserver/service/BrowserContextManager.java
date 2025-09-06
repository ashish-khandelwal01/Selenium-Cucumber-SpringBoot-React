package com.framework.apiserver.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class BrowserContextManager {

    private static final String BROWSER_TYPE_HEADER = "X-Browser-Type";
    private static final String DEFAULT_BROWSER = "chrome";

    @Getter
    @Setter
    private String browserType;

    /**
     * Extract and set browser type from current HTTP request context.
     * This manually does what BrowserInterceptor would do.
     */
    public String extractAndSetBrowserType() {
        String browserType = extractBrowserTypeFromRequest();
        if (browserType != null) {
            setBrowserType(browserType);
        } else {
            browserType = DEFAULT_BROWSER;
            setBrowserType(browserType);
        }

        return browserType;
    }

    /**
     * Try to extract browser type from current request context
     */
    private String extractBrowserTypeFromRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String browserType = request.getHeader(BROWSER_TYPE_HEADER);

                if (browserType != null && !browserType.trim().isEmpty()) {
                    return browserType.toLowerCase().trim();
                }

                // Log all headers for debugging
                request.getHeaderNames().asIterator().forEachRemaining(headerName ->
                        System.out.println("  " + headerName + ": " + request.getHeader(headerName)));
            }
        } catch (Exception e) {
            System.out.println("BrowserContextManager: Error extracting browser type from request: " + e.getMessage());
        }

        return null;
    }
}