package ci.org.infrastructure.config;

import ci.org.shared.kernel.UseCase;
import ci.org.shared.kernel.DomainService;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * Configuration Spring pour le scan automatique des Use Cases et Domain Services.
 *
 * Spring détecte automatiquement :
 * - Tous les @UseCase (Application Layer)
 * - Tous les @DomainService (Domain Layer)
 * - Tous les @Repository (Infrastructure Layer)
 * - Tous les @Component (Infrastructure Layer)
 *
 * Pas besoin de @Bean manuels, tout est auto-découvert !
 */
@Configuration
@ComponentScan(
        basePackages = {
                "ci.org.application",
                "ci.org.infrastructure",
                "ci.org.domain"
        },
        includeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ANNOTATION,
                        classes = {UseCase.class}
                ),
                @ComponentScan.Filter(
                        type = FilterType.ANNOTATION,
                        classes = {DomainService.class}
                )
        }
)
public class UseCaseConfiguration {
    // Spring détecte automatiquement :
    // - Tous les @UseCase
    // - Tous les @Repository
    // - Tous les @Component
    // - Tous les @DomainService
}