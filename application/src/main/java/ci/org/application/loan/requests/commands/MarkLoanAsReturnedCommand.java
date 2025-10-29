package ci.org.application.loan.requests.commands;

import java.time.LocalDate;

/**
 * Command (DTO) pour marquer un emprunt comme retourné.
 *
 * Input pour MarkLoanAsReturnedUseCase.
 */
public record MarkLoanAsReturnedCommand(
        String loanId,
        LocalDate returnDate
) {

    public MarkLoanAsReturnedCommand {
        if (loanId == null || loanId.isBlank()) {
            throw new IllegalArgumentException("Loan ID cannot be null or blank");
        }
        if (returnDate == null) {
            throw new IllegalArgumentException("Return date cannot be null");
        }
    }
}
