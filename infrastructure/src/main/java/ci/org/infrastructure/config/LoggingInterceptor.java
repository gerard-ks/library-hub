package ci.org.infrastructure.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Intercepteur HTTP pour logger toutes les requêtes entrantes et sortantes.
 *
 * Logs automatiquement :
 * - Méthode HTTP et URI
 * - Adresse IP du client
 * - Status code de la réponse
 * - Temps d'exécution
 */
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
    private static final String START_TIME_ATTRIBUTE = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String clientIp = getClientIp(request);

        logger.info("→ {} {} from {}",
                request.getMethod(),
                request.getRequestURI(),
                clientIp
        );

        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        if (startTime == null) {
            return;
        }

        long duration = System.currentTimeMillis() - startTime;
        int status = response.getStatus();

        if (status >= 500) {
            logger.error("← {} {} completed with status {} in {}ms",
                    request.getMethod(),
                    request.getRequestURI(),
                    status,
                    duration
            );
        } else if (status >= 400) {
            logger.warn("← {} {} completed with status {} in {}ms",
                    request.getMethod(),
                    request.getRequestURI(),
                    status,
                    duration
            );
        } else {
            logger.info("← {} {} completed with status {} in {}ms",
                    request.getMethod(),
                    request.getRequestURI(),
                    status,
                    duration
            );
        }

        if (ex != null) {
            logger.error("Request failed with exception", ex);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            // L'en-tête X-Forwarded-For peut contenir une liste d'IP (client, proxy1, proxy2)
            // La *première* IP est celle du client d'origine.
            return forwardedFor.split(",")[0].trim();
        }
        // Fallback si on n'est pas derrière un proxy
        return request.getRemoteAddr();
    }
}