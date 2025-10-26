package ci.org.application.loan.commands;

/**
 * Command (DTO) pour marquer un emprunt comme en retard.
 *
 * Input pour MarkLoanAsOverdueUseCase.
 */
public record MarkLoanAsOverdueCommand(
        String loanId
) {

    public MarkLoanAsOverdueCommand {
        if (loanId == null || loanId.isBlank()) {
            throw new IllegalArgumentException("Loan ID cannot be null or blank");
        }
    }
}

