package ci.org.application.book.usecases;

import ci.org.application.book.dtos.BookResponse;
import ci.org.application.book.mappers.BookMapper;
import ci.org.application.book.queries.GetAllBooksQuery;
import ci.org.domain.book.repositories.BookRepository;
import ci.org.shared.kernel.*;

import java.util.Objects;
import java.util.Optional;

@UseCase(value = "GetAllBooks", transactionMode = TransactionMode.READ_ONLY)
public class GetAllBooksUseCase implements QueryUseCase<GetAllBooksQuery, Page<BookResponse>> {

    private final BookRepository bookRepository;

    public GetAllBooksUseCase(BookRepository bookRepository) {
        this.bookRepository = Objects.requireNonNull(bookRepository, "BookRepository cannot be null");
    }

    @Override
    public Page<BookResponse> execute(GetAllBooksQuery query) {
        return Optional.ofNullable(bookRepository.findAll(PageRequest.of(
                        query.page(),
                        query.size(),
                        Sort.by(query.sortBy(), Sort.Direction.valueOf(query.sortDirection()))
                ))).map(bookPage -> bookPage.map(BookMapper::toResponse))
                .orElseThrow(() -> new RuntimeException(""));
    }
}