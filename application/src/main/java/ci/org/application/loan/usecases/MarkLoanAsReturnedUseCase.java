package ci.org.application.loan.usecases;

import ci.org.application.loan.requests.commands.MarkLoanAsReturnedCommand;
import ci.org.application.loan.responses.LoanResponse;
import ci.org.application.loan.mappers.LoanMapper;
import ci.org.domain.loan.enitites.Loan;
import ci.org.domain.loan.exceptions.LoanNotFoundException;
import ci.org.domain.loan.repositories.LoanRepository;
import ci.org.domain.loan.valueobjects.LoanId;
import ci.org.shared.kernel.CommandUseCase;
import ci.org.shared.kernel.TransactionMode;
import ci.org.shared.kernel.UseCase;

import java.time.LocalDate;
import java.util.Objects;

@UseCase(value = "MarkLoanAsReturned", transactionMode = TransactionMode.READ_WRITE)
public class MarkLoanAsReturnedUseCase implements CommandUseCase<MarkLoanAsReturnedCommand, LoanResponse> {

    private final LoanRepository loanRepository;

    public MarkLoanAsReturnedUseCase(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    public LoanResponse execute(MarkLoanAsReturnedCommand command) {
        return loanRepository.findById(LoanId.of(command.loanId()))
                .map(loan ->  markAsReturned(loan, command.returnDate()))
                .map(LoanMapper::toResponse)
                .orElseThrow(() -> new LoanNotFoundException(command.loanId()));
    }

    private Loan markAsReturned(Loan loan, LocalDate returnDate) {
        loan.markAsReturned(returnDate);
        return loanRepository.save(loan);
    }
}

