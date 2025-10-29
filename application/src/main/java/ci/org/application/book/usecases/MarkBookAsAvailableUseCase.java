package ci.org.application.book.usecases;

import ci.org.application.book.requests.commands.MarkBookAsAvailableCommand;
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
 * Use Case : Marquer un livre comme disponible.
 *
 * Implémente CommandUseCase car modifie l'état (write).
 */
@UseCase(value = "MarkBookAsAvailable", transactionMode = TransactionMode.READ_WRITE)
public class MarkBookAsAvailableUseCase implements CommandUseCase<MarkBookAsAvailableCommand, BookResponse> {

    private final BookRepository bookRepository;

    public MarkBookAsAvailableUseCase(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public BookResponse execute(MarkBookAsAvailableCommand command) {
        return bookRepository.findById(BookId.of(command.bookId()))
                .map(this::markAsAvailable)
                .map(BookMapper::toResponse)
                .orElseThrow(() -> new BookNotFoundException(command.bookId()));
    }

    private Book markAsAvailable(Book book) {
        book.markAsAvailable();
        return bookRepository.save(book);
    }
}