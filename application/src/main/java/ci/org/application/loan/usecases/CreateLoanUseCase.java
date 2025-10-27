package ci.org.application.loan.usecases;

import ci.org.application.loan.commands.CreateLoanCommand;
import ci.org.application.loan.dtos.LoanResponse;
import ci.org.application.loan.mappers.LoanMapper;
import ci.org.domain.loan.enitites.Loan;
import ci.org.domain.loan.repositories.LoanRepository;
import ci.org.shared.kernel.CommandUseCase;
import ci.org.shared.kernel.TransactionMode;
import ci.org.shared.kernel.UseCase;

import java.util.Objects;

@UseCase(value = "CreateLoan", transactionMode = TransactionMode.READ_WRITE)
public class CreateLoanUseCase implements CommandUseCase<CreateLoanCommand, LoanResponse> {

    private final LoanRepository loanRepository;

    public CreateLoanUseCase(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    public LoanResponse execute(CreateLoanCommand command) {
        Objects.requireNonNull(command, "CreateLoanCommand cannot be null");

        // 1. Créer l'entité domain (validation métier automatique)
        Loan loan = Loan.create(
                command.memberId(),
                command.bookId(),
                command.borrowDate(),
                command.dueDate()
        );

        // 2. Persister
        Loan savedLoan = loanRepository.save(loan);

        // 3. Mapper vers DTO et retourner
        return LoanMapper.toResponse(savedLoan);
    }
}

