package ci.org.presentation.rest.mappers;

import ci.org.application.book.commands.*;
import ci.org.presentation.rest.requests.*;

/**
 * Mapper pour convertir les Request REST en Commands Application.
 */
public class BookMapper {

    public static CreateBookCommand toCommand(CreateBookRequest request) {
        return new CreateBookCommand(
                request.isbn(),
                request.title(),
                request.author()
        );
    }

    public static UpdateBookCommand toCommand(String bookId, UpdateBookRequest request) {
        return new UpdateBookCommand(
                bookId,
                request.title(),
                request.author()
        );
    }

    public static MarkBookAsAvailableCommand toAvailableCommand(String bookId) {
        return new MarkBookAsAvailableCommand(bookId);
    }

    public static MarkBookAsUnavailableCommand toUnavailableCommand(String bookId) {
        return new MarkBookAsUnavailableCommand(bookId);
    }
}