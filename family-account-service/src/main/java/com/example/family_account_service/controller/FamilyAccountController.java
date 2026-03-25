package com.example.family_account_service.controller;

import com.example.family_account_service.dto.AddMemberRequest;
import com.example.family_account_service.dto.CreateFamilyAccountRequest;
import com.example.family_account_service.dto.FamilyAccountResponse;
import com.example.family_account_service.service.FamilyAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/family-accounts")
@Slf4j
@Tag(name = "Family Accounts", description = "Family account management API")
public class FamilyAccountController {

    private final FamilyAccountService accountService;

    @PostMapping
    @Operation(summary = "Create a new family account")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<FamilyAccountResponse> createAccount(
            @Valid @RequestBody CreateFamilyAccountRequest request) {
        log.debug("POST /family-accounts ownerId={}", request.getOwnerId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createAccount(request));
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get family account by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account found"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<FamilyAccountResponse> getAccount(
            @PathVariable String accountId,
            @RequestHeader("X-Owner-Id") String ownerId) {
        log.debug("GET /family-accounts/{} ownerId={}", accountId, ownerId);
        return ResponseEntity.ok(accountService.getAccount(accountId, ownerId));
    }

    @PostMapping("/{accountId}/members")
    @Operation(summary = "Add member to family account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member added successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "409", description = "Member already exists")
    })
    public ResponseEntity<FamilyAccountResponse> addMember(
            @PathVariable String accountId,
            @RequestHeader("X-Owner-Id") String ownerId,
            @Valid @RequestBody AddMemberRequest request) {
        log.debug("POST /family-accounts/{}/members userId={}", accountId, request.getUserId());
        return ResponseEntity.ok(accountService.addMember(accountId, ownerId, request));
    }

    @DeleteMapping("/{accountId}")
    @Operation(summary = "Close family account")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Account closed successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<Void> closeAccount(
            @PathVariable String accountId,
            @RequestHeader("X-Owner-Id") String ownerId) {
        log.debug("DELETE /family-accounts/{} ownerId={}", accountId, ownerId);
        accountService.closeAccount(accountId, ownerId);
        return ResponseEntity.noContent().build();
    }
}