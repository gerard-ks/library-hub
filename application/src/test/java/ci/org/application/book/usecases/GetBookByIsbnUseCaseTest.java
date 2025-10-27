package ci.org.application.book.usecases;

import ci.org.application.book.dtos.BookResponse;
import ci.org.application.book.queries.GetBookByIsbnQuery;
import ci.org.domain.book.entities.Book;
import ci.org.domain.book.exceptions.BookNotFoundException;
import ci.org.domain.book.repositories.BookRepository;
import ci.org.domain.book.valueobjects.Isbn;
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
 * Tests unitaires pour GetBookByIsbnUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetBookByIsbnUseCase Tests")
class GetBookByIsbnUseCaseTest {

    @Mock
    private BookRepository bookRepository;

    private GetBookByIsbnUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetBookByIsbnUseCase(bookRepository);
    }

    @Test
    @DisplayName("Should get book by ISBN successfully")
    void shouldGetBookByIsbnSuccessfully() {
        // Given
        String isbn = "978-0-59-652068-7";
        Book book = Book.create(isbn, "Clean Code", "Robert Martin");
        GetBookByIsbnQuery query = new GetBookByIsbnQuery(isbn);

        when(bookRepository.findByIsbn(any(Isbn.class))).thenReturn(Optional.of(book));

        // When
        BookResponse response = useCase.execute(query);

        // Then
        assertNotNull(response);
        assertEquals("9780596520687", response.isbn());
        assertEquals("Clean Code", response.title());
        assertEquals("Robert Martin", response.author());
        assertTrue(response.available());

        verify(bookRepository, times(1)).findByIsbn(any(Isbn.class));
    }

    @Test
    @DisplayName("Should throw exception when book not found by ISBN")
    void shouldThrowExceptionWhenBookNotFoundByIsbn() {
        // Given
        GetBookByIsbnQuery query = new GetBookByIsbnQuery("978-0-000-00000-0");

        when(bookRepository.findByIsbn(any(Isbn.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BookNotFoundException.class, () -> {
            useCase.execute(query);
        });

        verify(bookRepository, times(1)).findByIsbn(any(Isbn.class));
    }

    @Test
    @DisplayName("Should throw exception when query is null")
    void shouldThrowExceptionWhenQueryIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            useCase.execute(null);
        });

        verify(bookRepository, never()).findByIsbn(any(Isbn.class));
    }

    @Test
    @DisplayName("Should find book with ISBN-13 format")
    void shouldFindBookWithIsbn13Format() {
        // Given
        String isbn13 = "978-0-59-652068-7";
        Book book = Book.create(isbn13, "Clean Code", "Robert Martin");
        GetBookByIsbnQuery query = new GetBookByIsbnQuery(isbn13);

        when(bookRepository.findByIsbn(any(Isbn.class))).thenReturn(Optional.of(book));

        // When
        BookResponse response = useCase.execute(query);

        // Then
        assertEquals("9780596520687", response.isbn());
    }

    @Test
    @DisplayName("Should find book with ISBN-10 format")
    void shouldFindBookWithIsbn10Format() {
        // Given
        String isbn10 = "0-59-652068-9";
        Book book = Book.create(isbn10, "Clean Code", "Robert Martin");
        GetBookByIsbnQuery query = new GetBookByIsbnQuery(isbn10);

        when(bookRepository.findByIsbn(any(Isbn.class))).thenReturn(Optional.of(book));

        // When
        BookResponse response = useCase.execute(query);

        // Then
        assertEquals("0596520689", response.isbn());
    }
}