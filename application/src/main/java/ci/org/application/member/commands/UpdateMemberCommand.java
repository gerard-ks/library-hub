package ci.org.application.member.commands;

/**
 * Command (DTO) pour mettre à jour un membre.
 *
 * Input pour UpdateMemberUseCase.
 */
public record UpdateMemberCommand(
        String memberId,
        String newName
) {

    public UpdateMemberCommand {
        if (memberId == null || memberId.isBlank()) {
            throw new IllegalArgumentException("Member ID cannot be null or blank");
        }
        if (newName == null) {
            throw new IllegalArgumentException("New name cannot be null");
        }
    }
}
