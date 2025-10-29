package ci.org.application.book.usecases;

import ci.org.application.book.responses.BookResponse;
import ci.org.application.book.mappers.BookMapper;
import ci.org.application.book.requests.queries.FindBookQuery;
import ci.org.domain.book.entities.Book;
import ci.org.domain.book.exceptions.BookNotFoundException;
import ci.org.domain.book.repositories.BookRepository;
import ci.org.domain.book.valueobjects.Isbn;
import ci.org.shared.kernel.QueryUseCase;
import ci.org.shared.kernel.TransactionMode;
import ci.org.shared.kernel.UseCase;

import java.util.Objects;

@UseCase(value = "GetBookByIsbn", transactionMode = TransactionMode.READ_ONLY)
public class GetBookByIsbnUseCase implements QueryUseCase<FindBookQuery, BookResponse> {

    private final BookRepository bookRepository;

    public GetBookByIsbnUseCase(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public BookResponse execute(FindBookQuery query) {
        return bookRepository.findByIsbn(Isbn.of(query.isbn()))
                .map(BookMapper::toResponse)
                .orElseThrow(() -> new BookNotFoundException("Book not found with ISBN: " + query.isbn()));
    }
}
