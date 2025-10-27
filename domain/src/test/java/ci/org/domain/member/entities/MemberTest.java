package ci.org.domain.member.entities;

import ci.org.domain.member.exceptions.InvalidBorrowingLimitException;
import ci.org.domain.member.exceptions.InvalidMemberNameException;
import ci.org.domain.member.valueobjects.MemberId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires essentiels pour l'aggregate Member.
 *
 * Focus sur :
 * - Validation des règles métier principales
 * - Edge cases importants
 * - Scénario complet d'utilisation
 */
@DisplayName("Member Domain Tests")
class MemberTest {

    @Nested
    @DisplayName("Creation and Name Validation")
    class CreationTests {

        @Test
        @DisplayName("Should create member with valid name")
        void shouldCreateMemberWithValidName() {
            // When
            Member member = Member.create("John Doe");

            // Then
            assertNotNull(member);
            assertNotNull(member.getId());
            assertEquals("John Doe", member.getNameValue());
            assertTrue(member.canBorrow());
            assertEquals(5, member.getMaxSimultaneousLoans());
        }

        @Test
        @DisplayName("Should create member with minimum name length (2 chars)")
        void shouldCreateMemberWithMinimumNameLength() {
            // When
            Member member = Member.create("Ab");

            // Then
            assertEquals("Ab", member.getNameValue());
        }

        @Test
        @DisplayName("Should trim whitespace from name")
        void shouldTrimWhitespaceFromName() {
            // When
            Member member = Member.create("  John Doe  ");

            // Then
            assertEquals("John Doe", member.getNameValue());
        }

        @Test
        @DisplayName("Should throw exception when name is null")
        void shouldThrowExceptionWhenNameIsNull() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                Member.create(null);
            });
        }

        @Test
        @DisplayName("Should throw exception when name is too short")
        void shouldThrowExceptionWhenNameIsTooShort() {
            // When & Then
            assertThrows(InvalidMemberNameException.class, () -> {
                Member.create("A");
            });
        }
    }

    @Nested
    @DisplayName("Borrowing Eligibility")
    class EligibilityTests {

        @Test
        @DisplayName("Should be eligible when can borrow and under limit")
        void shouldBeEligibleWhenCanBorrowAndUnderLimit() {
            // Given
            Member member = Member.create("John Doe");

            // When
            boolean eligible = member.isEligibleToBorrow(3);

            // Then
            assertTrue(eligible);  // 3 < 5
        }

        @Test
        @DisplayName("Should not be eligible when limit is reached")
        void shouldNotBeEligibleWhenLimitIsReached() {
            // Given
            Member member = Member.create("John Doe");

            // When
            boolean eligible = member.isEligibleToBorrow(5);

            // Then
            assertFalse(eligible);  // 5 = 5 (edge case important)
        }

        @Test
        @DisplayName("Should not be eligible when cannot borrow")
        void shouldNotBeEligibleWhenCannotBorrow() {
            // Given
            Member member = Member.create("John Doe");
            member.forbidBorrowing();

            // When
            boolean eligible = member.isEligibleToBorrow(2);

            // Then
            assertFalse(eligible);
        }

        @Test
        @DisplayName("Should throw exception when current active loans is negative")
        void shouldThrowExceptionWhenCurrentActiveLoansIsNegative() {
            // Given
            Member member = Member.create("John Doe");

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                member.isEligibleToBorrow(-1);
            });
        }
    }

    @Nested
    @DisplayName("Borrowing Permission Management")
    class PermissionTests {

        @Test
        @DisplayName("Should forbid borrowing")
        void shouldForbidBorrowing() {
            // Given
            Member member = Member.create("John Doe");
            assertTrue(member.canBorrow());

            // When
            member.forbidBorrowing();

            // Then
            assertFalse(member.canBorrow());
        }

        @Test
        @DisplayName("Should allow borrowing")
        void shouldAllowBorrowing() {
            // Given
            Member member = Member.create("John Doe");
            member.forbidBorrowing();

            // When
            member.allowBorrowing();

            // Then
            assertTrue(member.canBorrow());
        }
    }

    @Nested
    @DisplayName("Borrowing Limit Adjustment")
    class LimitAdjustmentTests {

        @Test
        @DisplayName("Should adjust limit to valid value")
        void shouldAdjustLimitToValidValue() {
            // Given
            Member member = Member.create("John Doe");

            // When
            member.adjustBorrowingLimit(10);

            // Then
            assertEquals(10, member.getMaxSimultaneousLoans());
        }

        @Test
        @DisplayName("Should adjust limit to minimum (1)")
        void shouldAdjustLimitToMinimum() {
            // Given
            Member member = Member.create("John Doe");

            // When
            member.adjustBorrowingLimit(1);

            // Then
            assertEquals(1, member.getMaxSimultaneousLoans());
        }

        @Test
        @DisplayName("Should adjust limit to maximum (20)")
        void shouldAdjustLimitToMaximum() {
            // Given
            Member member = Member.create("John Doe");

            // When
            member.adjustBorrowingLimit(20);

            // Then
            assertEquals(20, member.getMaxSimultaneousLoans());
        }

        @Test
        @DisplayName("Should throw exception when limit is zero")
        void shouldThrowExceptionWhenLimitIsZero() {
            // Given
            Member member = Member.create("John Doe");

            // When & Then
            assertThrows(InvalidBorrowingLimitException.class, () -> {
                member.adjustBorrowingLimit(0);
            });
        }
    }

    @Nested
    @DisplayName("Name Update")
    class NameUpdateTests {

        @Test
        @DisplayName("Should update name with valid value")
        void shouldUpdateNameWithValidValue() {
            // Given
            Member member = Member.create("John Doe");

            // When
            member.updateName("Jane Smith");

            // Then
            assertEquals("Jane Smith", member.getNameValue());
        }

        @Test
        @DisplayName("Should throw exception when updating to invalid name")
        void shouldThrowExceptionWhenUpdatingToInvalidName() {
            // Given
            Member member = Member.create("John Doe");

            // When & Then
            assertThrows(InvalidMemberNameException.class, () -> {
                member.updateName("A");
            });
        }
    }

    @Nested
    @DisplayName("Identity and Equality")
    class IdentityTests {

        @Test
        @DisplayName("Should be equal when same ID")
        void shouldBeEqualWhenSameId() {
            // Given
            MemberId id = MemberId.generate();
            Member member1 = Member.reconstitute(id, "John Doe", true, 5);
            Member member2 = Member.reconstitute(id, "Jane Smith", false, 10);

            // When & Then
            assertEquals(member1, member2);
            assertEquals(member1.hashCode(), member2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when different IDs")
        void shouldNotBeEqualWhenDifferentIds() {
            // Given
            Member member1 = Member.create("John Doe");
            Member member2 = Member.create("John Doe");

            // When & Then
            assertNotEquals(member1, member2);
        }
    }

    @Nested
    @DisplayName("Complete Lifecycle")
    class LifecycleTest {

        @Test
        @DisplayName("Should handle complete member lifecycle scenario")
        void shouldHandleCompleteMemberLifecycle() {
            // 1. Create member
            Member member = Member.create("John Doe");
            assertTrue(member.canBorrow());
            assertEquals(5, member.getMaxSimultaneousLoans());

            // 2. Check eligibility - can borrow
            assertTrue(member.isEligibleToBorrow(0));
            assertTrue(member.isEligibleToBorrow(4));

            // 3. Reach limit - cannot borrow
            assertFalse(member.isEligibleToBorrow(5));

            // 4. Forbid borrowing - cannot borrow even with 0 loans
            member.forbidBorrowing();
            assertFalse(member.isEligibleToBorrow(0));

            // 5. Allow borrowing - can borrow again
            member.allowBorrowing();
            assertTrue(member.isEligibleToBorrow(0));

            // 6. Upgrade to premium (10 loans)
            member.adjustBorrowingLimit(10);
            assertTrue(member.isEligibleToBorrow(9));
            assertFalse(member.isEligibleToBorrow(10));

            // 7. Update name
            member.updateName("John Smith");
            assertEquals("John Smith", member.getNameValue());
        }
    }
}