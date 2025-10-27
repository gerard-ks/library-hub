package ci.org.presentation.rest.requests;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de requête pour modifier un membre.
 */
public record UpdateMemberRequest(
        @NotBlank(message = "Name cannot be blank")
        String name
) {
}