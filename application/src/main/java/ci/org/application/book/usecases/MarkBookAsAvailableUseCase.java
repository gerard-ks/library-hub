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

/**
 * Use Case : Marquer un livre comme disponible.
 *
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
        Objects.requireNonNull(command, "MarkBookAsAvailableCommand cannot be null");

        // 1. Rechercher le livre
        Book book = bookRepository.findById(BookId.of(command.bookId()))
                .orElseThrow(() -> new BookNotFoundException(command.bookId()));

        // 2. Appliquer la règle métier
        book.markAsAvailable();

        // 3. Persister
        Book updatedBook = bookRepository.save(book);

        // 4. Mapper vers DTO et retourner
        return BookMapper.toResponse(updatedBook);
    }
}