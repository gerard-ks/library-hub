package ci.org.application.member.requests.commands;

/**
 * Command (DTO) pour interdire l'emprunt à un membre.
 */
public record ForbidBorrowingCommand(
        String memberId
) {
    public ForbidBorrowingCommand {
        if (memberId == null || memberId.isBlank()) {
            throw new IllegalArgumentException("Member ID cannot be null or blank");
        }
    }
}
