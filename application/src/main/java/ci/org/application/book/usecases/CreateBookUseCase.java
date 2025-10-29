package ci.org.application.book.usecases;

import ci.org.application.book.requests.commands.CreateBookCommand;
import ci.org.application.book.responses.BookResponse;
import ci.org.application.book.mappers.BookMapper;
import ci.org.domain.book.entities.Book;
import ci.org.domain.book.exceptions.BookAlreadyAvailableException;
import ci.org.domain.book.exceptions.BookAlreadyExistsException;
import ci.org.domain.book.repositories.BookRepository;
import ci.org.domain.book.valueobjects.Isbn;
import ci.org.shared.kernel.CommandUseCase;
import ci.org.shared.kernel.TransactionMode;
import ci.org.shared.kernel.UseCase;

import java.util.Objects;
import java.util.Optional;


@UseCase(value = "CreateBook", transactionMode = TransactionMode.READ_WRITE)
public class CreateBookUseCase implements CommandUseCase<CreateBookCommand, BookResponse> {

    private final BookRepository bookRepository;

    public CreateBookUseCase(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public BookResponse execute(CreateBookCommand command) {
        checkIsbnNotExists(Isbn.of(command.isbn()));

        Book savedBook = bookRepository.save(Book.create(command.isbn(), command.title(), command.author()));

        return BookMapper.toResponse(savedBook);
    }

    private void checkIsbnNotExists(Isbn isbn) {
        bookRepository.findByIsbn(isbn)
                .ifPresent(existingBook -> {
                    throw new BookAlreadyExistsException(isbn);
                });
    }
}