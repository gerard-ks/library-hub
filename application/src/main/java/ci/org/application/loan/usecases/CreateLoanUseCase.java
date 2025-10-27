package ci.org.application.loan.usecases;

import ci.org.application.loan.commands.CreateLoanCommand;
import ci.org.application.loan.dtos.LoanResponse;
import ci.org.application.loan.mappers.LoanMapper;
import ci.org.domain.book.entities.Book;
import ci.org.domain.book.exceptions.BookNotFoundException;
import ci.org.domain.book.repositories.BookRepository;
import ci.org.domain.book.valueobjects.BookId;
import ci.org.domain.loan.enitites.Loan;
import ci.org.domain.loan.exceptions.LoanNotAllowedException;
import ci.org.domain.loan.repositories.LoanRepository;
import ci.org.domain.loan.services.LoanEligibilityService;
import ci.org.domain.member.entities.Member;
import ci.org.domain.member.exceptions.MemberNotFoundException;
import ci.org.domain.member.repositories.MemberRepository;
import ci.org.domain.member.valueobjects.MemberId;
import ci.org.shared.kernel.CommandUseCase;
import ci.org.shared.kernel.TransactionMode;
import ci.org.shared.kernel.UseCase;

import java.util.List;
import java.util.Objects;

@UseCase(value = "CreateLoan", transactionMode = TransactionMode.READ_WRITE)
public class CreateLoanUseCase implements CommandUseCase<CreateLoanCommand, LoanResponse> {

    private final LoanRepository loanRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final LoanEligibilityService eligibilityService;


    public CreateLoanUseCase(LoanRepository loanRepository, MemberRepository memberRepository, BookRepository bookRepository, LoanEligibilityService eligibilityService) {
        this.loanRepository = loanRepository;
        this.memberRepository = memberRepository;
        this.bookRepository = bookRepository;
        this.eligibilityService = eligibilityService;
    }

    @Override
    public LoanResponse execute(CreateLoanCommand command) {
        Objects.requireNonNull(command, "CreateLoanCommand cannot be null");

        // 1. Récupérer le membre
        Member member = memberRepository.findById(new MemberId(command.memberId()))
                .orElseThrow(() -> new MemberNotFoundException(command.memberId()));

        // 2. Récupérer le livre
        Book book = bookRepository.findById(new BookId(command.bookId()))
                .orElseThrow(() -> new BookNotFoundException(command.bookId()));

        // 3. Compter les emprunts actifs du membre
        long currentActiveLoans = loanRepository.countActiveByMemberId(command.memberId());


        // 4. Vérifier l'éligibilité
        boolean canBorrow = eligibilityService.canMemberBorrowBook(
                member,
                book,
                (int) currentActiveLoans
        );

        if (!canBorrow) {
            // Construire un message d'erreur détaillé
            String reason = buildLoanDenialReason(member, book, (int) currentActiveLoans);
            throw new LoanNotAllowedException(reason);
        }

        // 5. Créer l'entité domain (validation métier automatique)
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

    private String buildLoanDenialReason(Member member, Book book, int currentActiveLoans) {
        StringBuilder reason = new StringBuilder("Loan not allowed for member '");
        reason.append(member.getNameValue()).append("' (ID: ").append(member.getId().value()).append("). ");

        if (!member.canBorrow()) {
            reason.append("Member is not authorized to borrow. ");
        }

        if (!member.isEligibleToBorrow(currentActiveLoans)) {
            reason.append("Member has reached borrowing limit (")
                    .append(currentActiveLoans).append("/").append(member.getMaxSimultaneousLoans())
                    .append(" active loans). ");
        }

        if (!book.canBeBorrowed()) {
            reason.append("Book '").append(book.getTitle().value())
                    .append("' (ISBN: ").append(book.getIsbn().value())
                    .append(") is not available.");
        }

        return reason.toString();
    }
}

