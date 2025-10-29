package ci.org.application.member.usecases;

import ci.org.application.member.requests.commands.CreateMemberCommand;
import ci.org.application.member.responses.MemberResponse;
import ci.org.domain.member.entities.Member;
import ci.org.domain.member.exceptions.InvalidMemberNameException;
import ci.org.domain.member.repositories.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour CreateMemberUseCase.
 *
 * On teste l'orchestration, pas le domain (déjà testé).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateMemberUseCase Tests")
class CreateMemberUseCaseTest {

    @Mock
    private MemberRepository memberRepository;

    private CreateMemberUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateMemberUseCase(memberRepository);
    }

    @Test
    @DisplayName("Should create member successfully")
    void shouldCreateMemberSuccessfully() {
        // Given
        CreateMemberCommand command = new CreateMemberCommand("John Doe");
        Member savedMember = Member.create("John Doe");

        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        // When
        MemberResponse response = useCase.execute(command);

        // Then
        assertNotNull(response);
        assertEquals("John Doe", response.name());
        assertTrue(response.canBorrow());
        assertEquals(5, response.maxSimultaneousLoans());

        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("Should throw exception when name is too short")
    void shouldThrowExceptionWhenNameIsTooShort() {
        // Given
        CreateMemberCommand command = new CreateMemberCommand("A");  // Trop court (< 2)

        // When & Then
        assertThrows(InvalidMemberNameException.class, () -> {
            useCase.execute(command);
        });

        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("Should throw exception when command is null")
    void shouldThrowExceptionWhenCommandIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            useCase.execute(null);
        });

        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("Should trim member name before creating")
    void shouldTrimMemberNameBeforeCreating() {
        // Given
        CreateMemberCommand command = new CreateMemberCommand("  John Doe  ");
        Member savedMember = Member.create("John Doe");

        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        // When
        MemberResponse response = useCase.execute(command);

        // Then
        assertEquals("John Doe", response.name());  // Trimmed
        verify(memberRepository, times(1)).save(any(Member.class));
    }
}