package ci.org.application.loan.usecases;

import ci.org.application.loan.requests.commands.CreateLoanCommand;
import ci.org.application.loan.responses.LoanResponse;
import ci.org.domain.book.entities.Book;
import ci.org.domain.book.exceptions.BookNotFoundException;
import ci.org.domain.book.repositories.BookRepository;
import ci.org.domain.book.valueobjects.BookId;
import ci.org.domain.loan.enitites.Loan;
import ci.org.domain.loan.exceptions.InvalidLoanPeriodException;
import ci.org.domain.loan.exceptions.LoanNotAllowedException;
import ci.org.domain.loan.repositories.LoanRepository;
import ci.org.domain.loan.services.LoanEligibilityService;
import ci.org.domain.member.entities.Member;
import ci.org.domain.member.exceptions.MemberNotFoundException;
import ci.org.domain.member.repositories.MemberRepository;
import ci.org.domain.member.valueobjects.MemberId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour CreateLoanUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateLoanUseCase Tests")
class CreateLoanUseCaseTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LoanEligibilityService eligibilityService;

    private CreateLoanUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateLoanUseCase(loanRepository, memberRepository, bookRepository, eligibilityService);
    }

    @Test
    @DisplayName("Should create loan successfully")
    void shouldCreateLoanSuccessfully() {
        // Given
        String memberId = "member-123";
        String bookId = "book-456";
        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(14);

        Member member = Member.create("John Doe");
        Book book = Book.create("978-0-13-235088-4", "Clean Code", "Robert Martin");
        CreateLoanCommand command = new CreateLoanCommand(memberId, bookId, borrowDate, dueDate);
        Loan savedLoan = Loan.create(memberId, bookId, borrowDate, dueDate);

        when(memberRepository.findById(any(MemberId.class))).thenReturn(Optional.of(member));
        when(bookRepository.findById(any(BookId.class))).thenReturn(Optional.of(book));
        when(loanRepository.countActiveByMemberId(memberId)).thenReturn(0L);
        when(eligibilityService.canMemberBorrowBook(any(Member.class), any(Book.class), anyInt())).thenReturn(true);
        when(loanRepository.save(any(Loan.class))).thenReturn(savedLoan);

        // When
        LoanResponse response = useCase.execute(command);

        // Then
        assertNotNull(response);
        assertEquals(memberId, response.memberId());
        assertEquals(bookId, response.bookId());
        assertEquals(borrowDate, response.borrowDate());
        assertEquals(dueDate, response.dueDate());
        assertEquals("ACTIVE", response.status());
        assertNull(response.returnDate());

        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @Test
    @DisplayName("Should throw exception when member not found")
    void shouldThrowExceptionWhenMemberNotFound() {
        // Given
        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(14);
        CreateLoanCommand command = new CreateLoanCommand("non-existent-member", "book-456", borrowDate, dueDate);

        when(memberRepository.findById(any(MemberId.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(MemberNotFoundException.class, () -> {
            useCase.execute(command);
        });

        verify(memberRepository, times(1)).findById(any(MemberId.class));
        verify(bookRepository, never()).findById(any(BookId.class));
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    @DisplayName("Should throw exception when book not found")
    void shouldThrowExceptionWhenBookNotFound() {
        // Given
        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(14);
        CreateLoanCommand command = new CreateLoanCommand("member-123", "non-existent-book", borrowDate, dueDate);
        Member member = Member.create("John Doe");

        when(memberRepository.findById(any(MemberId.class))).thenReturn(Optional.of(member));
        when(bookRepository.findById(any(BookId.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BookNotFoundException.class, () -> {
            useCase.execute(command);
        });

        verify(memberRepository, times(1)).findById(any(MemberId.class));
        verify(bookRepository, times(1)).findById(any(BookId.class));
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    @DisplayName("Should throw exception when member not eligible")
    void shouldThrowExceptionWhenMemberNotEligible() {
        // Given
        String memberId = "member-123";
        String bookId = "book-456";
        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(14);

        Member member = Member.create("John Doe");
        Book book = Book.create("978-0-13-235088-4", "Clean Code", "Robert Martin");
        CreateLoanCommand command = new CreateLoanCommand(memberId, bookId, borrowDate, dueDate);

        when(memberRepository.findById(any(MemberId.class))).thenReturn(Optional.of(member));
        when(bookRepository.findById(any(BookId.class))).thenReturn(Optional.of(book));
        when(loanRepository.countActiveByMemberId(memberId)).thenReturn(0L);
        when(eligibilityService.canMemberBorrowBook(any(Member.class), any(Book.class), anyInt())).thenReturn(false);

        // When & Then
        assertThrows(LoanNotAllowedException.class, () -> {
            useCase.execute(command);
        });

        verify(eligibilityService, times(1)).canMemberBorrowBook(eq(member), eq(book), eq(0));
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    @DisplayName("Should throw exception when book not available")
    void shouldThrowExceptionWhenBookNotAvailable() {
        // Given
        String memberId = "member-123";
        String bookId = "book-456";
        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(14);

        Member member = Member.create("John Doe");
        Book book = Book.create("978-0-13-235088-4", "Clean Code", "Robert Martin");
        book.markAsUnavailable();
        CreateLoanCommand command = new CreateLoanCommand(memberId, bookId, borrowDate, dueDate);

        when(memberRepository.findById(any(MemberId.class))).thenReturn(Optional.of(member));
        when(bookRepository.findById(any(BookId.class))).thenReturn(Optional.of(book));
        when(loanRepository.countActiveByMemberId(memberId)).thenReturn(0L);
        when(eligibilityService.canMemberBorrowBook(any(Member.class), any(Book.class), anyInt())).thenReturn(false);

        // When & Then
        assertThrows(LoanNotAllowedException.class, () -> {
            useCase.execute(command);
        });

        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    @DisplayName("Should check active loans count before creating loan")
    void shouldCheckActiveLoansCountBeforeCreatingLoan() {
        // Given
        String memberId = "member-123";
        String bookId = "book-456";
        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(14);

        Member member = Member.create("John Doe");
        Book book = Book.create("978-0-13-235088-4", "Clean Code", "Robert Martin");
        CreateLoanCommand command = new CreateLoanCommand(memberId, bookId, borrowDate, dueDate);
        Loan savedLoan = Loan.create(memberId, bookId, borrowDate, dueDate);

        when(memberRepository.findById(any(MemberId.class))).thenReturn(Optional.of(member));
        when(bookRepository.findById(any(BookId.class))).thenReturn(Optional.of(book));
        when(loanRepository.countActiveByMemberId(memberId)).thenReturn(2L); // 2 emprunts actifs
        when(eligibilityService.canMemberBorrowBook(any(Member.class), any(Book.class), eq(2))).thenReturn(true);
        when(loanRepository.save(any(Loan.class))).thenReturn(savedLoan);

        // When
        LoanResponse response = useCase.execute(command);

        // Then
        assertNotNull(response);
        verify(loanRepository, times(1)).countActiveByMemberId(memberId);
        verify(eligibilityService, times(1)).canMemberBorrowBook(eq(member), eq(book), eq(2));
    }


    @Test
    @DisplayName("Should throw exception when due date is before borrow date")
    void shouldThrowExceptionWhenDueDateIsBeforeBorrowDate() {
        // Given
        String memberId = "member-123";
        String bookId = "book-456";
        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.minusDays(1);  // Invalide

        Member member = Member.create("John Doe");
        Book book = Book.create("978-0-13-235088-4", "Clean Code", "Robert Martin");
        CreateLoanCommand command = new CreateLoanCommand(memberId, bookId, borrowDate, dueDate);

        when(memberRepository.findById(any(MemberId.class))).thenReturn(Optional.of(member));
        when(bookRepository.findById(any(BookId.class))).thenReturn(Optional.of(book));
        when(loanRepository.countActiveByMemberId(memberId)).thenReturn(0L);
        when(eligibilityService.canMemberBorrowBook(any(Member.class), any(Book.class), anyInt())).thenReturn(true);


        // When & Then
        assertThrows(InvalidLoanPeriodException.class, () -> {
            useCase.execute(command);
        });

        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    @DisplayName("Should throw exception when due date equals borrow date")
    void shouldThrowExceptionWhenDueDateEqualsBorrowDate() {
        // Given
        String memberId = "member-123";
        String bookId = "book-456";
        LocalDate date = LocalDate.now();
        Member member = Member.create("John Doe");
        Book book = Book.create("978-0-13-235088-4", "Clean Code", "Robert Martin");

        CreateLoanCommand command = new CreateLoanCommand(memberId, bookId, date, date);

        when(memberRepository.findById(any(MemberId.class))).thenReturn(Optional.of(member));
        when(bookRepository.findById(any(BookId.class))).thenReturn(Optional.of(book));
        when(loanRepository.countActiveByMemberId(memberId)).thenReturn(0L);
        when(eligibilityService.canMemberBorrowBook(any(Member.class), any(Book.class), anyInt())).thenReturn(true);


        // When & Then
        assertThrows(InvalidLoanPeriodException.class, () -> {
            useCase.execute(command);
        });

        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    @DisplayName("Should throw exception when command is null")
    void shouldThrowExceptionWhenCommandIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            useCase.execute(null);
        });

        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    @DisplayName("Should throw exception when member ID is blank")
    void shouldThrowExceptionWhenMemberIdIsBlank() {
        // Given
        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(14);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            new CreateLoanCommand("", "book-456", borrowDate, dueDate);
        });
    }

    @Test
    @DisplayName("Should throw exception when book ID is blank")
    void shouldThrowExceptionWhenBookIdIsBlank() {
        // Given
        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(14);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            new CreateLoanCommand("member-123", "", borrowDate, dueDate);
        });
    }
}