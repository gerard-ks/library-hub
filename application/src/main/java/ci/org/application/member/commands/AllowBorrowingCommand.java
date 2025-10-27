package ci.org.application.member.commands;

/**
 * Command (DTO) pour autoriser l'emprunt à un membre.
 */
public record AllowBorrowingCommand(String memberId) {
    public AllowBorrowingCommand {
        if (memberId == null || memberId.isBlank()) {
            throw new IllegalArgumentException("Member ID cannot be null or blank");
        }
    }
}
