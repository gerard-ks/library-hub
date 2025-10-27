package ci.org.presentation.rest.requests;

import jakarta.validation.constraints.Min;

/**
 * DTO de requête pour ajuster la limite d'emprunts d'un membre.
 */
public record AdjustMemberLimitRequest(
        @Min(value = 1, message = "Borrowing limit must be at least 1")
        int newLimit
) {
}