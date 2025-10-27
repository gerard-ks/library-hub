package ci.org.presentation.rest.controllers;

import ci.org.application.member.commands.*;
import ci.org.application.member.dtos.MemberResponse;
import ci.org.application.member.queries.*;
import ci.org.presentation.rest.mappers.MemberMapper;
import ci.org.presentation.rest.requests.*;
import ci.org.presentation.rest.responses.ApiResponse;
import ci.org.shared.kernel.CommandUseCase;
import ci.org.shared.kernel.Page;
import ci.org.shared.kernel.QueryUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour la gestion des membres.
 */
@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final CommandUseCase<CreateMemberCommand, MemberResponse> createMember;
    private final CommandUseCase<UpdateMemberCommand, MemberResponse> updateMember;
    private final QueryUseCase<GetMemberByIdQuery, MemberResponse> getMemberById;
    private final QueryUseCase<GetAllMembersQuery, Page<MemberResponse>> getAllMembers;
    private final CommandUseCase<ForbidBorrowingCommand, MemberResponse> forbidBorrowing;
    private final CommandUseCase<AllowBorrowingCommand, MemberResponse> allowBorrowing;
    private final CommandUseCase<AdjustBorrowingLimitCommand, MemberResponse> adjustLimit;

    public MemberController(CommandUseCase<CreateMemberCommand, MemberResponse> createMember, CommandUseCase<UpdateMemberCommand, MemberResponse> updateMember, QueryUseCase<GetMemberByIdQuery, MemberResponse> getMemberById, QueryUseCase<GetAllMembersQuery, Page<MemberResponse>> getAllMembers, CommandUseCase<ForbidBorrowingCommand, MemberResponse> forbidBorrowing, CommandUseCase<AllowBorrowingCommand, MemberResponse> allowBorrowing, CommandUseCase<AdjustBorrowingLimitCommand, MemberResponse> adjustLimit) {
        this.createMember = createMember;
        this.updateMember = updateMember;
        this.getMemberById = getMemberById;
        this.getAllMembers = getAllMembers;
        this.forbidBorrowing = forbidBorrowing;
        this.allowBorrowing = allowBorrowing;
        this.adjustLimit = adjustLimit;
    }


    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponse>> createMember(
            @Valid @RequestBody CreateMemberRequest request) {

        CreateMemberCommand command = MemberMapper.toCommand(request);

        MemberResponse member = createMember.execute(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Member created successfully", member));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> updateMember(
            @PathVariable String id,
            @Valid @RequestBody UpdateMemberRequest request) {

        UpdateMemberCommand command = MemberMapper.toCommand(id, request);
        MemberResponse member = updateMember.execute(command);

        return ResponseEntity.ok(
                ApiResponse.success("Member updated successfully", member)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> getMemberById(
            @PathVariable String id) {

        GetMemberByIdQuery query = new GetMemberByIdQuery(id);
        MemberResponse member = getMemberById.execute(query);

        return ResponseEntity.ok(ApiResponse.success(member));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MemberResponse>>> getAllMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        GetAllMembersQuery query = new GetAllMembersQuery(page, size, sortBy, sortDirection);
        Page<MemberResponse> members = getAllMembers.execute(query);

        return ResponseEntity.ok(ApiResponse.success(members));
    }

    @PatchMapping("/{id}/forbid")
    public ResponseEntity<ApiResponse<MemberResponse>> forbidBorrowing(
            @PathVariable String id) {

        ForbidBorrowingCommand command = MemberMapper.toForbidCommand(id);
        MemberResponse member = forbidBorrowing.execute(command);

        return ResponseEntity.ok(
                ApiResponse.success("Member borrowing forbidden successfully", member)
        );
    }

    @PatchMapping("/{id}/allow")
    public ResponseEntity<ApiResponse<MemberResponse>> allowBorrowing(
            @PathVariable String id) {

        AllowBorrowingCommand command = MemberMapper.toAllowCommand(id);
        MemberResponse member = allowBorrowing.execute(command);

        return ResponseEntity.ok(
                ApiResponse.success("Member borrowing allowed successfully", member)
        );
    }

    @PatchMapping("/{id}/limit")
    public ResponseEntity<ApiResponse<MemberResponse>> adjustLimit(
            @PathVariable String id,
            @Valid @RequestBody AdjustMemberLimitRequest request) {

        AdjustBorrowingLimitCommand command = MemberMapper.toCommand(id, request);
        MemberResponse member = adjustLimit.execute(command);

        return ResponseEntity.ok(
                ApiResponse.success("Member limit adjusted successfully", member)
        );
    }
}