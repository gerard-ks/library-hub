package ci.org.application.book.usecases;

import ci.org.application.book.commands.MarkBookAsUnavailableCommand;
import ci.org.application.book.dtos.BookResponse;
import ci.org.domain.book.entities.Book;
import ci.org.domain.book.exceptions.BookAlreadyAvailableException;
import ci.org.domain.book.exceptions.BookAlreadyUnavailableException;
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
 * Tests unitaires pour MarkBookAsUnavailableUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MarkBookAsUnavailableUseCase Tests")
class MarkBookAsUnavailableUseCaseTest {

    @Mock
    private BookRepository bookRepository;

    private MarkBookAsUnavailableUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new MarkBookAsUnavailableUseCase(bookRepository);
    }

    @Test
    @DisplayName("Should mark book as unavailable successfully")
    void shouldMarkBookAsUnavailableSuccessfully() {
        // Given
        Book book = Book.create("978-0-59-652068-7", "Clean Code", "Robert Martin");
        String bookId = book.getId().value();
        MarkBookAsUnavailableCommand command = new MarkBookAsUnavailableCommand(bookId);

        assertTrue(book.isAvailable());  // Initialement disponible

        when(bookRepository.findById(any(BookId.class))).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // When
        BookResponse response = useCase.execute(command);

        // Then
        assertNotNull(response);
        assertFalse(response.available());  // Maintenant indisponible

        verify(bookRepository, times(1)).findById(any(BookId.class));
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw exception when book not found")
    void shouldThrowExceptionWhenBookNotFound() {
        // Given
        MarkBookAsUnavailableCommand command = new MarkBookAsUnavailableCommand("non-existent-id");

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
    @DisplayName("Should remain unavailable when already unavailable")
    void shouldRemainUnavailableWhenAlreadyUnavailable() {
        // Given
        Book book = Book.create("978-0-59-652068-7", "Clean Code", "Robert Martin");
        book.markAsUnavailable();  // Déjà indisponible
        String bookId = book.getId().value();
        MarkBookAsUnavailableCommand command = new MarkBookAsUnavailableCommand(bookId);

        assertFalse(book.isAvailable());  // Déjà indisponible

        when(bookRepository.findById(any(BookId.class))).thenReturn(Optional.of(book));

        // When & Then
        assertThrows(BookAlreadyUnavailableException.class, () -> {
            useCase.execute(command);
        });

        verify(bookRepository, times(1)).findById(any(BookId.class));
        verify(bookRepository, never()).save(any(Book.class));
    }
}