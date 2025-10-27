package ci.org.domain.loan.services;

import ci.org.domain.book.entities.Book;
import ci.org.domain.member.entities.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires essentiels pour le Domain Service LoanEligibilityService.
 *
 * Focus sur :
 * - Coordination entre Member et Book
 * - Règles d'éligibilité combinées
 * - Validation des paramètres
 */
@DisplayName("LoanEligibilityService Tests")
class LoanEligibilityServiceTest {

    private LoanEligibilityService service;

    @BeforeEach
    void setUp() {
        service = new LoanEligibilityService();
    }

    @Nested
    @DisplayName("Can Member Borrow Book")
    class CanMemberBorrowBookTests {

        @Test
        @DisplayName("Should allow when member is eligible and book is available")
        void shouldAllowWhenMemberIsEligibleAndBookIsAvailable() {
            // Given
            Member member = Member.create("John Doe");
            Book book = Book.create("1234567890", "Clean Code", "Robert Martin");
            int currentActiveLoans = 2;  // Sous la limite de 5

            // When
            boolean canBorrow = service.canMemberBorrowBook(member, book, currentActiveLoans);

            // Then
            assertTrue(canBorrow);
        }

        @Test
        @DisplayName("Should deny when member has reached borrowing limit")
        void shouldDenyWhenMemberHasReachedBorrowingLimit() {
            // Given
            Member member = Member.create("John Doe");
            Book book = Book.create("1234567890", "Clean Code", "Robert Martin");
            int currentActiveLoans = 5;  // Limite atteinte

            // When
            boolean canBorrow = service.canMemberBorrowBook(member, book, currentActiveLoans);

            // Then
            assertFalse(canBorrow);
        }

        @Test
        @DisplayName("Should deny when member cannot borrow")
        void shouldDenyWhenMemberCannotBorrow() {
            // Given
            Member member = Member.create("John Doe");
            member.forbidBorrowing();
            Book book = Book.create("1234567890", "Clean Code", "Robert Martin");
            int currentActiveLoans = 0;

            // When
            boolean canBorrow = service.canMemberBorrowBook(member, book, currentActiveLoans);

            // Then
            assertFalse(canBorrow);
        }

        @Test
        @DisplayName("Should deny when book is not available")
        void shouldDenyWhenBookIsNotAvailable() {
            // Given
            Member member = Member.create("John Doe");
            Book book = Book.create("1234567890", "Clean Code", "Robert Martin");
            book.markAsUnavailable();
            int currentActiveLoans = 2;

            // When
            boolean canBorrow = service.canMemberBorrowBook(member, book, currentActiveLoans);

            // Then
            assertFalse(canBorrow);
        }

        @Test
        @DisplayName("Should deny when both member and book are not eligible")
        void shouldDenyWhenBothNotEligible() {
            // Given
            Member member = Member.create("John Doe");
            member.forbidBorrowing();
            Book book = Book.create("1234567890", "Clean Code", "Robert Martin");
            book.markAsUnavailable();
            int currentActiveLoans = 0;

            // When
            boolean canBorrow = service.canMemberBorrowBook(member, book, currentActiveLoans);

            // Then
            assertFalse(canBorrow);
        }

        @Test
        @DisplayName("Should throw exception when member is null")
        void shouldThrowExceptionWhenMemberIsNull() {
            // Given
            Book book = Book.create("1234567890", "Clean Code", "Robert Martin");

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                service.canMemberBorrowBook(null, book, 0);
            });
        }

        @Test
        @DisplayName("Should throw exception when book is null")
        void shouldThrowExceptionWhenBookIsNull() {
            // Given
            Member member = Member.create("John Doe");

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                service.canMemberBorrowBook(member, null, 0);
            });
        }

        @Test
        @DisplayName("Should throw exception when current active loans is negative")
        void shouldThrowExceptionWhenCurrentActiveLoansIsNegative() {
            // Given
            Member member = Member.create("John Doe");
            Book book = Book.create("1234567890", "Clean Code", "Robert Martin");

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                service.canMemberBorrowBook(member, book, -1);
            });
        }
    }

    @Nested
    @DisplayName("Is Member Eligible To Borrow")
    class IsMemberEligibleToBorrowTests {

        @Test
        @DisplayName("Should be eligible when under limit and allowed")
        void shouldBeEligibleWhenUnderLimitAndAllowed() {
            // Given
            Member member = Member.create("John Doe");

            // When
            boolean eligible = service.isMemberEligibleToBorrow(member, 3);

            // Then
            assertTrue(eligible);
        }

        @Test
        @DisplayName("Should not be eligible when limit reached")
        void shouldNotBeEligibleWhenLimitReached() {
            // Given
            Member member = Member.create("John Doe");

            // When
            boolean eligible = service.isMemberEligibleToBorrow(member, 5);

            // Then
            assertFalse(eligible);
        }

        @Test
        @DisplayName("Should not be eligible when forbidden to borrow")
        void shouldNotBeEligibleWhenForbiddenToBorrow() {
            // Given
            Member member = Member.create("John Doe");
            member.forbidBorrowing();

            // When
            boolean eligible = service.isMemberEligibleToBorrow(member, 0);

            // Then
            assertFalse(eligible);
        }
    }

    @Nested
    @DisplayName("Is Book Available For Borrowing")
    class IsBookAvailableForBorrowingTests {

        @Test
        @DisplayName("Should be available when book is available")
        void shouldBeAvailableWhenBookIsAvailable() {
            // Given
            Book book = Book.create("1234567890", "Clean Code", "Robert Martin");

            // When
            boolean available = service.isBookAvailableForBorrowing(book);

            // Then
            assertTrue(available);
        }

        @Test
        @DisplayName("Should not be available when book is unavailable")
        void shouldNotBeAvailableWhenBookIsUnavailable() {
            // Given
            Book book = Book.create("1234567890", "Clean Code", "Robert Martin");
            book.markAsUnavailable();

            // When
            boolean available = service.isBookAvailableForBorrowing(book);

            // Then
            assertFalse(available);
        }

        @Test
        @DisplayName("Should throw exception when book is null")
        void shouldThrowExceptionWhenBookIsNull() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                service.isBookAvailableForBorrowing(null);
            });
        }
    }

    @Nested
    @DisplayName("Complex Scenarios")
    class ComplexScenariosTests {

        @Test
        @DisplayName("Should handle member with adjusted borrowing limit")
        void shouldHandleMemberWithAdjustedBorrowingLimit() {
            // Given
            Member premiumMember = Member.create("Premium Member");
            premiumMember.adjustBorrowingLimit(10);  // Augmenté à 10
            Book book = Book.create("1234567890", "Clean Code", "Robert Martin");
            int currentActiveLoans = 8;

            // When
            boolean canBorrow = service.canMemberBorrowBook(premiumMember, book, currentActiveLoans);

            // Then
            assertTrue(canBorrow);  // 8 < 10
        }

        @Test
        @DisplayName("Should handle restricted member")
        void shouldHandleRestrictedMember() {
            // Given
            Member restrictedMember = Member.create("Restricted Member");
            restrictedMember.adjustBorrowingLimit(2);  // Limité à 2
            Book book = Book.create("1234567890", "Clean Code", "Robert Martin");
            int currentActiveLoans = 2;

            // When
            boolean canBorrow = service.canMemberBorrowBook(restrictedMember, book, currentActiveLoans);

            // Then
            assertFalse(canBorrow);  // 2 = 2 (limite atteinte)
        }

        @Test
        @DisplayName("Should coordinate multiple eligibility checks")
        void shouldCoordinateMultipleEligibilityChecks() {
            // Given
            Member member = Member.create("John Doe");
            Book book1 = Book.create("1111111111", "Book 1", "Author 1");
            Book book2 = Book.create("2222222222", "Book 2", "Author 2");
            book2.markAsUnavailable();

            // When
            boolean canBorrowBook1 = service.canMemberBorrowBook(member, book1, 0);
            boolean canBorrowBook2 = service.canMemberBorrowBook(member, book2, 0);

            // Then
            assertTrue(canBorrowBook1);   // Member OK + Book1 available
            assertFalse(canBorrowBook2);  // Member OK BUT Book2 unavailable
        }
    }
}