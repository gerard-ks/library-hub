package ci.org.application.book.usecases;

import ci.org.application.book.commands.MarkBookAsAvailableCommand;
import ci.org.application.book.dtos.BookResponse;
import ci.org.application.book.mappers.BookMapper;
import ci.org.domain.book.entities.Book;
import ci.org.domain.book.exceptions.BookNotFoundException;
import ci.org.domain.book.repositories.BookRepository;
import ci.org.domain.book.valueobjects.BookId;
import ci.org.shared.kernel.CommandUseCase;
import ci.org.shared.kernel.TransactionMode;
import ci.org.shared.kernel.UseCase;

import java.util.Objects;
import java.util.Optional;

/**
 * Use Case : Marquer un livre comme disponible.
 * <p>
 * Implémente CommandUseCase car modifie l'état (write).
 */
@UseCase(value = "MarkBookAsAvailable", transactionMode = TransactionMode.READ_WRITE)
public class MarkBookAsAvailableUseCase implements CommandUseCase<MarkBookAsAvailableCommand, BookResponse> {

    private final BookRepository bookRepository;

    public MarkBookAsAvailableUseCase(BookRepository bookRepository) {
        this.bookRepository = Objects.requireNonNull(bookRepository, "BookRepository cannot be null");
    }

    @Override
    public BookResponse execute(MarkBookAsAvailableCommand command) {
        Book book = bookRepository.findById(BookId.of(command.bookId()))
                .orElseThrow(() -> new BookNotFoundException(command.bookId()));

        book.markAsAvailable();

        return Optional.ofNullable(bookRepository.save(book))
                .map(BookMapper::toResponse)
                .orElseThrow(() -> new RuntimeException(""));
    }
}