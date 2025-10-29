package ci.org.application.loan.usecases;

import ci.org.application.loan.requests.commands.MarkLoanAsOverdueCommand;
import ci.org.application.loan.responses.LoanResponse;
import ci.org.domain.loan.enitites.Loan;
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
 * Tests unitaires pour MarkLoanAsOverdueUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MarkLoanAsOverdueUseCase Tests")
class MarkLoanAsOverdueUseCaseTest {

    @Mock
    private LoanRepository loanRepository;

    private MarkLoanAsOverdueUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new MarkLoanAsOverdueUseCase(loanRepository);
    }

    @Test
    @DisplayName("Should mark loan as overdue successfully")
    void shouldMarkLoanAsOverdueSuccessfully() {
        // Given
        LocalDate borrowDate = LocalDate.now().minusDays(20);
        LocalDate dueDate = borrowDate.plusDays(14);

        Loan loan = Loan.create("member-123", "book-456", borrowDate, dueDate);
        String loanId = loan.getId().value();
        MarkLoanAsOverdueCommand command = new MarkLoanAsOverdueCommand(loanId);

        assertEquals("ACTIVE", loan.getStatus().name());

        when(loanRepository.findById(any(LoanId.class))).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        // When
        LoanResponse response = useCase.execute(command);

        // Then
        assertNotNull(response);
        assertEquals("OVERDUE", response.status());

        verify(loanRepository, times(1)).findById(any(LoanId.class));
        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @Test
    @DisplayName("Should throw exception when loan not found")
    void shouldThrowExceptionWhenLoanNotFound() {
        // Given
        MarkLoanAsOverdueCommand command = new MarkLoanAsOverdueCommand("non-existent-id");

        when(loanRepository.findById(any(LoanId.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(LoanNotFoundException.class, () -> {
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
    @DisplayName("Should throw exception when loan is already returned")
    void shouldThrowExceptionWhenLoanIsAlreadyReturned() {
        // Given
        LocalDate borrowDate = LocalDate.now().minusDays(20);
        LocalDate dueDate = borrowDate.plusDays(14);
        LocalDate returnDate = LocalDate.now();

        Loan loan = Loan.create("member-123", "book-456", borrowDate, dueDate);
        loan.markAsReturned(returnDate);  // Retourné
        String loanId = loan.getId().value();

        MarkLoanAsOverdueCommand command = new MarkLoanAsOverdueCommand(loanId);

        when(loanRepository.findById(any(LoanId.class))).thenReturn(Optional.of(loan));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            useCase.execute(command);
        });

        verify(loanRepository, times(1)).findById(any(LoanId.class));
        verify(loanRepository, never()).save(any(Loan.class));
    }
}