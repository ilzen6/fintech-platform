package com.example.family_account_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateFamilyAccountRequest {
    @NotBlank
    private String ownerId;

    @NotBlank
    @Size(max = 100)
    private String accountName;
}
