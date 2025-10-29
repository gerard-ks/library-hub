package ci.org.application.member.usecases;

import ci.org.application.member.requests.commands.ForbidBorrowingCommand;
import ci.org.application.member.responses.MemberResponse;
import ci.org.domain.member.entities.Member;
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
 * Tests unitaires pour ForbidBorrowingUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ForbidBorrowingUseCase Tests")
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

        when(memberRepository.findById(any(MemberId.class)))
                .thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MemberResponse response = useCase.execute(command);

        // Then
        assertNotNull(response);
        assertFalse(response.canBorrow());  // Maintenant interdit
        assertEquals(memberId, response.id());
        assertEquals("John Doe", response.name());

        // Vérifier que la méthode forbidBorrowing a bien été appelée sur l'entité
        assertFalse(member.canBorrow());

        verify(memberRepository).findById(MemberId.of(memberId));
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("Should throw exception when member not found")
    void shouldThrowExceptionWhenMemberNotFound() {
        // Given
        String nonExistentId = "non-existent-id";
        ForbidBorrowingCommand command = new ForbidBorrowingCommand(nonExistentId);

        when(memberRepository.findById(any(MemberId.class)))
                .thenReturn(Optional.empty());

        // When & Then
        MemberNotFoundException exception = assertThrows(
                MemberNotFoundException.class,
                () -> useCase.execute(command)
        );

        assertTrue(exception.getMessage().contains(nonExistentId));

        verify(memberRepository).findById(MemberId.of(nonExistentId));
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

        when(memberRepository.findById(any(MemberId.class)))
                .thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MemberResponse response = useCase.execute(command);

        // Then
        assertFalse(response.canBorrow());  // Toujours interdit
        assertFalse(member.canBorrow());

        verify(memberRepository).findById(MemberId.of(memberId));
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("Should only update canBorrow and not other properties")
    void shouldOnlyUpdateCanBorrowAndNotOtherProperties() {
        // Given
        Member member = Member.create("John Doe");
        member.adjustBorrowingLimit(10);

        String memberId = member.getId().value();
        String originalName = member.getNameValue();
        int originalLimit = member.getMaxSimultaneousLoans();

        ForbidBorrowingCommand command = new ForbidBorrowingCommand(memberId);

        when(memberRepository.findById(any(MemberId.class)))
                .thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MemberResponse response = useCase.execute(command);

        // Then - Vérifier que seul canBorrow a changé
        assertFalse(response.canBorrow());
        assertEquals(originalName, member.getNameValue());
        assertEquals(originalLimit, member.getMaxSimultaneousLoans());
        assertEquals(memberId, member.getId().value());
    }

    @Test
    @DisplayName("Should preserve member ID when forbidding borrowing")
    void shouldPreserveMemberIdWhenForbiddingBorrowing() {
        // Given
        Member member = Member.create("John Doe");
        String memberId = member.getId().value();
        ForbidBorrowingCommand command = new ForbidBorrowingCommand(memberId);

        when(memberRepository.findById(any(MemberId.class)))
                .thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MemberResponse response = useCase.execute(command);

        // Then
        assertEquals(memberId, response.id());
        assertEquals(memberId, member.getId().value());
    }

    @Test
    @DisplayName("Should call save with the correct member")
    void shouldCallSaveWithTheCorrectMember() {
        // Given
        Member member = Member.create("John Doe");
        String memberId = member.getId().value();
        ForbidBorrowingCommand command = new ForbidBorrowingCommand(memberId);

        when(memberRepository.findById(any(MemberId.class)))
                .thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.execute(command);

        // Then
        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());

        Member savedMember = memberCaptor.getValue();
        assertFalse(savedMember.canBorrow());
        assertEquals(memberId, savedMember.getId().value());
    }

    @Test
    @DisplayName("Should change from allowed to forbidden")
    void shouldChangeFromAllowedToForbidden() {
        // Given
        Member member = Member.create("John Doe");
        assertTrue(member.canBorrow());  // État initial : autorisé

        String memberId = member.getId().value();
        ForbidBorrowingCommand command = new ForbidBorrowingCommand(memberId);

        when(memberRepository.findById(any(MemberId.class)))
                .thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MemberResponse response = useCase.execute(command);

        // Then - Vérifier la transition d'état
        assertFalse(response.canBorrow());  // État final : interdit
        assertFalse(member.canBorrow());
    }
}