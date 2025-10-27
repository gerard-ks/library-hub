package ci.org.application.member.usecases;

import ci.org.application.member.commands.UpdateMemberCommand;
import ci.org.application.member.dtos.MemberResponse;
import ci.org.domain.member.entities.Member;
import ci.org.domain.member.exceptions.InvalidMemberNameException;
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
 * Tests unitaires pour UpdateMemberUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateMemberUseCase Tests")
class UpdateMemberUseCaseTest {

    @Mock
    private MemberRepository memberRepository;

    private UpdateMemberUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateMemberUseCase(memberRepository);
    }

    @Test
    @DisplayName("Should update member name successfully")
    void shouldUpdateMemberNameSuccessfully() {
        // Given
        Member existingMember = Member.create("John Doe");
        String memberId = existingMember.getId().value();
        UpdateMemberCommand command = new UpdateMemberCommand(memberId, "Jane Smith");

        when(memberRepository.findById(any(MemberId.class))).thenReturn(Optional.of(existingMember));
        when(memberRepository.save(any(Member.class))).thenReturn(existingMember);

        // When
        MemberResponse response = useCase.execute(command);

        // Then
        assertNotNull(response);
        assertEquals("Jane Smith", response.name());

        verify(memberRepository, times(1)).findById(any(MemberId.class));
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("Should throw exception when member not found")
    void shouldThrowExceptionWhenMemberNotFound() {
        // Given
        UpdateMemberCommand command = new UpdateMemberCommand("non-existent-id", "New Name");

        when(memberRepository.findById(any(MemberId.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(MemberNotFoundException.class, () -> {
            useCase.execute(command);
        });

        verify(memberRepository, times(1)).findById(any(MemberId.class));
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("Should throw exception when new name is invalid")
    void shouldThrowExceptionWhenNewNameIsInvalid() {
        // Given
        Member existingMember = Member.create("John Doe");
        String memberId = existingMember.getId().value();
        UpdateMemberCommand command = new UpdateMemberCommand(memberId, "X");  // Trop court

        when(memberRepository.findById(any(MemberId.class))).thenReturn(Optional.of(existingMember));

        // When & Then
        assertThrows(InvalidMemberNameException.class, () -> {
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
}