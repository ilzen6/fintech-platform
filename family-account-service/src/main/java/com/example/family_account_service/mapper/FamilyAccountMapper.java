package com.example.family_account_service.mapper;

import com.example.family_account_service.domain.AccountStatus;
import com.example.family_account_service.domain.FamilyAccountDocument;
import com.example.family_account_service.dto.CreateFamilyAccountRequest;
import com.example.family_account_service.dto.FamilyAccountResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;

@Component
public class FamilyAccountMapper {

    public FamilyAccountDocument toDocument(CreateFamilyAccountRequest request) {
        return FamilyAccountDocument.builder()
                .accountName(request.getAccountName())
                .ownerId(request.getOwnerId())
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .members(new ArrayList<>())
                .build();
    }

    public FamilyAccountResponse toResponse(FamilyAccountDocument accountDocument) {
        return FamilyAccountResponse.builder()
                .id(accountDocument.getId())
                .ownerId(accountDocument.getOwnerId())
                .accountName(accountDocument.getAccountName())
                .balance(accountDocument.getBalance())
                .status(accountDocument.getStatus())
                .memberCount(accountDocument.getMembers().size())
                .createdAt(accountDocument.getCreatedAt())
                .build();
    }
}
