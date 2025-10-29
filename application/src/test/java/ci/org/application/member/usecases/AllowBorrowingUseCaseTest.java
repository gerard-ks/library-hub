package ci.org.application.member.usecases;

import ci.org.application.member.requests.commands.AllowBorrowingCommand;
import ci.org.application.member.responses.MemberResponse;
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
 * Tests unitaires pour AllowMemberBorrowingUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AllowMemberBorrowingUseCase Tests")
class AllowBorrowingUseCaseTest {

    @Mock
    private MemberRepository memberRepository;

    private AllowBorrowingUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AllowBorrowingUseCase(memberRepository);
    }

    @Test
    @DisplayName("Should allow member borrowing successfully")
    void shouldAllowMemberBorrowingSuccessfully() {
        // Given
        Member member = Member.create("John Doe");
        member.forbidBorrowing();  // Interdit initialement
        String memberId = member.getId().value();
        AllowBorrowingCommand command = new AllowBorrowingCommand(memberId);

        assertFalse(member.canBorrow());  // Initialement interdit

        when(memberRepository.findById(any(MemberId.class))).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        MemberResponse response = useCase.execute(command);

        // Then
        assertNotNull(response);
        assertTrue(response.canBorrow());  // Maintenant autorisé

        verify(memberRepository, times(1)).findById(any(MemberId.class));
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("Should throw exception when member not found")
    void shouldThrowExceptionWhenMemberNotFound() {
        // Given
        AllowBorrowingCommand command = new AllowBorrowingCommand("non-existent-id");

        when(memberRepository.findById(any(MemberId.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(MemberNotFoundException.class, () -> {
            useCase.execute(command);
        });

        verify(memberRepository, times(1)).findById(any(MemberId.class));
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("Should throw exception when command is null")
    void shouldThrowExceptionWhenCommandIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            useCase.execute(null);
        });

        verify(memberRepository, never()).findById(any(MemberId.class));
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("Should remain allowed when already allowed")
    void shouldRemainAllowedWhenAlreadyAllowed() {
        // Given
        Member member = Member.create("John Doe");
        String memberId = member.getId().value();
        AllowBorrowingCommand command = new AllowBorrowingCommand(memberId);

        assertTrue(member.canBorrow());  // Déjà autorisé

        when(memberRepository.findById(any(MemberId.class))).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        MemberResponse response = useCase.execute(command);

        // Then
        assertTrue(response.canBorrow());  // Toujours autorisé

        verify(memberRepository, times(1)).findById(any(MemberId.class));
        verify(memberRepository, times(1)).save(any(Member.class));
    }
}