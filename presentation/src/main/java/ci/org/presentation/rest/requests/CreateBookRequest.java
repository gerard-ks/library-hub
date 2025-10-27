package ci.org.presentation.rest.requests;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de requête pour créer un livre.
 */
public record CreateBookRequest(
        @NotBlank(message = "ISBN cannot be blank")
        String isbn,

        @NotBlank(message = "Title cannot be blank")
        String title,

        @NotBlank(message = "Author cannot be blank")
        String author
) {
}