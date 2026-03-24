package com.example.family_account_service.dto;


import com.example.family_account_service.domain.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyAccountResponse {
    private String id;
    private String ownerId;
    private String accountName;
    private BigDecimal balance;
    private AccountStatus status;
    private Integer memberCount;
    private Instant createdAt;
}
