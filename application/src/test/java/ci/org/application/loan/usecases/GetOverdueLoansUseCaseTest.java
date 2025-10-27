package ci.org.application.loan.usecases;

import ci.org.application.loan.dtos.LoanResponse;
import ci.org.application.loan.queries.GetOverdueLoansQuery;
import ci.org.domain.loan.enitites.Loan;
import ci.org.domain.loan.repositories.LoanRepository;
import ci.org.shared.kernel.Page;
import ci.org.shared.kernel.PageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour GetOverdueLoansUseCase (avec pagination).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetOverdueLoansUseCase Tests")
class GetOverdueLoansUseCaseTest {

    @Mock
    private LoanRepository loanRepository;

    private GetOverdueLoansUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetOverdueLoansUseCase(loanRepository);
    }

    @Test
    @DisplayName("Should get all overdue loans with pagination successfully")
    void shouldGetAllOverdueLoansWithPaginationSuccessfully() {
        // Given
        GetOverdueLoansQuery query = new GetOverdueLoansQuery(0, 20, "dueDate", "ASC");

        LocalDate borrowDate1 = LocalDate.now().minusDays(30);
        LocalDate borrowDate2 = LocalDate.now().minusDays(25);
        LocalDate borrowDate3 = LocalDate.now().minusDays(20);

        Loan loan1 = Loan.create("member-1", "book-1", borrowDate1, borrowDate1.plusDays(14));
        loan1.markAsOverdue();

        Loan loan2 = Loan.create("member-2", "book-2", borrowDate2, borrowDate2.plusDays(14));
        loan2.markAsOverdue();

        Loan loan3 = Loan.create("member-3", "book-3", borrowDate3, borrowDate3.plusDays(14));
        loan3.markAsOverdue();

        List<Loan> loans = Arrays.asList(loan1, loan2, loan3);
        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<Loan> loanPage = Page.of(loans, pageRequest, 3);

        when(loanRepository.findOverdueLoans(any(PageRequest.class))).thenReturn(loanPage);

        // When
        Page<LoanResponse> responsePage = useCase.execute(query);

        // Then
        assertNotNull(responsePage);
        assertEquals(3, responsePage.getContent().size());
        assertEquals(3, responsePage.getTotalElements());

        // Tous les statuts doivent être OVERDUE
        responsePage.getContent().forEach(loan -> {
            assertEquals("OVERDUE", loan.status());
            assertNull(loan.returnDate());
        });

        verify(loanRepository, times(1)).findOverdueLoans(any(PageRequest.class));
    }

    @Test
    @DisplayName("Should return empty page when no overdue loans")
    void shouldReturnEmptyPageWhenNoOverdueLoans() {
        // Given
        GetOverdueLoansQuery query = new GetOverdueLoansQuery(0, 20, "dueDate", "ASC");

        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<Loan> emptyPage = Page.of(Collections.emptyList(), pageRequest, 0);

        when(loanRepository.findOverdueLoans(any(PageRequest.class))).thenReturn(emptyPage);

        // When
        Page<LoanResponse> responsePage = useCase.execute(query);

        // Then
        assertNotNull(responsePage);
        assertTrue(responsePage.isEmpty());
        assertEquals(0, responsePage.getTotalElements());

        verify(loanRepository, times(1)).findOverdueLoans(any(PageRequest.class));
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void shouldHandlePaginationCorrectly() {
        // Given
        GetOverdueLoansQuery query = new GetOverdueLoansQuery(0, 10, "dueDate", "ASC");

        LocalDate borrowDate = LocalDate.now().minusDays(30);
        List<Loan> loans = Arrays.asList(
                Loan.create("member-1", "book-1", borrowDate, borrowDate.plusDays(14)),
                Loan.create("member-2", "book-2", borrowDate, borrowDate.plusDays(14))
        );
        loans.forEach(Loan::markAsOverdue);

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Loan> loanPage = Page.of(loans, pageRequest, 15);

        when(loanRepository.findOverdueLoans(any(PageRequest.class))).thenReturn(loanPage);

        // When
        Page<LoanResponse> responsePage = useCase.execute(query);

        // Then
        assertEquals(0, responsePage.getPageNumber());
        assertEquals(10, responsePage.getPageSize());
        assertEquals(15, responsePage.getTotalElements());
        assertEquals(2, responsePage.getTotalPages());

        assertTrue(responsePage.isFirst());
        assertFalse(responsePage.isLast());
        assertTrue(responsePage.hasNext());
    }

    @Test
    @DisplayName("Should throw exception when query is null")
    void shouldThrowExceptionWhenQueryIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            useCase.execute(null);
        });

        verify(loanRepository, never()).findOverdueLoans(any(PageRequest.class));
    }
}