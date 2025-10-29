package ci.org.application.book.usecases;

import ci.org.application.book.responses.BookResponse;
import ci.org.application.book.requests.queries.ListBooksQuery;
import ci.org.domain.book.entities.Book;
import ci.org.domain.book.repositories.BookRepository;
import ci.org.shared.kernel.Page;
import ci.org.shared.kernel.PageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour GetAllBooksUseCase (avec pagination).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetAllBooksUseCase Tests")
class GetAllBooksUseCaseTest {

    @Mock
    private BookRepository bookRepository;

    private GetAllBooksUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetAllBooksUseCase(bookRepository);
    }

    @Test
    @DisplayName("Should get all books with pagination successfully")
    void shouldGetAllBooksWithPaginationSuccessfully() {
        // Given
        ListBooksQuery query = new ListBooksQuery(0, 20, "title", "ASC");

        Book book1 = Book.create("978-0-59-652068-7", "Clean Code", "Robert Martin");
        Book book2 = Book.create("978-0-13-235088-4", "Clean Architecture", "Robert Martin");
        Book book3 = Book.create("978-0-20-163361-0", "Design Patterns", "Gang of Four");

        List<Book> books = Arrays.asList(book1, book2, book3);
        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<Book> bookPage = Page.of(books, pageRequest, 3);

        when(bookRepository.findAll(any(PageRequest.class))).thenReturn(bookPage);

        // When
        Page<BookResponse> responsePage = useCase.execute(query);

        // Then
        assertNotNull(responsePage);
        assertEquals(3, responsePage.getContent().size());
        assertEquals(3, responsePage.getTotalElements());
        assertEquals(0, responsePage.getPageNumber());
        assertEquals(20, responsePage.getPageSize());
        assertEquals(1, responsePage.getTotalPages());

        assertEquals("Clean Code", responsePage.getContent().get(0).title());
        assertEquals("Clean Architecture", responsePage.getContent().get(1).title());
        assertEquals("Design Patterns", responsePage.getContent().get(2).title());

        verify(bookRepository, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    @DisplayName("Should return empty page when no books")
    void shouldReturnEmptyPageWhenNoBooks() {
        // Given
        ListBooksQuery query = new ListBooksQuery(0, 20, "title", "ASC");

        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<Book> emptyPage = Page.of(Collections.emptyList(), pageRequest, 0);

        when(bookRepository.findAll(any(PageRequest.class))).thenReturn(emptyPage);

        // When
        Page<BookResponse> responsePage = useCase.execute(query);

        // Then
        assertNotNull(responsePage);
        assertTrue(responsePage.isEmpty());
        assertEquals(0, responsePage.getTotalElements());
        assertEquals(0, responsePage.getNumberOfElements());

        verify(bookRepository, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void shouldHandlePaginationCorrectly() {
        // Given - Page 1 (10 éléments par page, total 25 éléments)
        ListBooksQuery query = new ListBooksQuery(1, 10, "title", "ASC");

        List<Book> books = Arrays.asList(
                Book.create("978-0-59-652068-7", "Book 11", "Author 11"),
                Book.create("978-0-13-235088-4", "Book 12", "Author 12"),
                Book.create("978-0-20-163361-0", "Book 13", "Author 13")
        );

        PageRequest pageRequest = PageRequest.of(1, 10);
        Page<Book> bookPage = Page.of(books, pageRequest, 25);  // Total 25

        when(bookRepository.findAll(any(PageRequest.class))).thenReturn(bookPage);

        // When
        Page<BookResponse> responsePage = useCase.execute(query);

        // Then
        assertEquals(1, responsePage.getPageNumber());  // Page 1
        assertEquals(10, responsePage.getPageSize());   // 10 par page
        assertEquals(25, responsePage.getTotalElements());  // 25 au total
        assertEquals(3, responsePage.getTotalPages());  // ceil(25/10) = 3 pages
        assertEquals(3, responsePage.getNumberOfElements());  // 3 éléments dans cette page

        assertFalse(responsePage.isFirst());  // Pas la première page
        assertFalse(responsePage.isLast());   // Pas la dernière page
        assertTrue(responsePage.hasPrevious());  // A une page précédente
        assertTrue(responsePage.hasNext());  // A une page suivante
    }

    @Test
    @DisplayName("Should map all book properties correctly with pagination")
    void shouldMapAllBookPropertiesCorrectlyWithPagination() {
        // Given
        ListBooksQuery query = new ListBooksQuery(0, 10, "title", "ASC");

        Book book = Book.create("978-0-59-652068-7", "Clean Code", "Robert Martin");
        book.markAsUnavailable();

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Book> bookPage = Page.of(List.of(book), pageRequest, 1);

        when(bookRepository.findAll(any(PageRequest.class))).thenReturn(bookPage);

        // When
        Page<BookResponse> responsePage = useCase.execute(query);

        // Then
        assertEquals(1, responsePage.getContent().size());
        BookResponse response = responsePage.getContent().get(0);

        assertEquals(book.getId().value(), response.id());
        assertEquals("9780596520687", response.isbn());
        assertEquals("Clean Code", response.title());
        assertEquals("Robert Martin", response.author());
        assertFalse(response.available());
    }

    @Test
    @DisplayName("Should throw exception when query is null")
    void shouldThrowExceptionWhenQueryIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            useCase.execute(null);
        });

        verify(bookRepository, never()).findAll(any(PageRequest.class));
    }

    @Test
    @DisplayName("Should handle DESC sort direction")
    void shouldHandleDescSortDirection() {
        // Given
        ListBooksQuery query = new ListBooksQuery(0, 20, "title", "DESC");

        Book book1 = Book.create("978-0-20-163361-0", "Design Patterns", "Gang of Four");
        Book book2 = Book.create("978-0-13-235088-4", "Clean Architecture", "Robert Martin");
        Book book3 = Book.create("978-0-59-652068-7", "Clean Code", "Robert Martin");

        List<Book> books = Arrays.asList(book1, book2, book3);  // Déjà trié DESC
        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<Book> bookPage = Page.of(books, pageRequest, 3);

        when(bookRepository.findAll(any(PageRequest.class))).thenReturn(bookPage);

        // When
        Page<BookResponse> responsePage = useCase.execute(query);

        // Then
        assertEquals("Design Patterns", responsePage.getContent().get(0).title());
        assertEquals("Clean Architecture", responsePage.getContent().get(1).title());
        assertEquals("Clean Code", responsePage.getContent().get(2).title());

        verify(bookRepository, times(1)).findAll(any(PageRequest.class));
    }
}