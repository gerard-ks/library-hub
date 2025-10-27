package ci.org.presentation.rest.controllers;

import ci.org.application.book.commands.CreateBookCommand;
import ci.org.application.book.commands.MarkBookAsAvailableCommand;
import ci.org.application.book.commands.MarkBookAsUnavailableCommand;
import ci.org.application.book.commands.UpdateBookCommand;
import ci.org.application.book.dtos.BookResponse;
import ci.org.application.book.queries.*;
import ci.org.presentation.rest.mappers.BookMapper;
import ci.org.presentation.rest.requests.*;
import ci.org.presentation.rest.responses.ApiResponse;
import ci.org.shared.kernel.CommandUseCase;
import ci.org.shared.kernel.Page;
import ci.org.shared.kernel.QueryUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour la gestion des livres.
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final CommandUseCase<CreateBookCommand, BookResponse> createBook;
    private final CommandUseCase<UpdateBookCommand, BookResponse> updateBook;
    private final QueryUseCase<GetBookByIsbnQuery, BookResponse> getBookByIsbn;
    private final QueryUseCase<GetAllBooksQuery, Page<BookResponse>> getAllBooks;
    private final CommandUseCase<MarkBookAsAvailableCommand, BookResponse> markAsAvailable;
    private final CommandUseCase<MarkBookAsUnavailableCommand, BookResponse> markAsUnavailable;

    public BookController(CommandUseCase<CreateBookCommand, BookResponse> createBook, CommandUseCase<UpdateBookCommand, BookResponse> updateBook, QueryUseCase<GetBookByIsbnQuery, BookResponse> getBookByIsbn, QueryUseCase<GetAllBooksQuery, Page<BookResponse>> getAllBooks, CommandUseCase<MarkBookAsAvailableCommand, BookResponse> markAsAvailable, CommandUseCase<MarkBookAsUnavailableCommand, BookResponse> markAsUnavailable) {
        this.createBook = createBook;
        this.updateBook = updateBook;
        this.getBookByIsbn = getBookByIsbn;
        this.getAllBooks = getAllBooks;
        this.markAsAvailable = markAsAvailable;
        this.markAsUnavailable = markAsUnavailable;
    }


    @PostMapping
    public ResponseEntity<ApiResponse<BookResponse>> createBook(
            @Valid @RequestBody CreateBookRequest request) {

        CreateBookCommand command = BookMapper.toCommand(request);
        BookResponse book = createBook.execute(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Book created successfully", book));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(
            @PathVariable String id,
            @Valid @RequestBody UpdateBookRequest request) {

        UpdateBookCommand command = BookMapper.toCommand(id, request);
        BookResponse book = updateBook.execute(command);

        return ResponseEntity.ok(
                ApiResponse.success("Book updated successfully", book)
        );
    }

    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<ApiResponse<BookResponse>> getBookByIsbn(
            @PathVariable String isbn) {

        GetBookByIsbnQuery query = new GetBookByIsbnQuery(isbn);
        BookResponse book = getBookByIsbn.execute(query);

        return ResponseEntity.ok(ApiResponse.success(book));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookResponse>>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        GetAllBooksQuery query = new GetAllBooksQuery(page, size, sortBy, sortDirection);
        Page<BookResponse> books = getAllBooks.execute(query);

        return ResponseEntity.ok(ApiResponse.success(books));
    }

    @PatchMapping("/{id}/available")
    public ResponseEntity<ApiResponse<BookResponse>> markAsAvailable(
            @PathVariable String id) {

        MarkBookAsAvailableCommand command = BookMapper.toAvailableCommand(id);
        BookResponse book = markAsAvailable.execute(command);

        return ResponseEntity.ok(
                ApiResponse.success("Book marked as available successfully", book)
        );
    }

    @PatchMapping("/{id}/unavailable")
    public ResponseEntity<ApiResponse<BookResponse>> markAsUnavailable(
            @PathVariable String id) {

        MarkBookAsUnavailableCommand command = BookMapper.toUnavailableCommand(id);
        BookResponse book = markAsUnavailable.execute(command);

        return ResponseEntity.ok(
                ApiResponse.success("Book marked as unavailable successfully", book)
        );
    }
}