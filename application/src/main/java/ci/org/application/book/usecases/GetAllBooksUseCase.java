package ci.org.application.book.usecases;

import ci.org.application.book.dtos.BookResponse;
import ci.org.application.book.mappers.BookMapper;
import ci.org.application.book.queries.GetAllBooksQuery;
import ci.org.domain.book.entities.Book;
import ci.org.domain.book.repositories.BookRepository;
import ci.org.shared.kernel.*;

import java.util.Objects;

@UseCase(value = "GetAllBooks", transactionMode = TransactionMode.READ_ONLY)
public class GetAllBooksUseCase implements QueryUseCase<GetAllBooksQuery, Page<BookResponse>> {

    private final BookRepository bookRepository;

    public GetAllBooksUseCase(BookRepository bookRepository) {
        this.bookRepository = Objects.requireNonNull(bookRepository, "BookRepository cannot be null");
    }

    @Override
    public Page<BookResponse> execute(GetAllBooksQuery query) {
        Objects.requireNonNull(query, "GetAllBooksQuery cannot be null");

        // 1. Créer PageRequest (domain)
        PageRequest pageRequest = PageRequest.of(
                query.page(),
                query.size(),
                Sort.by(query.sortBy(), Sort.Direction.valueOf(query.sortDirection()))
        );

        // 2. Récupérer Page<Book> (domain)
        Page<Book> bookPage = bookRepository.findAll(pageRequest);

        // 3. Mapper vers Page<BookResponse>
        return bookPage.map(BookMapper::toResponse);
    }
}