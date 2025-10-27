package ci.org.application.member.usecases;

import ci.org.application.member.commands.ForbidBorrowingCommand;
import ci.org.application.member.dtos.MemberResponse;
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
 * Tests unitaires pour ForbidMemberBorrowingUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ForbidMemberBorrowingUseCase Tests")
class ForbidBorrowingUseCaseTest {

    @Mock
    private MemberRepository memberRepository;

    private ForbidBorrowingUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ForbidBorrowingUseCase(memberRepository);
    }

    @Test
    @DisplayName("Should forbid member borrowing successfully")
    void shouldForbidMemberBorrowingSuccessfully() {
        // Given
        Member member = Member.create("John Doe");
        String memberId = member.getId().value();
        ForbidBorrowingCommand command = new ForbidBorrowingCommand(memberId);

        assertTrue(member.canBorrow());  // Initialement autorisé

        when(memberRepository.findById(any(MemberId.class))).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        MemberResponse response = useCase.execute(command);

        // Then
        assertNotNull(response);
        assertFalse(response.canBorrow());  // Maintenant interdit

        verify(memberRepository, times(1)).findById(any(MemberId.class));
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("Should throw exception when member not found")
    void shouldThrowExceptionWhenMemberNotFound() {
        // Given
        ForbidBorrowingCommand command = new ForbidBorrowingCommand("non-existent-id");

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
    @DisplayName("Should remain forbidden when already forbidden")
    void shouldRemainForbiddenWhenAlreadyForbidden() {
        // Given
        Member member = Member.create("John Doe");
        member.forbidBorrowing();  // Déjà interdit
        String memberId = member.getId().value();
        ForbidBorrowingCommand command = new ForbidBorrowingCommand(memberId);

        assertFalse(member.canBorrow());  // Déjà interdit

        when(memberRepository.findById(any(MemberId.class))).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        MemberResponse response = useCase.execute(command);

        // Then
        assertFalse(response.canBorrow());  // Toujours interdit

        verify(memberRepository, times(1)).findById(any(MemberId.class));
        verify(memberRepository, times(1)).save(any(Member.class));
    }
}