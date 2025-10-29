package ci.org.application.loan.usecases;

import ci.org.application.loan.requests.queries.SearchLoansQuery;
import ci.org.application.loan.responses.LoanResponse;
import ci.org.application.loan.mappers.LoanMapper;
import ci.org.domain.loan.enitites.Loan;
import ci.org.domain.loan.repositories.LoanRepository;
import ci.org.shared.kernel.*;

/**
 * Use Case : Récupérer tous les emprunts actifs avec pagination.
 *
 * Implémente QueryUseCase car interroge l'état (read).
 */
@UseCase(value = "GetActiveLoans", transactionMode = TransactionMode.READ_ONLY)
public class GetActiveLoansUseCase implements QueryUseCase<SearchLoansQuery, Page<LoanResponse>> {

    private final LoanRepository loanRepository;

    public GetActiveLoansUseCase(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    public Page<LoanResponse> execute(SearchLoansQuery query) {

        Page<Loan> loanPage = loanRepository.findActiveLoans(toPageRequest(query));

        return loanPage.map(LoanMapper::toResponse);
    }

    private PageRequest toPageRequest(SearchLoansQuery query) {
        return PageRequest.of(
                query.page(),
                query.size(),
                Sort.by(query.sortBy(), Sort.Direction.valueOf(query.sortDirection()))
        );
    }
}