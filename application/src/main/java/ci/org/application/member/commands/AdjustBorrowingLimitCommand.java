package ci.org.application.member.commands;

/**
 * Command (DTO) pour ajuster la limite d'emprunts d'un membre.
 *
 * Input pour AdjustMemberLimitUseCase.
 */
public record AdjustBorrowingLimitCommand(
        String memberId,
        int newLimit
) {

    public AdjustBorrowingLimitCommand {
        if (memberId == null || memberId.isBlank()) {
            throw new IllegalArgumentException("Member ID cannot be null or blank");
        }
        if (newLimit < 0) {
            throw new IllegalArgumentException("New limit cannot be negative");
        }
    }
}
