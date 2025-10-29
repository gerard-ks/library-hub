package ci.org.application.member.requests.queries;

/**
 * Query (DTO) pour récupérer un membre par son ID.
 *
 * Input pour GetMemberByIdUseCase.
 */
public record FindMemberQuery(
        String memberId
) {

    public FindMemberQuery {
        if (memberId == null || memberId.isBlank()) {
            throw new IllegalArgumentException("Member ID cannot be null or blank");
        }
    }
}
