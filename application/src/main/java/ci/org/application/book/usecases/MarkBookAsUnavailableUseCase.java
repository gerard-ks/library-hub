package ci.org.application.book.usecases;

import ci.org.application.book.commands.MarkBookAsUnavailableCommand;
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

/**
 * Use Case : Marquer un livre comme indisponible.
 *
 * Implémente CommandUseCase car modifie l'état (write).
 */
@UseCase(value = "MarkBookAsUnavailable", transactionMode = TransactionMode.READ_WRITE)
public class MarkBookAsUnavailableUseCase implements CommandUseCase<MarkBookAsUnavailableCommand, BookResponse> {

    private final BookRepository bookRepository;

    public MarkBookAsUnavailableUseCase(BookRepository bookRepository) {
        this.bookRepository = Objects.requireNonNull(bookRepository, "BookRepository cannot be null");
    }

    @Override
    public BookResponse execute(MarkBookAsUnavailableCommand command) {
        Objects.requireNonNull(command, "MarkBookAsUnavailableCommand cannot be null");

        // 1. Rechercher le livre
        Book book = bookRepository.findById(BookId.of(command.bookId()))
                .orElseThrow(() -> new BookNotFoundException(command.bookId()));

        // 2. Appliquer la règle métier
        book.markAsUnavailable();

        // 3. Persister
        Book updatedBook = bookRepository.save(book);

        // 4. Mapper vers DTO et retourner
        return BookMapper.toResponse(updatedBook);
    }
}