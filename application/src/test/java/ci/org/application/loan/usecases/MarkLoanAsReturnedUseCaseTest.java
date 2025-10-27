package ci.org.application.loan.usecases;

import ci.org.application.loan.commands.MarkLoanAsReturnedCommand;
import ci.org.application.loan.dtos.LoanResponse;
import ci.org.domain.loan.enitites.Loan;
import ci.org.domain.loan.exceptions.LoanAlreadyReturnedException;
import ci.org.domain.loan.exceptions.LoanNotFoundException;
import ci.org.domain.loan.repositories.LoanRepository;
import ci.org.domain.loan.valueobjects.LoanId;
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
 * Tests unitaires pour MarkLoanAsReturnedUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MarkLoanAsReturnedUseCase Tests")
class MarkLoanAsReturnedUseCaseTest {

    @Mock
    private LoanRepository loanRepository;

    private MarkLoanAsReturnedUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new MarkLoanAsReturnedUseCase(loanRepository);
    }

    @Test
    @DisplayName("Should mark loan as returned successfully")
    void shouldMarkLoanAsReturnedSuccessfully() {
        // Given
        LocalDate borrowDate = LocalDate.now().minusDays(10);
        LocalDate dueDate = borrowDate.plusDays(14);
        LocalDate returnDate = LocalDate.now();

        Loan loan = Loan.create("member-123", "book-456", borrowDate, dueDate);
        String loanId = loan.getId().value();
        MarkLoanAsReturnedCommand command = new MarkLoanAsReturnedCommand(loanId, returnDate);

        assertTrue(loan.isActive());
        assertNull(loan.getReturnDate());

        when(loanRepository.findById(any(LoanId.class))).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        // When
        LoanResponse response = useCase.execute(command);

        // Then
        assertNotNull(response);
        assertEquals("RETURNED", response.status());
        assertEquals(returnDate, response.returnDate());

        verify(loanRepository, times(1)).findById(any(LoanId.class));
        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @Test
    @DisplayName("Should throw exception when loan not found")
    void shouldThrowExceptionWhenLoanNotFound() {
        // Given
        LocalDate returnDate = LocalDate.now();
        MarkLoanAsReturnedCommand command = new MarkLoanAsReturnedCommand("non-existent-id", returnDate);

        when(loanRepository.findById(any(LoanId.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(LoanNotFoundException.class, () -> {
            useCase.execute(command);
        });

        verify(loanRepository, times(1)).findById(any(LoanId.class));
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    @DisplayName("Should throw exception when loan already returned")
    void shouldThrowExceptionWhenLoanAlreadyReturned() {
        // Given
        LocalDate borrowDate = LocalDate.now().minusDays(20);
        LocalDate dueDate = borrowDate.plusDays(14);
        LocalDate firstReturnDate = LocalDate.now().minusDays(5);
        LocalDate secondReturnDate = LocalDate.now();

        Loan loan = Loan.create("member-123", "book-456", borrowDate, dueDate);
        loan.markAsReturned(firstReturnDate);  // Déjà retourné
        String loanId = loan.getId().value();

        MarkLoanAsReturnedCommand command = new MarkLoanAsReturnedCommand(loanId, secondReturnDate);

        when(loanRepository.findById(any(LoanId.class))).thenReturn(Optional.of(loan));

        // When & Then
        assertThrows(LoanAlreadyReturnedException.class, () -> {
            useCase.execute(command);
        });

        verify(loanRepository, times(1)).findById(any(LoanId.class));
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    @DisplayName("Should throw exception when command is null")
    void shouldThrowExceptionWhenCommandIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            useCase.execute(null);
        });

        verify(loanRepository, never()).findById(any(LoanId.class));
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    @DisplayName("Should mark overdue loan as returned")
    void shouldMarkOverdueLoanAsReturned() {
        // Given
        LocalDate borrowDate = LocalDate.now().minusDays(20);
        LocalDate dueDate = borrowDate.plusDays(14);  // En retard
        LocalDate returnDate = LocalDate.now();

        Loan loan = Loan.create("member-123", "book-456", borrowDate, dueDate);
        loan.markAsOverdue();
        String loanId = loan.getId().value();

        MarkLoanAsReturnedCommand command = new MarkLoanAsReturnedCommand(loanId, returnDate);

        when(loanRepository.findById(any(LoanId.class))).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        // When
        LoanResponse response = useCase.execute(command);

        // Then
        assertEquals("RETURNED", response.status());
        assertEquals(returnDate, response.returnDate());
    }
}