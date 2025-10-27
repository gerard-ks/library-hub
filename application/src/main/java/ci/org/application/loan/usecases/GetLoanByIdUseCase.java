package ci.org.application.loan.usecases;

import ci.org.application.loan.dtos.LoanResponse;
import ci.org.application.loan.mappers.LoanMapper;
import ci.org.application.loan.queries.GetLoanByIdQuery;
import ci.org.domain.loan.enitites.Loan;
import ci.org.domain.loan.exceptions.LoanNotFoundException;
import ci.org.domain.loan.repositories.LoanRepository;
import ci.org.domain.loan.valueobjects.LoanId;
import ci.org.shared.kernel.QueryUseCase;
import ci.org.shared.kernel.TransactionMode;
import ci.org.shared.kernel.UseCase;

import java.util.Objects;

/**
 * Use Case : Récupérer un emprunt par son ID.
 *
 * Implémente QueryUseCase car interroge l'état (read).
 */
@UseCase(value = "GetLoanById", transactionMode = TransactionMode.READ_ONLY)
public class GetLoanByIdUseCase implements QueryUseCase<GetLoanByIdQuery, LoanResponse> {

    private final LoanRepository loanRepository;

    public GetLoanByIdUseCase(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    public LoanResponse execute(GetLoanByIdQuery query) {
        Objects.requireNonNull(query, "GetLoanByIdQuery cannot be null");

        // 1. Rechercher l'emprunt
        Loan loan = loanRepository.findById(LoanId.of(query.loanId()))
                .orElseThrow(() -> new LoanNotFoundException(query.loanId()));

        // 2. Mapper vers DTO et retourner
        return LoanMapper.toResponse(loan);
    }
}