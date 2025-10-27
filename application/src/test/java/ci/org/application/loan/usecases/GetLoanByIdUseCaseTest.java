package ci.org.application.loan.usecases;

import ci.org.application.loan.dtos.LoanResponse;
import ci.org.application.loan.queries.GetLoanByIdQuery;
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
 * Tests unitaires pour GetLoanByIdUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetLoanByIdUseCase Tests")
class GetLoanByIdUseCaseTest {

    @Mock
    private LoanRepository loanRepository;

    private GetLoanByIdUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetLoanByIdUseCase(loanRepository);
    }

    @Test
    @DisplayName("Should get loan by ID successfully")
    void shouldGetLoanByIdSuccessfully() {
        // Given
        LocalDate borrowDate = LocalDate.now().minusDays(10);
        LocalDate dueDate = borrowDate.plusDays(14);

        Loan loan = Loan.create("member-123", "book-456", borrowDate, dueDate);
        String loanId = loan.getId().value();
        GetLoanByIdQuery query = new GetLoanByIdQuery(loanId);

        when(loanRepository.findById(any(LoanId.class))).thenReturn(Optional.of(loan));

        // When
        LoanResponse response = useCase.execute(query);

        // Then
        assertNotNull(response);
        assertEquals(loanId, response.id());
        assertEquals("member-123", response.memberId());
        assertEquals("book-456", response.bookId());
        assertEquals(borrowDate, response.borrowDate());
        assertEquals(dueDate, response.dueDate());
        assertEquals("ACTIVE", response.status());
        assertNull(response.returnDate());

        verify(loanRepository, times(1)).findById(any(LoanId.class));
    }

    @Test
    @DisplayName("Should throw exception when loan not found")
    void shouldThrowExceptionWhenLoanNotFound() {
        // Given
        GetLoanByIdQuery query = new GetLoanByIdQuery("non-existent-id");

        when(loanRepository.findById(any(LoanId.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(LoanNotFoundException.class, () -> {
            useCase.execute(query);
        });

        verify(loanRepository, times(1)).findById(any(LoanId.class));
    }

    @Test
    @DisplayName("Should throw exception when query is null")
    void shouldThrowExceptionWhenQueryIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            useCase.execute(null);
        });

        verify(loanRepository, never()).findById(any(LoanId.class));
    }

    @Test
    @DisplayName("Should return loan with overdue status")
    void shouldReturnLoanWithOverdueStatus() {
        // Given
        LocalDate borrowDate = LocalDate.now().minusDays(20);
        LocalDate dueDate = borrowDate.plusDays(14);

        Loan loan = Loan.create("member-123", "book-456", borrowDate, dueDate);
        loan.markAsOverdue();
        String loanId = loan.getId().value();
        GetLoanByIdQuery query = new GetLoanByIdQuery(loanId);

        when(loanRepository.findById(any(LoanId.class))).thenReturn(Optional.of(loan));

        // When
        LoanResponse response = useCase.execute(query);

        // Then
        assertEquals("OVERDUE", response.status());
    }

    @Test
    @DisplayName("Should return loan with returned status and return date")
    void shouldReturnLoanWithReturnedStatusAndReturnDate() {
        // Given
        LocalDate borrowDate = LocalDate.now().minusDays(20);
        LocalDate dueDate = borrowDate.plusDays(14);
        LocalDate returnDate = LocalDate.now();

        Loan loan = Loan.create("member-123", "book-456", borrowDate, dueDate);
        loan.markAsReturned(returnDate);
        String loanId = loan.getId().value();
        GetLoanByIdQuery query = new GetLoanByIdQuery(loanId);

        when(loanRepository.findById(any(LoanId.class))).thenReturn(Optional.of(loan));

        // When
        LoanResponse response = useCase.execute(query);

        // Then
        assertEquals("RETURNED", response.status());
        assertEquals(returnDate, response.returnDate());
    }
}