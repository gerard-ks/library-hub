package ci.org.domain.loan.entities;

import ci.org.domain.loan.enitites.Loan;
import ci.org.domain.loan.exceptions.InvalidLoanPeriodException;
import ci.org.domain.loan.exceptions.LoanAlreadyReturnedException;
import ci.org.domain.loan.valueobjects.LoanId;
import ci.org.domain.loan.valueobjects.LoanStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires essentiels pour l'aggregate Loan.
 *
 * Focus sur :
 * - Création et validation
 * - Gestion des retours
 * - Calcul des retards
 * - Transitions d'états
 */
@DisplayName("Loan Domain Tests")
class LoanTest {

    @Nested
    @DisplayName("Creation and Validation")
    class CreationTests {

        @Test
        @DisplayName("Should create loan with valid data")
        void shouldCreateLoanWithValidData() {
            // Given
            LocalDate borrowDate = LocalDate.of(2025, 1, 1);
            LocalDate dueDate = LocalDate.of(2025, 1, 15);

            // When
            Loan loan = Loan.create("member-1", "book-1", borrowDate, dueDate);

            // Then
            assertNotNull(loan);
            assertNotNull(loan.getId());
            assertEquals("member-1", loan.getMemberId());
            assertEquals("book-1", loan.getBookId());
            assertEquals(borrowDate, loan.getBorrowDate());
            assertEquals(dueDate, loan.getDueDate());
            assertEquals(LoanStatus.ACTIVE, loan.getStatus());
            assertNull(loan.getReturnDate());
            assertTrue(loan.isActive());
            assertFalse(loan.isReturned());
        }

        @Test
        @DisplayName("Should throw exception when due date is before borrow date")
        void shouldThrowExceptionWhenDueDateIsBeforeBorrowDate() {
            // Given
            LocalDate borrowDate = LocalDate.of(2025, 1, 15);
            LocalDate dueDate = LocalDate.of(2025, 1, 1);

            // When & Then
            assertThrows(InvalidLoanPeriodException.class, () -> {
                Loan.create("member-1", "book-1", borrowDate, dueDate);
            });
        }

        @Test
        @DisplayName("Should throw exception when due date equals borrow date")
        void shouldThrowExceptionWhenDueDateEqualsBorrowDate() {
            // Given
            LocalDate date = LocalDate.of(2025, 1, 1);

            // When & Then
            assertThrows(InvalidLoanPeriodException.class, () -> {
                Loan.create("member-1", "book-1", date, date);
            });
        }

        @Test
        @DisplayName("Should throw exception when member ID is null")
        void shouldThrowExceptionWhenMemberIdIsNull() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                Loan.create(null, "book-1", LocalDate.now(), LocalDate.now().plusDays(7));
            });
        }

        @Test
        @DisplayName("Should throw exception when book ID is null")
        void shouldThrowExceptionWhenBookIdIsNull() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                Loan.create("member-1", null, LocalDate.now(), LocalDate.now().plusDays(7));
            });
        }
    }

    @Nested
    @DisplayName("Return Management")
    class ReturnTests {

        @Test
        @DisplayName("Should mark active loan as returned")
        void shouldMarkActiveLoanAsReturned() {
            // Given
            Loan loan = Loan.create("member-1", "book-1",
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 1, 15));
            LocalDate returnDate = LocalDate.of(2025, 1, 10);

            // When
            loan.markAsReturned(returnDate);

            // Then
            assertEquals(LoanStatus.RETURNED, loan.getStatus());
            assertEquals(returnDate, loan.getReturnDate());
            assertTrue(loan.isReturned());
            assertFalse(loan.isActive());
        }

        @Test
        @DisplayName("Should mark overdue loan as returned")
        void shouldMarkOverdueLoanAsReturned() {
            // Given
            Loan loan = Loan.create("member-1", "book-1",
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 1, 15));
            loan.markAsOverdue();
            LocalDate returnDate = LocalDate.of(2025, 1, 20);

            // When
            loan.markAsReturned(returnDate);

            // Then
            assertEquals(LoanStatus.RETURNED, loan.getStatus());
            assertEquals(returnDate, loan.getReturnDate());
        }

        @Test
        @DisplayName("Should throw exception when marking returned loan as returned")
        void shouldThrowExceptionWhenMarkingReturnedLoanAsReturned() {
            // Given
            Loan loan = Loan.create("member-1", "book-1",
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 1, 15));
            loan.markAsReturned(LocalDate.of(2025, 1, 10));

            // When & Then
            assertThrows(LoanAlreadyReturnedException.class, () -> {
                loan.markAsReturned(LocalDate.of(2025, 1, 11));
            });
        }
    }

    @Nested
    @DisplayName("Overdue Management")
    class OverdueTests {

        @Test
        @DisplayName("Should identify loan as overdue when current date is after due date")
        void shouldIdentifyLoanAsOverdue() {
            // Given
            Loan loan = Loan.create("member-1", "book-1",
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 1, 15));
            LocalDate currentDate = LocalDate.of(2025, 1, 20);

            // When
            boolean overdue = loan.isOverdue(currentDate);

            // Then
            assertTrue(overdue);
        }

        @Test
        @DisplayName("Should not be overdue when current date is before due date")
        void shouldNotBeOverdueWhenBeforeDueDate() {
            // Given
            Loan loan = Loan.create("member-1", "book-1",
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 1, 15));
            LocalDate currentDate = LocalDate.of(2025, 1, 10);

            // When
            boolean overdue = loan.isOverdue(currentDate);

            // Then
            assertFalse(overdue);
        }

        @Test
        @DisplayName("Should mark active loan as overdue")
        void shouldMarkActiveLoanAsOverdue() {
            // Given
            Loan loan = Loan.create("member-1", "book-1",
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 1, 15));

            // When
            loan.markAsOverdue();

            // Then
            assertEquals(LoanStatus.OVERDUE, loan.getStatus());
        }

        @Test
        @DisplayName("Should throw exception when marking non-active loan as overdue")
        void shouldThrowExceptionWhenMarkingNonActiveLoanAsOverdue() {
            // Given
            Loan loan = Loan.create("member-1", "book-1",
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 1, 15));
            loan.markAsReturned(LocalDate.of(2025, 1, 10));

            // When & Then
            assertThrows(IllegalStateException.class, () -> {
                loan.markAsOverdue();
            });
        }
    }

    @Nested
    @DisplayName("Late Days Calculation")
    class LateDaysTests {

        @Test
        @DisplayName("Should calculate late days for returned loan")
        void shouldCalculateLateDaysForReturnedLoan() {
            // Given
            Loan loan = Loan.create("member-1", "book-1",
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 1, 15));
            loan.markAsReturned(LocalDate.of(2025, 1, 20));  // 5 jours de retard

            // When
            long lateDays = loan.calculateLateDays(LocalDate.now());

            // Then
            assertEquals(5, lateDays);
        }

        @Test
        @DisplayName("Should calculate late days for active overdue loan")
        void shouldCalculateLateDaysForActiveOverdueLoan() {
            // Given
            Loan loan = Loan.create("member-1", "book-1",
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 1, 15));
            LocalDate currentDate = LocalDate.of(2025, 1, 22);  // 7 jours de retard

            // When
            long lateDays = loan.calculateLateDays(currentDate);

            // Then
            assertEquals(7, lateDays);
        }

        @Test
        @DisplayName("Should return zero late days when returned on time")
        void shouldReturnZeroLateDaysWhenReturnedOnTime() {
            // Given
            Loan loan = Loan.create("member-1", "book-1",
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 1, 15));
            loan.markAsReturned(LocalDate.of(2025, 1, 10));  // Avant dueDate

            // When
            long lateDays = loan.calculateLateDays(LocalDate.now());

            // Then
            assertEquals(0, lateDays);
        }

        @Test
        @DisplayName("Should return zero late days when not yet overdue")
        void shouldReturnZeroLateDaysWhenNotYetOverdue() {
            // Given
            Loan loan = Loan.create("member-1", "book-1",
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 1, 15));
            LocalDate currentDate = LocalDate.of(2025, 1, 10);

            // When
            long lateDays = loan.calculateLateDays(currentDate);

            // Then
            assertEquals(0, lateDays);
        }
    }

    @Nested
    @DisplayName("Loan Duration")
    class DurationTests {

        @Test
        @DisplayName("Should calculate loan duration in days")
        void shouldCalculateLoanDurationInDays() {
            // Given
            Loan loan = Loan.create("member-1", "book-1",
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 1, 15));

            // When
            long duration = loan.getLoanDurationInDays();

            // Then
            assertEquals(14, duration);
        }
    }

    @Nested
    @DisplayName("Identity and Equality")
    class IdentityTests {

        @Test
        @DisplayName("Should be equal when same ID")
        void shouldBeEqualWhenSameId() {
            // Given
            LoanId id = LoanId.generate();
            Loan loan1 = Loan.reconstitute(id, "member-1", "book-1",
                    LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 15),
                    LoanStatus.ACTIVE, null);
            Loan loan2 = Loan.reconstitute(id, "member-2", "book-2",
                    LocalDate.of(2025, 2, 1), LocalDate.of(2025, 2, 15),
                    LoanStatus.RETURNED, LocalDate.now());

            // When & Then
            assertEquals(loan1, loan2);
            assertEquals(loan1.hashCode(), loan2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when different IDs")
        void shouldNotBeEqualWhenDifferentIds() {
            // Given
            Loan loan1 = Loan.create("member-1", "book-1",
                    LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 15));
            Loan loan2 = Loan.create("member-1", "book-1",
                    LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 15));

            // When & Then
            assertNotEquals(loan1, loan2);
        }
    }

    @Nested
    @DisplayName("Complete Lifecycle")
    class LifecycleTest {

        @Test
        @DisplayName("Should handle complete loan lifecycle scenario")
        void shouldHandleCompleteLoanLifecycle() {
            // 1. Create loan (ACTIVE)
            Loan loan = Loan.create("member-123", "book-456",
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 1, 15));

            assertEquals(LoanStatus.ACTIVE, loan.getStatus());
            assertTrue(loan.isActive());
            assertEquals(14, loan.getLoanDurationInDays());

            // 2. Check not overdue initially
            assertFalse(loan.isOverdue(LocalDate.of(2025, 1, 10)));
            assertEquals(0, loan.calculateLateDays(LocalDate.of(2025, 1, 10)));

            // 3. Loan becomes overdue
            assertTrue(loan.isOverdue(LocalDate.of(2025, 1, 20)));
            loan.markAsOverdue();
            assertEquals(LoanStatus.OVERDUE, loan.getStatus());
            assertEquals(5, loan.calculateLateDays(LocalDate.of(2025, 1, 20)));

            // 4. Return book late
            loan.markAsReturned(LocalDate.of(2025, 1, 22));
            assertEquals(LoanStatus.RETURNED, loan.getStatus());
            assertTrue(loan.isReturned());
            assertEquals(7, loan.calculateLateDays(LocalDate.now()));
        }
    }
}