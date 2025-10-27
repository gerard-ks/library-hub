package ci.org.application.member.mappers;

import ci.org.application.member.dtos.MemberResponse;
import ci.org.domain.member.entities.Member;

/**
 * Mapper pour convertir entre Member (domain) et MemberResponse (DTO).
 *
 * Pattern : Anti-Corruption Layer (ACL)
 */
public class MemberMapper {

    /**
     * Convertit un Member (domain) en MemberResponse (DTO).
     */
    public static MemberResponse toResponse(Member member) {
        if (member == null) {
            return null;
        }

        return new MemberResponse(
                member.getId().value(),
                member.getName().value(),
                member.canBorrow(),
                member.getMaxSimultaneousLoans()
        );
    }
}
