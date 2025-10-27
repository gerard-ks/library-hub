package ci.org.application.loan.usecases;

import ci.org.application.loan.dtos.LoanResponse;
import ci.org.application.loan.mappers.LoanMapper;
import ci.org.application.loan.queries.GetOverdueLoansQuery;
import ci.org.domain.loan.enitites.Loan;
import ci.org.domain.loan.repositories.LoanRepository;
import ci.org.shared.kernel.*;

import java.util.Objects;

@UseCase(value = "GetOverdueLoans", transactionMode = TransactionMode.READ_ONLY)
public class GetOverdueLoansUseCase implements QueryUseCase<GetOverdueLoansQuery, Page<LoanResponse>> {

    private final LoanRepository loanRepository;

    public GetOverdueLoansUseCase(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    public Page<LoanResponse> execute(GetOverdueLoansQuery query) {
        Objects.requireNonNull(query, "GetOverdueLoansQuery cannot be null");

        // 1. Créer PageRequest (domain)
        PageRequest pageRequest = PageRequest.of(
                query.page(),
                query.size(),
                Sort.by(query.sortBy(), Sort.Direction.valueOf(query.sortDirection()))
        );

        // 2. Récupérer Page<Loan> (domain)
        Page<Loan> loanPage = loanRepository.findOverdueLoans(pageRequest);

        // 3. Mapper vers Page<LoanResponse>
        return loanPage.map(LoanMapper::toResponse);
    }
}
