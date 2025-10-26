package ci.org.application.loan.usecases;

import ci.org.application.loan.dtos.LoanResponse;
import ci.org.application.loan.mappers.LoanMapper;
import ci.org.application.loan.queries.GetActiveLoansQuery;
import ci.org.domain.loan.enitites.Loan;
import ci.org.domain.loan.repositories.LoanRepository;
import ci.org.shared.kernel.*;

import java.util.Objects;

/**
 * Use Case : Récupérer tous les emprunts actifs avec pagination.
 *
 * Implémente QueryUseCase car interroge l'état (read).
 */
@UseCase(value = "GetActiveLoans", transactionMode = TransactionMode.READ_ONLY)
public class GetActiveLoansUseCase implements QueryUseCase<GetActiveLoansQuery, Page<LoanResponse>> {

    private final LoanRepository loanRepository;

    public GetActiveLoansUseCase(LoanRepository loanRepository) {
        this.loanRepository = Objects.requireNonNull(loanRepository, "LoanRepository cannot be null");
    }

    @Override
    public Page<LoanResponse> execute(GetActiveLoansQuery query) {
        Objects.requireNonNull(query, "GetActiveLoansQuery cannot be null");

        // 1. Créer PageRequest (domain)
        PageRequest pageRequest = PageRequest.of(
                query.page(),
                query.size(),
                Sort.by(query.sortBy(), Sort.Direction.valueOf(query.sortDirection()))
        );

        // 2. Récupérer Page<Loan> (domain)
        Page<Loan> loanPage = loanRepository.findActiveLoans(pageRequest);

        // 3. Mapper vers Page<LoanResponse>
        return loanPage.map(LoanMapper::toResponse);
    }
}