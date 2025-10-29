package ci.org.application.book.usecases;

import ci.org.application.book.requests.commands.MarkBookAsAvailableCommand;
import ci.org.application.book.responses.BookResponse;
import ci.org.domain.book.entities.Book;
import ci.org.domain.book.exceptions.BookAlreadyAvailableException;
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
 * Tests unitaires pour MarkBookAsAvailableUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MarkBookAsAvailableUseCase Tests")
class MarkBookAsAvailableUseCaseTest {

    @Mock
    private BookRepository bookRepository;

    private MarkBookAsAvailableUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new MarkBookAsAvailableUseCase(bookRepository);
    }

    @Test
    @DisplayName("Should mark book as available successfully")
    void shouldMarkBookAsAvailableSuccessfully() {
        // Given
        Book book = Book.create("978-0-59-652068-7", "Clean Code", "Robert Martin");
        book.markAsUnavailable();  // Initialement indisponible
        String bookId = book.getId().value();
        MarkBookAsAvailableCommand command = new MarkBookAsAvailableCommand(bookId);

        assertFalse(book.isAvailable());  // Initialement indisponible

        when(bookRepository.findById(any(BookId.class))).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // When
        BookResponse response = useCase.execute(command);

        // Then
        assertNotNull(response);
        assertTrue(response.available());  // Maintenant disponible

        verify(bookRepository, times(1)).findById(any(BookId.class));
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw exception when book not found")
    void shouldThrowExceptionWhenBookNotFound() {
        // Given
        MarkBookAsAvailableCommand command = new MarkBookAsAvailableCommand("non-existent-id");

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
    @DisplayName("Should remain available when already available")
    void shouldRemainAvailableWhenAlreadyAvailable() {
        // Given
        Book book = Book.create("978-0-59-652068-7", "Clean Code", "Robert Martin");
        String bookId = book.getId().value();
        MarkBookAsAvailableCommand command = new MarkBookAsAvailableCommand(bookId);

        assertTrue(book.isAvailable());  // Déjà disponible

        when(bookRepository.findById(any(BookId.class))).thenReturn(Optional.of(book));

        // When & Then
        assertThrows(BookAlreadyAvailableException.class, () -> {
            useCase.execute(command);
        });

        verify(bookRepository, times(1)).findById(any(BookId.class));
        verify(bookRepository, never()).save(any(Book.class));
    }
}