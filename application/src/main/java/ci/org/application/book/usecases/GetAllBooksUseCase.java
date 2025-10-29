package ci.org.application.book.usecases;

import ci.org.application.book.responses.BookResponse;
import ci.org.application.book.mappers.BookMapper;
import ci.org.application.book.requests.queries.ListBooksQuery;
import ci.org.domain.book.entities.Book;
import ci.org.domain.book.repositories.BookRepository;
import ci.org.shared.kernel.*;

import java.util.Objects;

@UseCase(value = "GetAllBooks", transactionMode = TransactionMode.READ_ONLY)
public class GetAllBooksUseCase implements QueryUseCase<ListBooksQuery, Page<BookResponse>> {

    private final BookRepository bookRepository;

    public GetAllBooksUseCase(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public Page<BookResponse> execute(ListBooksQuery query) {

        Page<Book> bookPage = bookRepository.findAll(toPageRequest(query));

        return bookPage.map(BookMapper::toResponse);
    }

    private PageRequest toPageRequest(ListBooksQuery query) {
        return PageRequest.of(
                query.page(),
                query.size(),
                Sort.by(query.sortBy(), Sort.Direction.valueOf(query.sortDirection()))
        );
    }
}