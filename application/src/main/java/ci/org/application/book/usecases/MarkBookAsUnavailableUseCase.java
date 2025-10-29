package ci.org.application.book.usecases;

import ci.org.application.book.requests.commands.MarkBookAsUnavailableCommand;
import ci.org.application.book.responses.BookResponse;
import ci.org.application.book.mappers.BookMapper;
import ci.org.domain.book.entities.Book;
import ci.org.domain.book.exceptions.BookNotFoundException;
import ci.org.domain.book.repositories.BookRepository;
import ci.org.domain.book.valueobjects.BookId;
import ci.org.shared.kernel.CommandUseCase;
import ci.org.shared.kernel.TransactionMode;
import ci.org.shared.kernel.UseCase;

import java.util.Objects;

/**
 * Use Case : Marquer un livre comme indisponible.
 *
 * Implémente CommandUseCase car modifie l'état (write).
 */
@UseCase(value = "MarkBookAsUnavailable", transactionMode = TransactionMode.READ_WRITE)
public class MarkBookAsUnavailableUseCase implements CommandUseCase<MarkBookAsUnavailableCommand, BookResponse> {

    private final BookRepository bookRepository;

    public MarkBookAsUnavailableUseCase(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public BookResponse execute(MarkBookAsUnavailableCommand command) {
        return bookRepository.findById(BookId.of(command.bookId()))
                .map(this::markAsUnavailable)
                .map(BookMapper::toResponse)
                .orElseThrow(() -> new BookNotFoundException(command.bookId()));
    }

    private Book markAsUnavailable(Book book) {
        book.markAsUnavailable();
        return bookRepository.save(book);
    }
}