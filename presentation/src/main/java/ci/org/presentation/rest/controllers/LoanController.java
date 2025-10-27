package ci.org.presentation.rest.controllers;

import ci.org.application.loan.commands.CreateLoanCommand;
import ci.org.application.loan.commands.MarkLoanAsOverdueCommand;
import ci.org.application.loan.commands.MarkLoanAsReturnedCommand;
import ci.org.application.loan.dtos.LoanResponse;
import ci.org.application.loan.queries.*;
import ci.org.presentation.rest.mappers.LoanMapper;
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
 * Contrôleur REST pour la gestion des emprunts.
 */
@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final CommandUseCase<CreateLoanCommand, LoanResponse> createLoan;
    private final QueryUseCase<GetLoanByIdQuery, LoanResponse> getLoanById;
    private final QueryUseCase<GetActiveLoansQuery, Page<LoanResponse>> getActiveLoans;
    private final QueryUseCase<GetOverdueLoansQuery, Page<LoanResponse>> getOverdueLoans;
    private final QueryUseCase<GetLoansByMemberIdQuery, Page<LoanResponse>> getLoansByMemberId;
    private final CommandUseCase<MarkLoanAsReturnedCommand, LoanResponse> markAsReturned;
    private final CommandUseCase<MarkLoanAsOverdueCommand, LoanResponse> markAsOverdue;

    public LoanController(CommandUseCase<CreateLoanCommand, LoanResponse> createLoan, QueryUseCase<GetLoanByIdQuery, LoanResponse> getLoanById, QueryUseCase<GetActiveLoansQuery, Page<LoanResponse>> getActiveLoans, QueryUseCase<GetOverdueLoansQuery, Page<LoanResponse>> getOverdueLoans, QueryUseCase<GetLoansByMemberIdQuery, Page<LoanResponse>> getLoansByMemberId, CommandUseCase<MarkLoanAsReturnedCommand, LoanResponse> markAsReturned, CommandUseCase<MarkLoanAsOverdueCommand, LoanResponse> markAsOverdue) {
        this.createLoan = createLoan;
        this.getLoanById = getLoanById;
        this.getActiveLoans = getActiveLoans;
        this.getOverdueLoans = getOverdueLoans;
        this.getLoansByMemberId = getLoansByMemberId;
        this.markAsReturned = markAsReturned;
        this.markAsOverdue = markAsOverdue;
    }


    @PostMapping
    public ResponseEntity<ApiResponse<LoanResponse>> createLoan(
            @Valid @RequestBody CreateLoanRequest request) {

        CreateLoanCommand command = LoanMapper.toCommand(request);
        LoanResponse loan = createLoan.execute(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Loan created successfully", loan));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LoanResponse>> getLoanById(
            @PathVariable String id) {

        GetLoanByIdQuery query = new GetLoanByIdQuery(id);
        LoanResponse loan = getLoanById.execute(query);

        return ResponseEntity.ok(ApiResponse.success(loan));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<Page<LoanResponse>>> getActiveLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "borrowDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        GetActiveLoansQuery query = new GetActiveLoansQuery(page, size, sortBy, sortDirection);
        Page<LoanResponse> loans = getActiveLoans.execute(query);

        return ResponseEntity.ok(ApiResponse.success(loans));
    }

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<Page<LoanResponse>>> getOverdueLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dueDate") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        GetOverdueLoansQuery query = new GetOverdueLoansQuery(page, size, sortBy, sortDirection);
        Page<LoanResponse> loans = getOverdueLoans.execute(query);

        return ResponseEntity.ok(ApiResponse.success(loans));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<ApiResponse<Page<LoanResponse>>> getLoansByMemberId(
            @PathVariable String memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "borrowDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        GetLoansByMemberIdQuery query = new GetLoansByMemberIdQuery(memberId, page, size, sortBy, sortDirection);
        Page<LoanResponse> loans = getLoansByMemberId.execute(query);

        return ResponseEntity.ok(ApiResponse.success(loans));
    }

    @PatchMapping("/{id}/return")
    public ResponseEntity<ApiResponse<LoanResponse>> markAsReturned(
            @PathVariable String id,
            @Valid @RequestBody ReturnLoanRequest request) {

        MarkLoanAsReturnedCommand command = LoanMapper.toCommand(id, request);
        LoanResponse loan = markAsReturned.execute(command);

        return ResponseEntity.ok(
                ApiResponse.success("Loan marked as returned successfully", loan)
        );
    }

    @PatchMapping("/{id}/overdue")
    public ResponseEntity<ApiResponse<LoanResponse>> markAsOverdue(
            @PathVariable String id) {

        MarkLoanAsOverdueCommand command = LoanMapper.toOverdueCommand(id);
        LoanResponse loan = markAsOverdue.execute(command);

        return ResponseEntity.ok(
                ApiResponse.success("Loan marked as overdue successfully", loan)
        );
    }
}