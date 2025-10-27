package ci.org.application.member.queries;

/**
 * Query (DTO) pour récupérer un membre par son ID.
 *
 * Input pour GetMemberByIdUseCase.
 */
public record GetMemberByIdQuery(
        String memberId
) {

    public GetMemberByIdQuery {
        if (memberId == null || memberId.isBlank()) {
            throw new IllegalArgumentException("Member ID cannot be null or blank");
        }
    }
}
