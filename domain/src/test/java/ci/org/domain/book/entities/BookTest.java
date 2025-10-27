package ci.org.domain.book.entities;

import ci.org.domain.book.exceptions.BookAlreadyAvailableException;
import ci.org.domain.book.exceptions.BookAlreadyUnavailableException;
import ci.org.domain.book.exceptions.InvalidIsbnException;
import ci.org.domain.book.valueobjects.BookId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires essentiels pour l'aggregate Book.
 *
 * Focus sur :
 * - Validation ISBN (via Isbn VO)
 * - Gestion de la disponibilité
 * - Scénario complet d'utilisation
 */
@DisplayName("Book Domain Tests")
class BookTest {

    @Nested
    @DisplayName("Creation and ISBN Validation")
    class CreationTests {

        @Test
        @DisplayName("Should create book with valid ISBN-10")
        void shouldCreateBookWithValidIsbn10() {
            // When
            Book book = Book.create("1234567890", "Clean Code", "Robert Martin");

            // Then
            assertNotNull(book);
            assertNotNull(book.getId());
            assertEquals("1234567890", book.getIsbnValue());
            assertEquals("Clean Code", book.getTitleValue());
            assertEquals("Robert Martin", book.getAuthorValue());
            assertTrue(book.isAvailable());
        }

        @Test
        @DisplayName("Should create book with valid ISBN-13")
        void shouldCreateBookWithValidIsbn13() {
            // When
            Book book = Book.create("9781234567890", "DDD", "Eric Evans");

            // Then
            assertEquals("9781234567890", book.getIsbnValue());
        }

        @Test
        @DisplayName("Should clean ISBN (remove spaces and dashes)")
        void shouldCleanIsbn() {
            // When
            Book book = Book.create("978-1-234-56789-0", "Title", "Author");

            // Then
            assertEquals("9781234567890", book.getIsbnValue());
        }

        @Test
        @DisplayName("Should throw exception when ISBN is invalid")
        void shouldThrowExceptionWhenIsbnIsInvalid() {
            // When & Then
            assertThrows(InvalidIsbnException.class, () -> {
                Book.create("123", "Title", "Author");
            });
        }

        @Test
        @DisplayName("Should throw exception when title is null")
        void shouldThrowExceptionWhenTitleIsNull() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                Book.create("1234567890", null, "Author");
            });
        }

        @Test
        @DisplayName("Should throw exception when author is null")
        void shouldThrowExceptionWhenAuthorIsNull() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                Book.create("1234567890", "Title", null);
            });
        }
    }

    @Nested
    @DisplayName("Availability Management")
    class AvailabilityTests {

        @Test
        @DisplayName("Should mark book as unavailable")
        void shouldMarkBookAsUnavailable() {
            // Given
            Book book = Book.create("1234567890", "Title", "Author");
            assertTrue(book.isAvailable());

            // When
            book.markAsUnavailable();

            // Then
            assertFalse(book.isAvailable());
        }

        @Test
        @DisplayName("Should mark book as available")
        void shouldMarkBookAsAvailable() {
            // Given
            Book book = Book.create("1234567890", "Title", "Author");
            book.markAsUnavailable();

            // When
            book.markAsAvailable();

            // Then
            assertTrue(book.isAvailable());
        }

        @Test
        @DisplayName("Should throw exception when marking unavailable book as unavailable")
        void shouldThrowExceptionWhenMarkingUnavailableBookAsUnavailable() {
            // Given
            Book book = Book.create("1234567890", "Title", "Author");
            book.markAsUnavailable();

            // When & Then
            assertThrows(BookAlreadyUnavailableException.class, () -> {
                book.markAsUnavailable();
            });
        }

        @Test
        @DisplayName("Should throw exception when marking available book as available")
        void shouldThrowExceptionWhenMarkingAvailableBookAsAvailable() {
            // Given
            Book book = Book.create("1234567890", "Title", "Author");

            // When & Then
            assertThrows(BookAlreadyAvailableException.class, () -> {
                book.markAsAvailable();
            });
        }
    }

    @Nested
    @DisplayName("Borrowing Check")
    class BorrowingTests {

        @Test
        @DisplayName("Should be borrowable when available")
        void shouldBeBorrowableWhenAvailable() {
            // Given
            Book book = Book.create("1234567890", "Title", "Author");

            // When & Then
            assertTrue(book.canBeBorrowed());
        }

        @Test
        @DisplayName("Should not be borrowable when unavailable")
        void shouldNotBeBorrowableWhenUnavailable() {
            // Given
            Book book = Book.create("1234567890", "Title", "Author");
            book.markAsUnavailable();

            // When & Then
            assertFalse(book.canBeBorrowed());
        }
    }

    @Nested
    @DisplayName("Update Operations")
    class UpdateTests {

        @Test
        @DisplayName("Should update title")
        void shouldUpdateTitle() {
            // Given
            Book book = Book.create("1234567890", "Old Title", "Author");

            // When
            book.updateTitle("New Title");

            // Then
            assertEquals("New Title", book.getTitleValue());
        }

        @Test
        @DisplayName("Should update author")
        void shouldUpdateAuthor() {
            // Given
            Book book = Book.create("1234567890", "Title", "Old Author");

            // When
            book.updateAuthor("New Author");

            // Then
            assertEquals("New Author", book.getAuthorValue());
        }
    }

    @Nested
    @DisplayName("Identity and Equality")
    class IdentityTests {

        @Test
        @DisplayName("Should be equal when same ID")
        void shouldBeEqualWhenSameId() {
            // Given
            BookId id = BookId.generate();
            Book book1 = Book.reconstitute(id, "1234567890", "Title 1", "Author 1", true);
            Book book2 = Book.reconstitute(id, "9999999999", "Title 2", "Author 2", false);

            // When & Then
            assertEquals(book1, book2);
            assertEquals(book1.hashCode(), book2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when different IDs")
        void shouldNotBeEqualWhenDifferentIds() {
            // Given
            Book book1 = Book.create("1234567890", "Title", "Author");
            Book book2 = Book.create("1234567890", "Title", "Author");

            // When & Then
            assertNotEquals(book1, book2);
        }
    }

    @Nested
    @DisplayName("ISBN Formatting")
    class IsbnFormattingTests {

        @Test
        @DisplayName("Should format ISBN-10 correctly")
        void shouldFormatIsbn10Correctly() {
            // Given
            Book book = Book.create("1234567890", "Title", "Author");

            // When
            String formatted = book.getIsbn().formatted();

            // Then
            assertEquals("1-23-456789-0", formatted);
        }

        @Test
        @DisplayName("Should format ISBN-13 correctly")
        void shouldFormatIsbn13Correctly() {
            // Given
            Book book = Book.create("9781234567890", "Title", "Author");

            // When
            String formatted = book.getIsbn().formatted();

            // Then
            assertEquals("978-1-23-456789-0", formatted);
        }
    }

    @Nested
    @DisplayName("Complete Lifecycle")
    class LifecycleTest {

        @Test
        @DisplayName("Should handle complete book lifecycle scenario")
        void shouldHandleCompleteBookLifecycle() {
            // 1. Create book (available by default)
            Book book = Book.create("978-0-13-468599-1", "Clean Architecture", "Robert C. Martin");
            assertTrue(book.isAvailable());
            assertTrue(book.canBeBorrowed());

            // 2. Book is borrowed (marked as unavailable)
            book.markAsUnavailable();
            assertFalse(book.isAvailable());
            assertFalse(book.canBeBorrowed());

            // 3. Book is returned (marked as available)
            book.markAsAvailable();
            assertTrue(book.isAvailable());
            assertTrue(book.canBeBorrowed());

            // 4. Update book info
            book.updateTitle("Clean Architecture: A Craftsman's Guide");
            book.updateAuthor("Robert C. Martin (Uncle Bob)");

            assertEquals("Clean Architecture: A Craftsman's Guide", book.getTitleValue());
            assertEquals("Robert C. Martin (Uncle Bob)", book.getAuthorValue());

            // 5. Verify ISBN formatting
            assertEquals("978-0-13-468599-1", book.getIsbn().formatted());
        }
    }
}