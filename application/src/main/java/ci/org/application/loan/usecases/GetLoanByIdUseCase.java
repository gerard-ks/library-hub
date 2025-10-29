package ci.org.application.loan.usecases;

import ci.org.application.loan.responses.LoanResponse;
import ci.org.application.loan.mappers.LoanMapper;
import ci.org.application.loan.requests.queries.FindLoanQuery;
import ci.org.domain.loan.exceptions.LoanNotFoundException;
import ci.org.domain.loan.repositories.LoanRepository;
import ci.org.domain.loan.valueobjects.LoanId;
import ci.org.shared.kernel.QueryUseCase;
import ci.org.shared.kernel.TransactionMode;
import ci.org.shared.kernel.UseCase;

/**
 * Use Case : Récupérer un emprunt par son ID.
 *
 * Implémente QueryUseCase car interroge l'état (read).
 */
@UseCase(value = "GetLoanById", transactionMode = TransactionMode.READ_ONLY)
public class GetLoanByIdUseCase implements QueryUseCase<FindLoanQuery, LoanResponse> {

    private final LoanRepository loanRepository;

    public GetLoanByIdUseCase(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    public LoanResponse execute(FindLoanQuery query) {
        return loanRepository.findById(LoanId.of(query.loanId()))
                .map(LoanMapper::toResponse)
                .orElseThrow(() -> new LoanNotFoundException(query.loanId()));
    }
}