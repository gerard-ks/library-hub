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
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour GetActiveLoansUseCase (avec pagination).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetActiveLoansUseCase Tests")
class GetActiveLoansUseCaseTest {

    @Mock
    private LoanRepository loanRepository;

    private GetActiveLoansUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetActiveLoansUseCase(loanRepository);
    }

    @Test
    @DisplayName("Should get all active loans with pagination successfully")
    void shouldGetAllActiveLoansWithPaginationSuccessfully() {
        // Given
        SearchLoansQuery query = SearchLoansQuery.forActiveLoans();

        LocalDate borrowDate1 = LocalDate.now().minusDays(10);
        LocalDate borrowDate2 = LocalDate.now().minusDays(5);
        LocalDate borrowDate3 = LocalDate.now().minusDays(2);

        Loan loan1 = Loan.create("member-1", "book-1", borrowDate1, borrowDate1.plusDays(14));
        Loan loan2 = Loan.create("member-2", "book-2", borrowDate2, borrowDate2.plusDays(14));
        Loan loan3 = Loan.create("member-3", "book-3", borrowDate3, borrowDate3.plusDays(14));

        List<Loan> loans = Arrays.asList(loan1, loan2, loan3);

        // Utiliser PageRequest avec Sort correct
        PageRequest pageRequest = PageRequest.of(0, 20, Sort.by("borrowDate", Sort.Direction.DESC));
        Page<Loan> loanPage = Page.of(loans, pageRequest, 3);

        when(loanRepository.findActiveLoans(any(PageRequest.class))).thenReturn(loanPage);

        // When
        Page<LoanResponse> responsePage = useCase.execute(query);

        // Then
        assertNotNull(responsePage);
        assertEquals(3, responsePage.getContent().size());
        assertEquals(3, responsePage.getTotalElements());
        assertEquals(0, responsePage.getPageNumber());
        assertEquals(20, responsePage.getPageSize());
        assertEquals(1, responsePage.getTotalPages());

        // Tous les statuts doivent être ACTIVE
        responsePage.getContent().forEach(loan -> {
            assertEquals("ACTIVE", loan.status());
            assertNull(loan.returnDate());
        });

        verify(loanRepository, times(1)).findActiveLoans(any(PageRequest.class));
    }

    @Test
    @DisplayName("Should return empty page when no active loans")
    void shouldReturnEmptyPageWhenNoActiveLoans() {
        // Given
        SearchLoansQuery query = SearchLoansQuery.forActiveLoans();

        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<Loan> emptyPage = Page.of(Collections.emptyList(), pageRequest, 0);

        when(loanRepository.findActiveLoans(any(PageRequest.class))).thenReturn(emptyPage);

        // When
        Page<LoanResponse> responsePage = useCase.execute(query);

        // Then
        assertNotNull(responsePage);
        assertTrue(responsePage.isEmpty());
        assertEquals(0, responsePage.getTotalElements());

        verify(loanRepository, times(1)).findActiveLoans(any(PageRequest.class));
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void shouldHandlePaginationCorrectly() {
        // Given - Page 1 (10 éléments par page, total 25 éléments)
        SearchLoansQuery query = new SearchLoansQuery(
                null, 1, 10, "borrowDate", "DESC"
        );

        LocalDate borrowDate = LocalDate.now().minusDays(5);
        List<Loan> loans = Arrays.asList(
                Loan.create("member-11", "book-11", borrowDate, borrowDate.plusDays(14)),
                Loan.create("member-12", "book-12", borrowDate, borrowDate.plusDays(14)),
                Loan.create("member-13", "book-13", borrowDate, borrowDate.plusDays(14))
        );

        PageRequest pageRequest = PageRequest.of(1, 10, Sort.by("borrowDate", Sort.Direction.DESC));
        Page<Loan> loanPage = Page.of(loans, pageRequest, 25);

        when(loanRepository.findActiveLoans(any(PageRequest.class))).thenReturn(loanPage);

        // When
        Page<LoanResponse> responsePage = useCase.execute(query);

        // Then
        assertEquals(1, responsePage.getPageNumber());
        assertEquals(10, responsePage.getPageSize());
        assertEquals(25, responsePage.getTotalElements());
        assertEquals(3, responsePage.getTotalPages());
        assertEquals(3, responsePage.getNumberOfElements());

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

        verify(loanRepository, never()).findActiveLoans(any(PageRequest.class));
    }

    @Test
    @DisplayName("Should use default values when using factory method")
    void shouldUseDefaultValuesWhenUsingFactoryMethod() {
        // Given
        SearchLoansQuery query = SearchLoansQuery.forActiveLoans();

        // Then - Vérifie les valeurs par défaut
        assertEquals(0, query.page());
        assertEquals(20, query.size());
        assertEquals("borrowDate", query.sortBy());
        assertEquals("DESC", query.sortDirection());
        assertNull(query.memberId());
    }

    @Test
    @DisplayName("Should create correct PageRequest from query")
    void shouldCreateCorrectPageRequestFromQuery() {
        // Given
        SearchLoansQuery query = new SearchLoansQuery(
                null, 3, 25, "dueDate", "ASC"
        );

        PageRequest expectedPageRequest = PageRequest.of(3, 25, Sort.by("dueDate", Sort.Direction.ASC));

        when(loanRepository.findActiveLoans(any(PageRequest.class)))
                .thenReturn(Page.of(Collections.emptyList(), expectedPageRequest, 0));

        // When
        useCase.execute(query);

        // Then - Vérifie que le PageRequest est correctement construit
        ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(loanRepository).findActiveLoans(pageRequestCaptor.capture());

        PageRequest capturedPageRequest = pageRequestCaptor.getValue();
        assertEquals(3, capturedPageRequest.pageNumber());
        assertEquals(25, capturedPageRequest.pageSize());

        // Vérifier le tri
        assertTrue(capturedPageRequest.sort().isSorted());
        assertEquals(1, capturedPageRequest.sort().getOrders().size());

        Sort.Order order = capturedPageRequest.sort().getOrders().get(0);
        assertEquals("dueDate", order.property());
        assertEquals(Sort.Direction.ASC, order.direction());
    }

    @Test
    @DisplayName("Should handle custom sort direction")
    void shouldHandleCustomSortDirection() {
        // Given
        SearchLoansQuery query = new SearchLoansQuery(
                null, 0, 20, "memberId", "ASC"
        );

        PageRequest pageRequest = PageRequest.of(0, 20, Sort.by("memberId", Sort.Direction.ASC));
        Page<Loan> loanPage = Page.of(Collections.emptyList(), pageRequest, 0);

        when(loanRepository.findActiveLoans(any(PageRequest.class))).thenReturn(loanPage);

        // When
        useCase.execute(query);

        // Then
        ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
        verify(loanRepository).findActiveLoans(captor.capture());

        PageRequest captured = captor.getValue();
        Sort.Order order = captured.sort().getOrders().get(0);
        assertEquals("memberId", order.property());
        assertEquals(Sort.Direction.ASC, order.direction());
    }

    @Test
    @DisplayName("Should calculate offset correctly for pagination")
    void shouldCalculateOffsetCorrectlyForPagination() {
        // Given
        SearchLoansQuery query = new SearchLoansQuery(
                null, 2, 10, "borrowDate", "DESC"
        );

        PageRequest pageRequest = PageRequest.of(2, 10, Sort.by("borrowDate", Sort.Direction.DESC));
        Page<Loan> loanPage = Page.of(Collections.emptyList(), pageRequest, 30);

        when(loanRepository.findActiveLoans(any(PageRequest.class))).thenReturn(loanPage);

        // When
        useCase.execute(query);

        // Then
        ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
        verify(loanRepository).findActiveLoans(captor.capture());

        PageRequest captured = captor.getValue();
        assertEquals(20, captured.getOffset()); // page 2, size 10 => offset 20
    }
}