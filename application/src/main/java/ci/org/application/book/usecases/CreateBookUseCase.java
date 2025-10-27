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


@UseCase(value = "CreateBook", transactionMode = TransactionMode.READ_WRITE)
public class CreateBookUseCase implements CommandUseCase<CreateBookCommand, BookResponse> {

    private final BookRepository bookRepository;

    public CreateBookUseCase(BookRepository bookRepository) {
        this.bookRepository = Objects.requireNonNull(bookRepository, "BookRepository cannot be null");
    }

    @Override
    public BookResponse execute(CreateBookCommand command) {
        Objects.requireNonNull(command, "CreateBookCommand cannot be null");

        // 1. Créer l'entité domain (validation métier automatique)
        Book book = Book.create(command.isbn(), command.title(), command.author());

        // 2. Persister
        Book savedBook = bookRepository.save(book);

        // 3. Mapper vers DTO et retourner
        return BookMapper.toResponse(savedBook);
    }
}