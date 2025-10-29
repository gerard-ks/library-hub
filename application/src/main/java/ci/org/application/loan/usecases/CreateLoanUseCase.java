package ci.org.application.loan.usecases;

import ci.org.application.loan.requests.commands.CreateLoanCommand;
import ci.org.application.loan.responses.LoanResponse;
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

        Member member = memberRepository.findById(new MemberId(command.memberId()))
                .orElseThrow(() -> new MemberNotFoundException(command.memberId()));

        Book book = bookRepository.findById(new BookId(command.bookId()))
                .orElseThrow(() -> new BookNotFoundException(command.bookId()));

        long currentActiveLoans = loanRepository.countActiveByMemberId(command.memberId());

        boolean canBorrow = isEligible(
                member,
                book,
                (int) currentActiveLoans
        );

        if (!canBorrow) {
            String reason = buildLoanDenialReason(member, book, (int) currentActiveLoans);
            throw new LoanNotAllowedException(reason);
        }

        Loan savedLoan = loanRepository.save(Loan.create(
                command.memberId(),
                command.bookId(),
                command.borrowDate(),
                command.dueDate()
        ));

        return LoanMapper.toResponse(savedLoan);
    }

    private boolean isEligible(Member member, Book book, int currentActiveLoans) {
        return eligibilityService.canMemberBorrowBook(
               member,
                book,
                currentActiveLoans
        );
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

