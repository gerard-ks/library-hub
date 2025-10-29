package ci.org.application.member.requests.commands;

/**
 * Command (DTO) pour créer un membre.
 *
 * Input pour CreateMemberUseCase.
 */
public record CreateMemberCommand(
        String name
) {

    /**
     * Validation basique (non null).
     * La validation métier est dans le domain.
     */
    public CreateMemberCommand {
        if (name == null) {
            throw new IllegalArgumentException("Member name cannot be null");
        }
    }
}
