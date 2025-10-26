package ci.org.application.loan.usecases;

import ci.org.application.loan.commands.MarkLoanAsOverdueCommand;
import ci.org.application.loan.dtos.LoanResponse;
import ci.org.application.loan.mappers.LoanMapper;
import ci.org.domain.loan.enitites.Loan;
import ci.org.domain.loan.exceptions.LoanNotFoundException;
import ci.org.domain.loan.repositories.LoanRepository;
import ci.org.domain.loan.valueobjects.LoanId;
import ci.org.shared.kernel.CommandUseCase;
import ci.org.shared.kernel.TransactionMode;
import ci.org.shared.kernel.UseCase;

import java.util.Objects;

@UseCase(value = "MarkLoanAsOverdue", transactionMode = TransactionMode.READ_WRITE)
public class MarkLoanAsOverdueUseCase implements CommandUseCase<MarkLoanAsOverdueCommand, LoanResponse> {

    private final LoanRepository loanRepository;

    public MarkLoanAsOverdueUseCase(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    public LoanResponse execute(MarkLoanAsOverdueCommand command) {
        Objects.requireNonNull(command, "MarkLoanAsOverdueCommand cannot be null");

        // 1. Rechercher l'emprunt
        Loan loan = loanRepository.findById(LoanId.of(command.loanId()))
                .orElseThrow(() -> new LoanNotFoundException(command.loanId()));

        // 2. Appliquer la règle métier
        loan.markAsOverdue();

        // 3. Persister
        Loan updatedLoan = loanRepository.save(loan);

        // 4. Mapper vers DTO et retourner
        return LoanMapper.toResponse(updatedLoan);
    }
}

