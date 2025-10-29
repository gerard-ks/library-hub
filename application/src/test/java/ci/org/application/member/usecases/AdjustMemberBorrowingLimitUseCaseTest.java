package ci.org.application.member.usecases;

import ci.org.application.member.requests.commands.AdjustBorrowingLimitCommand;
import ci.org.application.member.responses.MemberResponse;
import ci.org.domain.member.entities.Member;
import ci.org.domain.member.exceptions.InvalidBorrowingLimitException;
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
 * Tests unitaires pour AdjustMemberLimitUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdjustMemberLimitUseCase Tests")
class AdjustMemberBorrowingLimitUseCaseTest {

    @Mock
    private MemberRepository memberRepository;

    private AdjustMemberBorrowingLimitUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AdjustMemberBorrowingLimitUseCase(memberRepository);
    }

    @Test
    @DisplayName("Should adjust member limit successfully")
    void shouldAdjustMemberLimitSuccessfully() {
        // Given
        Member member = Member.create("John Doe");
        String memberId = member.getId().value();
        AdjustBorrowingLimitCommand command = new AdjustBorrowingLimitCommand(memberId, 10);

        assertEquals(5, member.getMaxSimultaneousLoans());  // Limite par défaut

        when(memberRepository.findById(any(MemberId.class))).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        MemberResponse response = useCase.execute(command);

        // Then
        assertNotNull(response);
        assertEquals(10, response.maxSimultaneousLoans());

        verify(memberRepository, times(1)).findById(any(MemberId.class));
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("Should throw exception when member not found")
    void shouldThrowExceptionWhenMemberNotFound() {
        // Given
        AdjustBorrowingLimitCommand command = new AdjustBorrowingLimitCommand("non-existent-id", 10);

        when(memberRepository.findById(any(MemberId.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(MemberNotFoundException.class, () -> {
            useCase.execute(command);
        });

        verify(memberRepository, times(1)).findById(any(MemberId.class));
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("Should throw exception when limit is too low")
    void shouldThrowExceptionWhenLimitIsTooLow() {
        // Given
        Member member = Member.create("John Doe");
        String memberId = member.getId().value();
        AdjustBorrowingLimitCommand command = new AdjustBorrowingLimitCommand(memberId, 0);  // < 1

        when(memberRepository.findById(any(MemberId.class))).thenReturn(Optional.of(member));

        // When & Then
        assertThrows(InvalidBorrowingLimitException.class, () -> {
            useCase.execute(command);
        });

        verify(memberRepository, times(1)).findById(any(MemberId.class));
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("Should throw exception when limit is too high")
    void shouldThrowExceptionWhenLimitIsTooHigh() {
        // Given
        Member member = Member.create("John Doe");
        String memberId = member.getId().value();
        AdjustBorrowingLimitCommand command = new AdjustBorrowingLimitCommand(memberId, 21);  // > 20

        when(memberRepository.findById(any(MemberId.class))).thenReturn(Optional.of(member));

        // When & Then
        assertThrows(InvalidBorrowingLimitException.class, () -> {
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
    @DisplayName("Should adjust to minimum limit")
    void shouldAdjustToMinimumLimit() {
        // Given
        Member member = Member.create("John Doe");
        String memberId = member.getId().value();
        AdjustBorrowingLimitCommand command = new AdjustBorrowingLimitCommand(memberId, 1);  // Min = 1

        when(memberRepository.findById(any(MemberId.class))).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        MemberResponse response = useCase.execute(command);

        // Then
        assertEquals(1, response.maxSimultaneousLoans());
    }

    @Test
    @DisplayName("Should adjust to maximum limit")
    void shouldAdjustToMaximumLimit() {
        // Given
        Member member = Member.create("John Doe");
        String memberId = member.getId().value();
        AdjustBorrowingLimitCommand command = new AdjustBorrowingLimitCommand(memberId, 20);  // Max = 20

        when(memberRepository.findById(any(MemberId.class))).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        MemberResponse response = useCase.execute(command);

        // Then
        assertEquals(20, response.maxSimultaneousLoans());
    }
}