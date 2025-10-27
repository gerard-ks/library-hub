package ci.org.application.loan.usecases;

import ci.org.application.loan.dtos.LoanResponse;
import ci.org.application.loan.queries.GetLoansByMemberIdQuery;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour GetLoansByMemberIdUseCase (avec pagination).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetLoansByMemberIdUseCase Tests")
class GetLoansByMemberIdUseCaseTest {

    @Mock
    private LoanRepository loanRepository;

    private GetLoansByMemberIdUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetLoansByMemberIdUseCase(loanRepository);
    }

    @Test
    @DisplayName("Should get all loans by member ID with pagination successfully")
    void shouldGetAllLoansByMemberIdWithPaginationSuccessfully() {
        // Given
        String memberId = "member-123";
        GetLoansByMemberIdQuery query = new GetLoansByMemberIdQuery(memberId, 0, 20, "borrowDate", "DESC");

        LocalDate borrowDate1 = LocalDate.now().minusDays(30);
        LocalDate borrowDate2 = LocalDate.now().minusDays(15);
        LocalDate borrowDate3 = LocalDate.now().minusDays(5);

        Loan loan1 = Loan.create(memberId, "book-1", borrowDate1, borrowDate1.plusDays(14));
        loan1.markAsReturned(borrowDate1.plusDays(10));

        Loan loan2 = Loan.create(memberId, "book-2", borrowDate2, borrowDate2.plusDays(14));
        loan2.markAsOverdue();

        Loan loan3 = Loan.create(memberId, "book-3", borrowDate3, borrowDate3.plusDays(14));

        List<Loan> loans = Arrays.asList(loan1, loan2, loan3);
        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<Loan> loanPage = Page.of(loans, pageRequest, 3);

        when(loanRepository.findByMemberId(eq(memberId), any(PageRequest.class))).thenReturn(loanPage);

        // When
        Page<LoanResponse> responsePage = useCase.execute(query);

        // Then
        assertNotNull(responsePage);
        assertEquals(3, responsePage.getContent().size());
        assertEquals(3, responsePage.getTotalElements());

        // Vérifier que tous les emprunts appartiennent au même membre
        responsePage.getContent().forEach(loan -> {
            assertEquals(memberId, loan.memberId());
        });

        // Vérifier les différents statuts
        assertEquals("RETURNED", responsePage.getContent().get(0).status());
        assertEquals("OVERDUE", responsePage.getContent().get(1).status());
        assertEquals("ACTIVE", responsePage.getContent().get(2).status());

        verify(loanRepository, times(1)).findByMemberId(eq(memberId), any(PageRequest.class));
    }

    @Test
    @DisplayName("Should return empty page when member has no loans")
    void shouldReturnEmptyPageWhenMemberHasNoLoans() {
        // Given
        String memberId = "member-without-loans";
        GetLoansByMemberIdQuery query = new GetLoansByMemberIdQuery(memberId, 0, 20, "borrowDate", "DESC");

        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<Loan> emptyPage = Page.of(Collections.emptyList(), pageRequest, 0);

        when(loanRepository.findByMemberId(eq(memberId), any(PageRequest.class))).thenReturn(emptyPage);

        // When
        Page<LoanResponse> responsePage = useCase.execute(query);

        // Then
        assertNotNull(responsePage);
        assertTrue(responsePage.isEmpty());
        assertEquals(0, responsePage.getTotalElements());

        verify(loanRepository, times(1)).findByMemberId(eq(memberId), any(PageRequest.class));
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void shouldHandlePaginationCorrectly() {
        // Given
        String memberId = "member-123";
        GetLoansByMemberIdQuery query = new GetLoansByMemberIdQuery(memberId, 1, 10, "borrowDate", "DESC");

        LocalDate borrowDate = LocalDate.now().minusDays(10);
        List<Loan> loans = Arrays.asList(
                Loan.create(memberId, "book-11", borrowDate, borrowDate.plusDays(14)),
                Loan.create(memberId, "book-12", borrowDate, borrowDate.plusDays(14))
        );

        PageRequest pageRequest = PageRequest.of(1, 10);
        Page<Loan> loanPage = Page.of(loans, pageRequest, 25);

        when(loanRepository.findByMemberId(eq(memberId), any(PageRequest.class))).thenReturn(loanPage);

        // When
        Page<LoanResponse> responsePage = useCase.execute(query);

        // Then
        assertEquals(1, responsePage.getPageNumber());
        assertEquals(10, responsePage.getPageSize());
        assertEquals(25, responsePage.getTotalElements());
        assertEquals(3, responsePage.getTotalPages());

        assertFalse(responsePage.isFirst());
        assertFalse(responsePage.isLast());
        assertTrue(responsePage.hasPrevious());
        assertTrue(responsePage.hasNext());
    }

    @Test
    @DisplayName("Should throw exception when query is null")
    void shouldThrowExceptionWhenQueryIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            useCase.execute(null);
        });

        verify(loanRepository, never()).findByMemberId(any(), any(PageRequest.class));
    }

    @Test
    @DisplayName("Should only return loans for specified member")
    void shouldOnlyReturnLoansForSpecifiedMember() {
        // Given
        String memberId = "member-123";
        GetLoansByMemberIdQuery query = new GetLoansByMemberIdQuery(memberId, 0, 20, "borrowDate", "DESC");

        LocalDate borrowDate = LocalDate.now().minusDays(10);
        List<Loan> loans = Arrays.asList(
                Loan.create(memberId, "book-1", borrowDate, borrowDate.plusDays(14)),
                Loan.create(memberId, "book-2", borrowDate, borrowDate.plusDays(14)),
                Loan.create(memberId, "book-3", borrowDate, borrowDate.plusDays(14))
        );

        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<Loan> loanPage = Page.of(loans, pageRequest, 3);

        when(loanRepository.findByMemberId(eq(memberId), any(PageRequest.class))).thenReturn(loanPage);

        // When
        Page<LoanResponse> responsePage = useCase.execute(query);

        // Then
        responsePage.getContent().forEach(loan -> {
            assertEquals(memberId, loan.memberId());
        });
    }
}