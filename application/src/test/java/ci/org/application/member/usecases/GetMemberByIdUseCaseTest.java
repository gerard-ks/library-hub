package ci.org.application.member.usecases;

import ci.org.application.member.dtos.MemberResponse;
import ci.org.application.member.queries.GetMemberByIdQuery;
import ci.org.domain.member.entities.Member;
import ci.org.domain.member.exceptions.MemberNotFoundException;
import ci.org.domain.member.repositories.MemberRepository;
import ci.org.domain.member.valueobjects.MemberId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour GetMemberByIdUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetMemberByIdUseCase Tests")
class GetMemberByIdUseCaseTest {

    @Mock
    private MemberRepository memberRepository;

    private GetMemberByIdUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetMemberByIdUseCase(memberRepository);
    }

    @Test
    @DisplayName("Should get member by ID successfully")
    void shouldGetMemberByIdSuccessfully() {
        // Given
        Member member = Member.create("John Doe");
        String memberId = member.getId().value();
        GetMemberByIdQuery query = new GetMemberByIdQuery(memberId);

        when(memberRepository.findById(any(MemberId.class))).thenReturn(Optional.of(member));

        // When
        MemberResponse response = useCase.execute(query);

        // Then
        assertNotNull(response);
        assertEquals(memberId, response.id());
        assertEquals("John Doe", response.name());
        assertTrue(response.canBorrow());
        assertEquals(5, response.maxSimultaneousLoans());

        verify(memberRepository, times(1)).findById(any(MemberId.class));
    }

    @Test
    @DisplayName("Should throw exception when member not found")
    void shouldThrowExceptionWhenMemberNotFound() {
        // Given
        GetMemberByIdQuery query = new GetMemberByIdQuery("non-existent-id");

        when(memberRepository.findById(any(MemberId.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(MemberNotFoundException.class, () -> {
            useCase.execute(query);
        });

        verify(memberRepository, times(1)).findById(any(MemberId.class));
    }

    @Test
    @DisplayName("Should throw exception when query is null")
    void shouldThrowExceptionWhenQueryIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            useCase.execute(null);
        });

        verify(memberRepository, never()).findById(any(MemberId.class));
    }

    @Test
    @DisplayName("Should return member with modified state")
    void shouldReturnMemberWithModifiedState() {
        // Given
        Member member = Member.create("John Doe");
        member.forbidBorrowing();
        member.adjustBorrowingLimit(10);
        String memberId = member.getId().value();
        GetMemberByIdQuery query = new GetMemberByIdQuery(memberId);

        when(memberRepository.findById(any(MemberId.class))).thenReturn(Optional.of(member));

        // When
        MemberResponse response = useCase.execute(query);

        // Then
        assertFalse(response.canBorrow());  // Forbidden
        assertEquals(10, response.maxSimultaneousLoans());  // Adjusted
    }
}