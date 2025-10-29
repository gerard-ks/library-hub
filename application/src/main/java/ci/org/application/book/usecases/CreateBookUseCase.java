package ci.org.application.book.usecases;

import ci.org.application.book.commands.CreateBookCommand;
import ci.org.application.book.dtos.BookResponse;
import ci.org.application.book.mappers.BookMapper;
import ci.org.domain.book.entities.Book;
import ci.org.domain.book.repositories.BookRepository;
import ci.org.shared.kernel.CommandUseCase;
import ci.org.shared.kernel.TransactionMode;
import ci.org.shared.kernel.UseCase;

import java.util.Objects;
import java.util.Optional;


@UseCase(value = "CreateBook", transactionMode = TransactionMode.READ_WRITE)
public class CreateBookUseCase implements CommandUseCase<CreateBookCommand, BookResponse> {

    private final BookRepository bookRepository;

    public CreateBookUseCase(BookRepository bookRepository) {
        this.bookRepository = Objects.requireNonNull(bookRepository, "BookRepository cannot be null");
    }

    @Override
    public BookResponse execute(CreateBookCommand command) {
        return Optional.ofNullable(
                bookRepository.save(
                        Book.create(command.isbn(), command.title(), command.author())
                )
        ).map(BookMapper::toResponse
        ).orElseThrow(() -> new RuntimeException(""));
    }
}