package com.example.family_account_service.dto;

import com.example.family_account_service.domain.MemberRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddMemberRequest {
    @NotBlank
    private String userId;

    @NotNull
    private MemberRole role;

}
