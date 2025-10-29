package ci.org.application.book.usecases;

import ci.org.application.book.requests.commands.UpdateBookCommand;
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
 * Use Case : Mettre à jour un livre.
 *
 * Implémente CommandUseCase car modifie l'état (write).
 */
@UseCase(value = "UpdateBook", transactionMode = TransactionMode.READ_WRITE)
public class UpdateBookUseCase implements CommandUseCase<UpdateBookCommand, BookResponse> {

    private final BookRepository bookRepository;

    public UpdateBookUseCase(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public BookResponse execute(UpdateBookCommand command) {

        return bookRepository.findById(BookId.of(command.bookId()))
                .map(book -> updateBook(book, command.newTitle(), command.newAuthor()))
                .map(BookMapper::toResponse)
                .orElseThrow(() -> new BookNotFoundException(command.bookId()));
    }

    private Book updateBook(Book book, String newTitle, String newAuthor) {
        book.updateTitle(newTitle);
        book.updateAuthor(newAuthor);
        return bookRepository.save(book);
    }
}