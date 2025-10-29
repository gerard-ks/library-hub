package ci.org.application.member.usecases;

import ci.org.application.member.requests.commands.UpdateMemberCommand;
import ci.org.application.member.responses.MemberResponse;
import ci.org.domain.member.entities.Member;
import ci.org.domain.member.exceptions.InvalidMemberNameException;
import ci.org.domain.member.exceptions.MemberNotFoundException;
import ci.org.domain.member.repositories.MemberRepository;
import ci.org.domain.member.valueobjects.MemberId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

        when(memberRepository.findById(any(MemberId.class)))
                .thenReturn(Optional.of(existingMember));
        when(memberRepository.save(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MemberResponse response = useCase.execute(command);

        // Then
        assertNotNull(response);
        assertEquals("Jane Smith", response.name());
        assertEquals(memberId, response.id());

        // Vérifier que updateName a bien été appelée
        assertEquals("Jane Smith", existingMember.getNameValue());

        verify(memberRepository).findById(MemberId.of(memberId));
        verify(memberRepository).save(existingMember);
    }

    @Test
    @DisplayName("Should throw exception when member not found")
    void shouldThrowExceptionWhenMemberNotFound() {
        // Given
        String nonExistentId = "non-existent-id";
        UpdateMemberCommand command = new UpdateMemberCommand(nonExistentId, "New Name");

        when(memberRepository.findById(any(MemberId.class)))
                .thenReturn(Optional.empty());

        // When & Then
        MemberNotFoundException exception = assertThrows(
                MemberNotFoundException.class,
                () -> useCase.execute(command)
        );

        // Vérifier le message d'erreur
        assertTrue(exception.getMessage().contains(nonExistentId));

        verify(memberRepository).findById(MemberId.of(nonExistentId));
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("Should throw exception when new name is invalid")
    void shouldThrowExceptionWhenNewNameIsInvalid() {
        // Given
        Member existingMember = Member.create("John Doe");
        String memberId = existingMember.getId().value();
        UpdateMemberCommand command = new UpdateMemberCommand(memberId, "X");  // Trop court

        when(memberRepository.findById(any(MemberId.class)))
                .thenReturn(Optional.of(existingMember));

        // When & Then
        assertThrows(InvalidMemberNameException.class, () -> {
            useCase.execute(command);
        });

        verify(memberRepository).findById(MemberId.of(memberId));
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
    @DisplayName("Should preserve member ID when updating name")
    void shouldPreserveMemberIdWhenUpdatingName() {
        // Given
        Member existingMember = Member.create("John Doe");
        String memberId = existingMember.getId().value();
        UpdateMemberCommand command = new UpdateMemberCommand(memberId, "Jane Smith");

        when(memberRepository.findById(any(MemberId.class)))
                .thenReturn(Optional.of(existingMember));
        when(memberRepository.save(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MemberResponse response = useCase.execute(command);

        // Then
        assertEquals(memberId, response.id());
        assertEquals(memberId, existingMember.getId().value());
    }

    @Test
    @DisplayName("Should only update name and not other properties")
    void shouldOnlyUpdateNameAndNotOtherProperties() {
        // Given
        Member existingMember = Member.create("John Doe");
        existingMember.adjustBorrowingLimit(10);
        existingMember.forbidBorrowing();

        String memberId = existingMember.getId().value();
        int originalLimit = existingMember.getMaxSimultaneousLoans();
        boolean originalCanBorrow = existingMember.canBorrow();

        UpdateMemberCommand command = new UpdateMemberCommand(memberId, "Jane Smith");

        when(memberRepository.findById(any(MemberId.class)))
                .thenReturn(Optional.of(existingMember));
        when(memberRepository.save(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MemberResponse response = useCase.execute(command);

        // Then
        assertEquals("Jane Smith", response.name());
        assertEquals(originalLimit, existingMember.getMaxSimultaneousLoans());
        assertEquals(originalCanBorrow, existingMember.canBorrow());
    }

    @Test
    @DisplayName("Should call save with the correct member")
    void shouldCallSaveWithTheCorrectMember() {
        // Given
        Member existingMember = Member.create("John Doe");
        String memberId = existingMember.getId().value();
        UpdateMemberCommand command = new UpdateMemberCommand(memberId, "Jane Smith");

        when(memberRepository.findById(any(MemberId.class)))
                .thenReturn(Optional.of(existingMember));
        when(memberRepository.save(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.execute(command);

        // Then
        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());

        Member savedMember = memberCaptor.getValue();
        assertEquals("Jane Smith", savedMember.getNameValue());
        assertEquals(memberId, savedMember.getId().value());
    }

    @Test
    @DisplayName("Should handle blank new name")
    void shouldHandleBlankNewName() {
        // Given
        Member existingMember = Member.create("John Doe");
        String memberId = existingMember.getId().value();
        UpdateMemberCommand command = new UpdateMemberCommand(memberId, "   ");  // Blank

        when(memberRepository.findById(any(MemberId.class)))
                .thenReturn(Optional.of(existingMember));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            useCase.execute(command);
        });

        verify(memberRepository).findById(MemberId.of(memberId));
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("Should handle name with special characters")
    void shouldHandleNameWithSpecialCharacters() {
        // Given
        Member existingMember = Member.create("John Doe");
        String memberId = existingMember.getId().value();
        UpdateMemberCommand command = new UpdateMemberCommand(memberId, "Jean-François O'Brien");

        when(memberRepository.findById(any(MemberId.class)))
                .thenReturn(Optional.of(existingMember));
        when(memberRepository.save(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MemberResponse response = useCase.execute(command);

        // Then
        assertNotNull(response);
        assertEquals("Jean-François O'Brien", response.name());
    }
}