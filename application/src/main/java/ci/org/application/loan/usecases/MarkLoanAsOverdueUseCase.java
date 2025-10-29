package ci.org.application.loan.usecases;

import ci.org.application.loan.requests.commands.MarkLoanAsOverdueCommand;
import ci.org.application.loan.responses.LoanResponse;
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
        return loanRepository.findById(LoanId.of(command.loanId()))
                .map(this::markAsOverdue)
                .map(LoanMapper::toResponse)
                .orElseThrow(() -> new LoanNotFoundException(command.loanId()));
    }

    private Loan markAsOverdue(Loan loan) {
        loan.markAsOverdue();
        return loanRepository.save(loan);
    }
}

