package ci.org.presentation.rest.requests;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de requête pour créer un membre.
 */
public record CreateMemberRequest(
        @NotBlank(message = "Name cannot be blank")
        String name
) {
}