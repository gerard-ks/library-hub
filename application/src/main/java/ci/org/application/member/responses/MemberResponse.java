package ci.org.application.member.responses;

/**
 * DTO de réponse pour un membre.
 *
 * Output de tous les Use Cases Member.
 */
public record MemberResponse(
        String id,
        String name,
        boolean canBorrow,
        int maxSimultaneousLoans
) {
}
