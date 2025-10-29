package ci.org.application.loan.requests.queries;

/**
 * Query (DTO) pour récupérer un emprunt par son ID.
 *
 * Input pour GetLoanByIdUseCase.
 */
public record FindLoanQuery(
        String loanId
) {

    public FindLoanQuery {
        if (loanId == null || loanId.isBlank()) {
            throw new IllegalArgumentException("Loan ID cannot be null or blank");
        }
    }
}

