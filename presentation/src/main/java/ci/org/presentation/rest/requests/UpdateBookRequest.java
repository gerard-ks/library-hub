package ci.org.presentation.rest.requests;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de requête pour modifier un livre.
 */
public record UpdateBookRequest(
        @NotBlank(message = "Title cannot be blank")
        String title,

        @NotBlank(message = "Author cannot be blank")
        String author
) {
}