package ci.org.application.book.usecases;

import ci.org.application.book.commands.UpdateBookCommand;
import ci.org.application.book.dtos.BookResponse;
import ci.org.domain.book.entities.Book;
import ci.org.domain.book.exceptions.BookNotFoundException;
import ci.org.domain.book.repositories.BookRepository;
import ci.org.domain.book.valueobjects.BookId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour UpdateBookUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateBookUseCase Tests")
class UpdateBookUseCaseTest {

    @Mock
    private BookRepository bookRepository;

    private UpdateBookUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateBookUseCase(bookRepository);
    }

    @Test
    @DisplayName("Should update book successfully")
    void shouldUpdateBookSuccessfully() {
        // Given
        Book existingBook = Book.create("978-0-596-52068-7", "Old Title", "Old Author");
        String bookId = existingBook.getId().value();
        UpdateBookCommand command = new UpdateBookCommand(bookId, "New Title", "New Author");

        when(bookRepository.findById(any(BookId.class))).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenReturn(existingBook);

        // When
        BookResponse response = useCase.execute(command);

        // Then
        assertNotNull(response);
        assertEquals("New Title", response.title());
        assertEquals("New Author", response.author());

        verify(bookRepository, times(1)).findById(any(BookId.class));
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw exception when book not found")
    void shouldThrowExceptionWhenBookNotFound() {
        // Given
        UpdateBookCommand command = new UpdateBookCommand("non-existent-id", "Title", "Author");

        when(bookRepository.findById(any(BookId.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BookNotFoundException.class, () -> {
            useCase.execute(command);
        });

        verify(bookRepository, times(1)).findById(any(BookId.class));
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw exception when command is null")
    void shouldThrowExceptionWhenCommandIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            useCase.execute(null);
        });

        verify(bookRepository, never()).findById(any(BookId.class));
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should update only title when author unchanged")
    void shouldUpdateOnlyTitleWhenAuthorUnchanged() {
        // Given
        Book existingBook = Book.create("978-0-596-52068-7", "Old Title", "Same Author");
        String bookId = existingBook.getId().value();
        UpdateBookCommand command = new UpdateBookCommand(bookId, "New Title", "Same Author");

        when(bookRepository.findById(any(BookId.class))).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenReturn(existingBook);

        // When
        BookResponse response = useCase.execute(command);

        // Then
        assertEquals("New Title", response.title());
        assertEquals("Same Author", response.author());
    }
}