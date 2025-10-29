package ci.org.application.loan.usecases;

import ci.org.application.loan.responses.LoanResponse;
import ci.org.application.loan.requests.queries.SearchLoansQuery;
import ci.org.domain.loan.enitites.Loan;
import ci.org.domain.loan.repositories.LoanRepository;
import ci.org.shared.kernel.Page;
import ci.org.shared.kernel.PageRequest;
import ci.org.shared.kernel.Sort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
        SearchLoansQuery query = SearchLoansQuery.forMember(memberId);

        LocalDate borrowDate1 = LocalDate.now().minusDays(30);
        LocalDate borrowDate2 = LocalDate.now().minusDays(15);
        LocalDate borrowDate3 = LocalDate.now().minusDays(5);

        Loan loan1 = Loan.create(memberId, "book-1", borrowDate1, borrowDate1.plusDays(14));
        loan1.markAsReturned(borrowDate1.plusDays(10));

        Loan loan2 = Loan.create(memberId, "book-2", borrowDate2, borrowDate2.plusDays(14));
        loan2.markAsOverdue();

        Loan loan3 = Loan.create(memberId, "book-3", borrowDate3, borrowDate3.plusDays(14));

        List<Loan> loans = Arrays.asList(loan1, loan2, loan3);
        PageRequest pageRequest = PageRequest.of(0, 20, Sort.by("borrowDate", Sort.Direction.DESC));
        Page<Loan> loanPage = Page.of(loans, pageRequest, 3);

        when(loanRepository.findByMemberId(eq(memberId), any(PageRequest.class))).thenReturn(loanPage);

        // When
        Page<LoanResponse> responsePage = useCase.execute(query);

        // Then
        assertNotNull(responsePage);
        assertEquals(3, responsePage.getContent().size());
        assertEquals(3, responsePage.getTotalElements());
        assertEquals(0, responsePage.getPageNumber());
        assertEquals(20, responsePage.getPageSize());

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
        SearchLoansQuery query = SearchLoansQuery.forMember(memberId);

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
        SearchLoansQuery query = new SearchLoansQuery(
                memberId, 1, 10, "borrowDate", "DESC"
        );

        LocalDate borrowDate = LocalDate.now().minusDays(10);
        List<Loan> loans = Arrays.asList(
                Loan.create(memberId, "book-11", borrowDate, borrowDate.plusDays(14)),
                Loan.create(memberId, "book-12", borrowDate, borrowDate.plusDays(14))
        );

        PageRequest pageRequest = PageRequest.of(1, 10, Sort.by("borrowDate", Sort.Direction.DESC));
        Page<Loan> loanPage = Page.of(loans, pageRequest, 25);

        when(loanRepository.findByMemberId(eq(memberId), any(PageRequest.class))).thenReturn(loanPage);

        // When
        Page<LoanResponse> responsePage = useCase.execute(query);

        // Then
        assertEquals(1, responsePage.getPageNumber());
        assertEquals(10, responsePage.getPageSize());
        assertEquals(25, responsePage.getTotalElements());
        assertEquals(3, responsePage.getTotalPages());
        assertEquals(2, responsePage.getNumberOfElements());

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
        SearchLoansQuery query = SearchLoansQuery.forMember(memberId);

        LocalDate borrowDate = LocalDate.now().minusDays(10);
        List<Loan> loans = Arrays.asList(
                Loan.create(memberId, "book-1", borrowDate, borrowDate.plusDays(14)),
                Loan.create(memberId, "book-2", borrowDate, borrowDate.plusDays(14)),
                Loan.create(memberId, "book-3", borrowDate, borrowDate.plusDays(14))
        );

        PageRequest pageRequest = PageRequest.of(0, 20, Sort.by("borrowDate", Sort.Direction.DESC));
        Page<Loan> loanPage = Page.of(loans, pageRequest, 3);

        when(loanRepository.findByMemberId(eq(memberId), any(PageRequest.class))).thenReturn(loanPage);

        // When
        Page<LoanResponse> responsePage = useCase.execute(query);

        // Then
        responsePage.getContent().forEach(loan -> {
            assertEquals(memberId, loan.memberId());
        });

        verify(loanRepository).findByMemberId(eq(memberId), any(PageRequest.class));
    }

    @Test
    @DisplayName("Should throw exception when member ID is null or blank")
    void shouldThrowExceptionWhenMemberIdIsNullOrBlank() {
        // Given - Query sans memberId
        SearchLoansQuery queryWithoutMemberId = new SearchLoansQuery(
                null, 0, 20, "borrowDate", "DESC"
        );

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(queryWithoutMemberId)
        );

        assertEquals("Member ID is required for this query", exception.getMessage());
        verify(loanRepository, never()).findByMemberId(any(), any(PageRequest.class));
    }

    @Test
    @DisplayName("Should use default values when using factory method")
    void shouldUseDefaultValuesWhenUsingFactoryMethod() {
        // Given
        String memberId = "member-123";
        SearchLoansQuery query = SearchLoansQuery.forMember(memberId);

        // Then - Vérifie les valeurs par défaut
        assertEquals(memberId, query.memberId());
        assertEquals(0, query.page());
        assertEquals(20, query.size());
        assertEquals("borrowDate", query.sortBy());
        assertEquals("DESC", query.sortDirection());
        assertTrue(query.hasMemberFilter());
    }

    @Test
    @DisplayName("Should create correct PageRequest from query")
    void shouldCreateCorrectPageRequestFromQuery() {
        // Given
        String memberId = "member-123";
        SearchLoansQuery query = new SearchLoansQuery(
                memberId, 2, 15, "dueDate", "ASC"
        );

        PageRequest expectedPageRequest = PageRequest.of(2, 15, Sort.by("dueDate", Sort.Direction.ASC));

        when(loanRepository.findByMemberId(eq(memberId), any(PageRequest.class)))
                .thenReturn(Page.of(Collections.emptyList(), expectedPageRequest, 0));

        // When
        useCase.execute(query);

        // Then - Vérifie que le PageRequest est correctement construit
        ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(loanRepository).findByMemberId(eq(memberId), pageRequestCaptor.capture());

        PageRequest capturedPageRequest = pageRequestCaptor.getValue();
        assertEquals(2, capturedPageRequest.pageNumber());
        assertEquals(15, capturedPageRequest.pageSize());

        // Vérifier le tri
        assertTrue(capturedPageRequest.sort().isSorted());
        assertEquals(1, capturedPageRequest.sort().getOrders().size());

        Sort.Order order = capturedPageRequest.sort().getOrders().get(0);
        assertEquals("dueDate", order.property());
        assertEquals(Sort.Direction.ASC, order.direction());
    }

    @Test
    @DisplayName("Should verify hasMemberFilter returns true when memberId is provided")
    void shouldVerifyHasMemberFilterReturnsTrueWhenMemberIdProvided() {
        // Given
        SearchLoansQuery query = SearchLoansQuery.forMember("member-123");

        // Then
        assertTrue(query.hasMemberFilter());
    }

    @Test
    @DisplayName("Should verify hasMemberFilter returns false when memberId is null")
    void shouldVerifyHasMemberFilterReturnsFalseWhenMemberIdNull() {
        // Given
        SearchLoansQuery query = SearchLoansQuery.forActiveLoans();

        // Then
        assertFalse(query.hasMemberFilter());
    }

    @Test
    @DisplayName("Should throw when creating forMember with null memberId")
    void shouldThrowWhenCreatingForMemberWithNullMemberId() {
        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> SearchLoansQuery.forMember(null)
        );
    }

    @Test
    @DisplayName("Should throw when creating forMember with blank memberId")
    void shouldThrowWhenCreatingForMemberWithBlankMemberId() {
        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> SearchLoansQuery.forMember("   ")
        );
    }

    @Test
    @DisplayName("Should calculate offset correctly for member loans pagination")
    void shouldCalculateOffsetCorrectlyForPagination() {
        // Given
        String memberId = "member-123";
        SearchLoansQuery query = new SearchLoansQuery(
                memberId, 4, 10, "borrowDate", "DESC"
        );

        PageRequest pageRequest = PageRequest.of(4, 10, Sort.by("borrowDate", Sort.Direction.DESC));
        Page<Loan> loanPage = Page.of(Collections.emptyList(), pageRequest, 50);

        when(loanRepository.findByMemberId(eq(memberId), any(PageRequest.class))).thenReturn(loanPage);

        // When
        useCase.execute(query);

        // Then
        ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
        verify(loanRepository).findByMemberId(eq(memberId), captor.capture());

        PageRequest captured = captor.getValue();
        assertEquals(40, captured.getOffset()); // page 4, size 10 => offset 40
    }
}