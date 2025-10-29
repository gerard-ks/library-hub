package ci.org.application.book.usecases;

import ci.org.application.book.requests.commands.CreateBookCommand;
import ci.org.application.book.responses.BookResponse;
import ci.org.domain.book.entities.Book;
import ci.org.domain.book.exceptions.InvalidIsbnException;
import ci.org.domain.book.repositories.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour CreateBookUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateBookUseCase Tests")
class CreateBookUseCaseTest {

    @Mock
    private BookRepository bookRepository;

    private CreateBookUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateBookUseCase(bookRepository);
    }

    @Test
    @DisplayName("Should create book successfully")
    void shouldCreateBookSuccessfully() {
        // Given
        CreateBookCommand command = new CreateBookCommand(
                "978-0-59-652068-7",
                "Clean Code",
                "Robert Martin"
        );
        Book savedBook = Book.create("978-0-59-652068-7", "Clean Code", "Robert Martin");

        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        // When
        BookResponse response = useCase.execute(command);

        // Then
        assertNotNull(response);
        assertEquals("9780596520687", response.isbn());
        assertEquals("Clean Code", response.title());
        assertEquals("Robert Martin", response.author());
        assertTrue(response.available());

        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw exception when ISBN is invalid")
    void shouldThrowExceptionWhenIsbnIsInvalid() {
        // Given
        CreateBookCommand command = new CreateBookCommand(
                "invalid-isbn",
                "Clean Code",
                "Robert Martin"
        );

        // When & Then
        assertThrows(InvalidIsbnException.class, () -> {
            useCase.execute(command);
        });

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw exception when command is null")
    void shouldThrowExceptionWhenCommandIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            useCase.execute(null);
        });

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should trim title and author before creating")
    void shouldTrimTitleAndAuthorBeforeCreating() {
        // Given
        CreateBookCommand command = new CreateBookCommand(
                "978-0-59-652068-7",
                "  Clean Code  ",
                "  Robert Martin  "
        );
        Book savedBook = Book.create("978-0-59-652068-7", "Clean Code", "Robert Martin");

        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        // When
        BookResponse response = useCase.execute(command);

        // Then
        assertEquals("Clean Code", response.title());
        assertEquals("Robert Martin", response.author());
        verify(bookRepository, times(1)).save(any(Book.class));
    }
}