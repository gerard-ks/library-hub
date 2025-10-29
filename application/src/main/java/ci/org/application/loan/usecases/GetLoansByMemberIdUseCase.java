package ci.org.application.loan.usecases;

import ci.org.application.loan.requests.queries.SearchLoansQuery;
import ci.org.application.loan.responses.LoanResponse;
import ci.org.application.loan.mappers.LoanMapper;
import ci.org.domain.loan.enitites.Loan;
import ci.org.domain.loan.repositories.LoanRepository;
import ci.org.shared.kernel.*;

import java.util.Objects;

@UseCase(value = "GetLoansByMemberId", transactionMode = TransactionMode.READ_ONLY)
public class GetLoansByMemberIdUseCase implements QueryUseCase<SearchLoansQuery, Page<LoanResponse>> {

    private final LoanRepository loanRepository;

    public GetLoansByMemberIdUseCase(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    public Page<LoanResponse> execute(SearchLoansQuery query) {
        if (!query.hasMemberFilter()) {
            throw new IllegalArgumentException("Member ID is required for this query");
        }

        Page<Loan> loanPage = loanRepository.findByMemberId(query.memberId(), toPageRequest(query));

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

