package ci.org.application.member.usecases;

import ci.org.application.member.responses.MemberResponse;
import ci.org.application.member.requests.queries.ListMembersQuery;
import ci.org.domain.member.entities.Member;
import ci.org.domain.member.repositories.MemberRepository;
import ci.org.shared.kernel.Page;
import ci.org.shared.kernel.PageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour GetAllMembersUseCase.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetAllMembersUseCase Tests")
class GetAllMembersUseCaseTest {

    @Mock
    private MemberRepository memberRepository;

    private GetAllMembersUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetAllMembersUseCase(memberRepository);
    }

    @Test
    @DisplayName("Should get all members successfully")
    void shouldGetAllMembersSuccessfully() {
        // Given
        ListMembersQuery query = new ListMembersQuery(0, 20, "name", "ASC");

        Member member1 = Member.create("Alice");
        Member member2 = Member.create("Bob");
        Member member3 = Member.create("Charlie");

        List<Member> members = Arrays.asList(member1, member2, member3);
        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<Member> memberPage = Page.of(members, pageRequest, 3);

        when(memberRepository.findAll(any(PageRequest.class))).thenReturn(memberPage);

        // When
        Page<MemberResponse> responsePage = useCase.execute(query);

        // Then
        assertNotNull(responsePage);
        assertEquals(3, responsePage.getContent().size());
        assertEquals(3, responsePage.getTotalElements());
        assertEquals(0, responsePage.getPageNumber());
        assertEquals(20, responsePage.getPageSize());
        assertEquals(1, responsePage.getTotalPages());

        assertEquals("Alice", responsePage.getContent().get(0).name());
        assertEquals("Bob", responsePage.getContent().get(1).name());
        assertEquals("Charlie", responsePage.getContent().get(2).name());

        verify(memberRepository, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    @DisplayName("Should return empty page when no members")
    void shouldReturnEmptyPageWhenNoMembers() {
        // Given
        ListMembersQuery query = new ListMembersQuery(0, 20, "name", "ASC");

        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<Member> emptyPage = Page.of(Collections.emptyList(), pageRequest, 0);

        when(memberRepository.findAll(any(PageRequest.class))).thenReturn(emptyPage);

        // When
        Page<MemberResponse> responsePage = useCase.execute(query);

        // Then
        assertNotNull(responsePage);
        assertTrue(responsePage.isEmpty());
        assertEquals(0, responsePage.getTotalElements());
        assertEquals(0, responsePage.getNumberOfElements());

        verify(memberRepository, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void shouldHandlePaginationCorrectly() {
        // Given - Page 1 (10 éléments par page, total 25 éléments)
        ListMembersQuery query = new ListMembersQuery(1, 10, "name", "ASC");

        List<Member> members = Arrays.asList(
                Member.create("Member 11"),
                Member.create("Member 12"),
                Member.create("Member 13")
        );

        PageRequest pageRequest = PageRequest.of(1, 10);
        Page<Member> memberPage = Page.of(members, pageRequest, 25);  // Total 25

        when(memberRepository.findAll(any(PageRequest.class))).thenReturn(memberPage);

        // When
        Page<MemberResponse> responsePage = useCase.execute(query);

        // Then
        assertEquals(1, responsePage.getPageNumber());  // Page 1
        assertEquals(10, responsePage.getPageSize());   // 10 par page
        assertEquals(25, responsePage.getTotalElements());  // 25 au total
        assertEquals(3, responsePage.getTotalPages());  // ceil(25/10) = 3 pages
        assertEquals(3, responsePage.getNumberOfElements());  // 3 éléments dans cette page

        assertFalse(responsePage.isFirst());  // Pas la première page
        assertFalse(responsePage.isLast());   // Pas la dernière page (page 2 existe)
        assertTrue(responsePage.hasPrevious());  // A une page précédente
        assertTrue(responsePage.hasNext());  // A une page suivante
    }

    @Test
    @DisplayName("Should map all member properties correctly")
    void shouldMapAllMemberPropertiesCorrectly() {
        // Given
        ListMembersQuery query = new ListMembersQuery(0, 10, "name", "ASC");

        Member member = Member.create("John Doe");
        member.forbidBorrowing();
        member.adjustBorrowingLimit(10);

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Member> memberPage = Page.of(List.of(member), pageRequest, 1);

        when(memberRepository.findAll(any(PageRequest.class))).thenReturn(memberPage);

        // When
        Page<MemberResponse> responsePage = useCase.execute(query);

        // Then
        assertEquals(1, responsePage.getContent().size());
        MemberResponse response = responsePage.getContent().get(0);

        assertEquals(member.getId().value(), response.id());
        assertEquals("John Doe", response.name());
        assertFalse(response.canBorrow());
        assertEquals(10, response.maxSimultaneousLoans());
    }

    @Test
    @DisplayName("Should handle DESC sort direction")
    void shouldHandleDescSortDirection() {
        // Given
        ListMembersQuery query = new ListMembersQuery(0, 20, "name", "DESC");

        Member member1 = Member.create("Charlie");
        Member member2 = Member.create("Bob");
        Member member3 = Member.create("Alice");

        List<Member> members = Arrays.asList(member1, member2, member3);  // Déjà trié DESC
        PageRequest pageRequest = PageRequest.of(0, 20);
        Page<Member> memberPage = Page.of(members, pageRequest, 3);

        when(memberRepository.findAll(any(PageRequest.class))).thenReturn(memberPage);

        // When
        Page<MemberResponse> responsePage = useCase.execute(query);

        // Then
        assertEquals("Charlie", responsePage.getContent().get(0).name());
        assertEquals("Bob", responsePage.getContent().get(1).name());
        assertEquals("Alice", responsePage.getContent().get(2).name());

        verify(memberRepository, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    @DisplayName("Should throw exception when query is null")
    void shouldThrowExceptionWhenQueryIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            useCase.execute(null);
        });

        verify(memberRepository, never()).findAll(any(PageRequest.class));
    }
}