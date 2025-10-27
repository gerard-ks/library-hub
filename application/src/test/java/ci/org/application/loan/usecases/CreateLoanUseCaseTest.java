package ci.org.application.loan.usecases;

import ci.org.application.loan.commands.CreateLoanCommand;
import ci.org.application.loan.dtos.LoanResponse;
import ci.org.domain.loan.enitites.Loan;
import ci.org.domain.loan.exceptions.InvalidLoanPeriodException;
import ci.org.domain.loan.repositories.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

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

    private CreateLoanUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateLoanUseCase(loanRepository);
    }

    @Test
    @DisplayName("Should create loan successfully")
    void shouldCreateLoanSuccessfully() {
        // Given
        String memberId = "member-123";
        String bookId = "book-456";
        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(14);

        CreateLoanCommand command = new CreateLoanCommand(memberId, bookId, borrowDate, dueDate);
        Loan savedLoan = Loan.create(memberId, bookId, borrowDate, dueDate);

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
    @DisplayName("Should throw exception when due date is before borrow date")
    void shouldThrowExceptionWhenDueDateIsBeforeBorrowDate() {
        // Given
        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.minusDays(1);  // Invalide

        CreateLoanCommand command = new CreateLoanCommand("member-123", "book-456", borrowDate, dueDate);

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
        LocalDate date = LocalDate.now();

        CreateLoanCommand command = new CreateLoanCommand("member-123", "book-456", date, date);

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