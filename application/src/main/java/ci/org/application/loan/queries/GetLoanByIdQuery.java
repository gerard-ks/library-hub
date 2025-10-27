package ci.org.application.loan.queries;

/**
 * Query (DTO) pour récupérer un emprunt par son ID.
 *
 * Input pour GetLoanByIdUseCase.
 */
public record GetLoanByIdQuery(
        String loanId
) {

    public GetLoanByIdQuery {
        if (loanId == null || loanId.isBlank()) {
            throw new IllegalArgumentException("Loan ID cannot be null or blank");
        }
    }
}

