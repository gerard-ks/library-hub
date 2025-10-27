package ci.org.infrastructure.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 * Aspect AOP pour appliquer automatiquement @Transactional sur les Use Cases.
 *
 * Phase 1 (In-Memory) : Pas de vraies transactions, mais logging
 * Phase 2 (JPA) : Ajouter @Transactional pour gérer les transactions
 *
 * Cet aspect intercepte l'exécution de tous les Use Cases annotés avec @UseCase.
 */
@Aspect
@Component
public class UseCaseTransactionalAspect {

    private static final Logger logger = LoggerFactory.getLogger(UseCaseTransactionalAspect.class);

    /**
     * Intercepte l'exécution de la méthode execute() de tous les Use Cases.
     *
     * @param joinPoint le point d'exécution
     * @return le résultat de l'exécution
     * @throws Throwable en cas d'erreur
     */
    @Around("@within(ci.org.shared.kernel.UseCase) && execution(* execute(..))")
    public Object applyTransactional(ProceedingJoinPoint joinPoint) throws Throwable {
        String useCaseName = joinPoint.getTarget().getClass().getSimpleName();

        logger.debug("Executing use case: {}", useCaseName);

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Use case {} completed in {} ms", useCaseName, executionTime);

            return result;

        } catch (Exception e) {
            logger.error("Use case {} failed: {}", useCaseName, e.getMessage());
            throw e;
        }
    }
}