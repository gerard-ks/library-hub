package ci.org.presentation.rest.requests;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO de requête pour retourner un emprunt.
 */
public record ReturnLoanRequest(
        @NotNull(message = "Return date cannot be null")
        LocalDate returnDate
) {
}