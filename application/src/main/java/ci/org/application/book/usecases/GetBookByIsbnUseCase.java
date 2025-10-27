package ci.org.application.book.usecases;

import ci.org.application.book.dtos.BookResponse;
import ci.org.application.book.mappers.BookMapper;
import ci.org.application.book.queries.GetBookByIsbnQuery;
import ci.org.domain.book.entities.Book;
import ci.org.domain.book.exceptions.BookNotFoundException;
import ci.org.domain.book.repositories.BookRepository;
import ci.org.domain.book.valueobjects.Isbn;
import ci.org.shared.kernel.QueryUseCase;
import ci.org.shared.kernel.TransactionMode;
import ci.org.shared.kernel.UseCase;

import java.util.Objects;

@UseCase(value = "GetBookByIsbn", transactionMode = TransactionMode.READ_ONLY)
public class GetBookByIsbnUseCase implements QueryUseCase<GetBookByIsbnQuery, BookResponse> {

    private final BookRepository bookRepository;

    public GetBookByIsbnUseCase(BookRepository bookRepository) {
        this.bookRepository = Objects.requireNonNull(bookRepository, "BookRepository cannot be null");
    }

    @Override
    public BookResponse execute(GetBookByIsbnQuery query) {
        Objects.requireNonNull(query, "GetBookByIsbnQuery cannot be null");

        // 1. Rechercher le livre par ISBN
        Book book = bookRepository.findByIsbn(Isbn.of(query.isbn()))
                .orElseThrow(() -> new BookNotFoundException("Book not found with ISBN: " + query.isbn()));

        // 2. Mapper vers DTO et retourner
        return BookMapper.toResponse(book);
    }
}
